/*
 * Copyright (c) 2015 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.nosql.view;

import org.codinjutsu.tools.nosql.utils.StringUtils;
import org.codinjutsu.tools.nosql.view.model.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.view.nodedescriptor.redis.FragmentedKeyNodeDescriptor;
import org.codinjutsu.tools.nosql.view.nodedescriptor.redis.RedisKeyValueDescriptor;
import org.jetbrains.jps.incremental.storage.FileKeyDescriptor;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Arrays;

public class RedisFragmentedKeyTreeModel extends DefaultTreeModel {

    private String separator;
    private DefaultTreeModel fragmentedKeyModel;

    private boolean needsUpdate = true;

    public RedisFragmentedKeyTreeModel(TreeNode root) {
        super(root);
        this.separator = "";
    }

    public void setSeparator(String separator) {
        this.separator = separator;
        needsUpdate = true;
        fireFilterChanged();
    }

    public void resetSeparator() {
        this.separator = "";
        needsUpdate = true;
        fireFilterChanged();
    }

    @Override
    public DefaultMutableTreeNode getRoot() {
        return (DefaultMutableTreeNode) getFragmentedKeyModel().getRoot();
    }

    @Override
    public void reload() {
        super.reload();
        getFragmentedKeyModel().reload();
        needsUpdate = true;
        fireFilterChanged();
    }

    @Override
    public int getChildCount(Object parent) {
        return getFragmentedKeyModel().getChildCount(parent);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getFragmentedKeyModel().getChild(parent, index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getFragmentedKeyModel().getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        super.addTreeModelListener(listener);
        getFragmentedKeyModel().addTreeModelListener(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        super.removeTreeModelListener(listener);
        getFragmentedKeyModel().removeTreeModelListener(listener);
    }

    @Override
    public boolean isLeaf(Object node) {
        return getFragmentedKeyModel().isLeaf(node);
    }

    private DefaultTreeModel getFragmentedKeyModel() {
        if (needsUpdate) {
            needsUpdate = false;
            updateFilteredModel();
        }
        return fragmentedKeyModel;
    }

    private void updateFilteredModel() {
        DefaultMutableTreeNode sourceRoot = (DefaultMutableTreeNode) super.getRoot();
        DefaultMutableTreeNode targetRoot = (DefaultMutableTreeNode) sourceRoot.clone();
        wrapNodes(sourceRoot, targetRoot);
        fragmentedKeyModel = new DefaultTreeModel(targetRoot);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                fragmentedKeyModel.addTreeModelListener((TreeModelListener) listeners[i + 1]);
            }
        }
        fireFilterChanged();
    }

    private void fireFilterChanged() {
        getFragmentedKeyModel().reload();
    }

    private void wrapNodes(DefaultMutableTreeNode source, DefaultMutableTreeNode target) {
        for (int i = 0; i < super.getChildCount(source); i++) {
            DefaultMutableTreeNode sourceChild = (DefaultMutableTreeNode) super.getChild(source, i);
            NoSqlTreeNode redisChild = (NoSqlTreeNode) sourceChild.clone();
            RedisKeyValueDescriptor descriptor = (RedisKeyValueDescriptor) redisChild.getDescriptor();
            String[] explodedKey = StringUtils.explode(descriptor.getKey(), separator);
            if (explodedKey.length == 1) {
                target.add(redisChild);
            } else {
                updateTree(target, explodedKey, descriptor);
            }
        }
    }

    private void updateTree(DefaultMutableTreeNode parentTargetNode, String[] explodedKey, RedisKeyValueDescriptor sourceDescriptor) {
        if (explodedKey.length == 0) {
             return;
        }
        String keyFragment = explodedKey[0];
        NoSqlTreeNode node = findNodeByKey(parentTargetNode, keyFragment);
        if (node == null) {
            if (explodedKey.length == 1) {
                node = new NoSqlTreeNode(RedisKeyValueDescriptor.createDescriptor(sourceDescriptor.getKeyType(), keyFragment, sourceDescriptor.getValue()));
            } else {
                node = new NoSqlTreeNode(FragmentedKeyNodeDescriptor.createDescriptor(keyFragment));
            }
        }
        updateTree(node, Arrays.copyOfRange(explodedKey, 1, explodedKey.length), sourceDescriptor);

        parentTargetNode.add(node);
    }

    private NoSqlTreeNode findNodeByKey(DefaultMutableTreeNode parentTargetNode, String keyFragment) {
        for (int i = 0; i < parentTargetNode.getChildCount(); i++) {
            NoSqlTreeNode currentChild = (NoSqlTreeNode) parentTargetNode.getChildAt(i);
            NodeDescriptor descriptor = currentChild.getDescriptor();
            String nodeKey;
            if (descriptor instanceof FragmentedKeyNodeDescriptor) {
                nodeKey = ((FragmentedKeyNodeDescriptor) descriptor).getKeyFragment();
            } else {
                nodeKey = ((RedisKeyValueDescriptor) descriptor).getKey();
            }
            if (keyFragment.equals(nodeKey)) {
                return currentChild;
            }
        }
        return null;
    }
}
