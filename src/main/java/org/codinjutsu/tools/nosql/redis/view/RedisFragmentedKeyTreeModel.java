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

package org.codinjutsu.tools.nosql.redis.view;

import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.redis.view.nodedescriptor.FragmentedKeyNodeDescriptor;
import org.codinjutsu.tools.nosql.redis.view.nodedescriptor.RedisKeyValueDescriptor;
import org.codinjutsu.tools.nosql.commons.utils.StringUtils;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.util.Arrays;
import java.util.Enumeration;

import static org.apache.commons.lang.StringUtils.*;

public class RedisFragmentedKeyTreeModel extends DefaultTreeModel {

    private String separator;
    private DefaultTreeModel fragmentedKeyModel;

    private boolean needsUpdate = true;

    public RedisFragmentedKeyTreeModel(NoSqlTreeNode root) {
        super(root);
        this.separator = "";
    }

    public void setSeparator(String separator) {
        this.separator = separator;
        needsUpdate = true;
        fireSeparatorChanged();
    }

    public void resetSeparator() {
        this.separator = "";
        needsUpdate = true;
        fireSeparatorChanged();
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
        fireSeparatorChanged();
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
        wrapNodes(sourceRoot, separator);
        fragmentedKeyModel = new DefaultTreeModel(targetRoot);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                fragmentedKeyModel.addTreeModelListener((TreeModelListener) listeners[i + 1]);
            }
        }
        fireSeparatorChanged();
    }

    private void fireSeparatorChanged() {
        getFragmentedKeyModel().reload();
    }

    public static DefaultMutableTreeNode wrapNodes(DefaultMutableTreeNode source, String separator) {
        if (isEmpty(separator)) {
            return source;
        }
        DefaultMutableTreeNode targetRootNode = (DefaultMutableTreeNode) source.clone();
        for (int i = 0; i < source.getChildCount(); i++) {
            DefaultMutableTreeNode originalChildNode = (DefaultMutableTreeNode) source.getChildAt(i);
            NoSqlTreeNode clonedChildNode = (NoSqlTreeNode) originalChildNode.clone();
            RedisKeyValueDescriptor descriptor = (RedisKeyValueDescriptor) clonedChildNode.getDescriptor();
            String[] explodedKey = StringUtils.explode(descriptor.getKey(), separator);
            if (explodedKey.length == 1) {
                addChildren(clonedChildNode, originalChildNode);
                targetRootNode.add(clonedChildNode);
            } else {
                updateTree(targetRootNode, originalChildNode, explodedKey, descriptor);
            }
        }
        return targetRootNode;
    }

    private static void updateTree(DefaultMutableTreeNode parentTargetNode, DefaultMutableTreeNode originalChildNode, String[] explodedKey, RedisKeyValueDescriptor sourceDescriptor) {
        if (explodedKey.length == 0) {
            addChildren(parentTargetNode, originalChildNode);
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
        updateTree(node, originalChildNode, Arrays.copyOfRange(explodedKey, 1, explodedKey.length), sourceDescriptor);

        parentTargetNode.add(node);
    }

    private static NoSqlTreeNode findNodeByKey(DefaultMutableTreeNode parentTargetNode, String keyFragment) {
        for (int i = 0; i < parentTargetNode.getChildCount(); i++) {
            NoSqlTreeNode currentChild = (NoSqlTreeNode) parentTargetNode.getChildAt(i);
            NodeDescriptor descriptor = currentChild.getDescriptor();
            String nodeKey;
            if (descriptor instanceof FragmentedKeyNodeDescriptor) {
                nodeKey = ((FragmentedKeyNodeDescriptor) descriptor).getKeyFragment();
            } else if (descriptor instanceof RedisKeyValueDescriptor){
                nodeKey = ((RedisKeyValueDescriptor) descriptor).getKey();
            } else {
                return null;
            }
            if (keyFragment.equals(nodeKey)) {
                return currentChild;
            }
        }
        return null;
    }

    private static void addChildren(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode originalChildNode) {
        Enumeration children = originalChildNode.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
            parentNode.add((MutableTreeNode) childNode.clone());
        }
    }
}
