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

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.redis.logic.RedisClient;
import org.codinjutsu.tools.nosql.redis.model.RedisQuery;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.redis.model.RedisResult;
import org.codinjutsu.tools.nosql.mongo.view.JsonTreeTableView;
import org.codinjutsu.tools.nosql.commons.view.NoSqlResultView;
import org.codinjutsu.tools.nosql.commons.view.action.ExecuteQuery;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class RedisPanel extends NoSqlResultView<RedisResult> {

    private JPanel toolBarPanel;
    private JPanel containerPanel;
    private JPanel errorPanel;
    private JPanel resultPanel;
    private JPanel mainPanel;
    private final LoadingDecorator loadingDecorator;


    private JsonTreeTableView resultTableView;

    private final Project project;
    private final RedisClient redisClient;
    private final ServerConfiguration configuration;
    private final RedisDatabase database;
    private JBTextField separatorField;
    private JBTextField filterField;
    private RedisResult redisResult;
    private boolean groupData;

    public RedisPanel(Project project, RedisClient redisClient, ServerConfiguration configuration, RedisDatabase database) {
        this.project = project;
        this.redisClient = redisClient;
        this.configuration = configuration;
        this.database = database;
        this.resultPanel = new JPanel(new BorderLayout());


        buildQueryToolBar();

        loadingDecorator = new LoadingDecorator(resultPanel, this, 0);

        containerPanel.add(loadingDecorator.getComponent());
        loadAndDisplayResults(getFilter(), getSeparator());

        setLayout(new BorderLayout());
        add(mainPanel);
    }

    private void loadAndDisplayResults(String filter, String separator) {
        redisResult = redisClient.loadRecords(configuration, database, new RedisQuery(filter));
        updateResultTableTree(redisResult, separator);
    }

    private void buildQueryToolBar() {
        toolBarPanel.setLayout(new BorderLayout());

        filterField = new JBTextField("*");
        filterField.setColumns(10);
        separatorField = new JBTextField();
        separatorField.setColumns(3);

        NonOpaquePanel westPanel = new NonOpaquePanel();

        NonOpaquePanel filterPanel = new NonOpaquePanel();
        filterPanel.add(new JLabel("Filter: "), BorderLayout.WEST);
        filterPanel.add(filterField, BorderLayout.CENTER);
        filterPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        westPanel.add(filterPanel, BorderLayout.WEST);

        NonOpaquePanel separatorPanel = new NonOpaquePanel();
//        separatorPanel.add();
        separatorPanel.add(new JLabel("Separator: "), BorderLayout.WEST);
        separatorPanel.add(separatorField, BorderLayout.CENTER);
        separatorPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        westPanel.add(separatorPanel, BorderLayout.CENTER);

        toolBarPanel.add(westPanel, BorderLayout.WEST);

        addCommonsActions();
    }

    protected void addCommonsActions() {
        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                RedisPanel.this.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                RedisPanel.this.collapseAll();
            }

            @Override
            public boolean canCollapse() {
                return true;
            }
        };

        CommonActionsManager actionsManager = CommonActionsManager.getInstance();

        final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, resultPanel);
        final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, resultPanel);

        Disposer.register(this, new Disposable() {
            @Override
            public void dispose() {
                collapseAllAction.unregisterCustomShortcutSet(resultPanel);
                expandAllAction.unregisterCustomShortcutSet(resultPanel);
            }
        });

        DefaultActionGroup actionResultGroup = new DefaultActionGroup("RedisResultGroup", true);
        actionResultGroup.add(new ExecuteQuery<RedisPanel>(this));
        actionResultGroup.addSeparator();
        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);

        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("MongoResultGroupActions", actionResultGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);

        toolBarPanel.add(actionToolBarComponent, BorderLayout.CENTER);
    }

    private String getSeparator() {
        String separator = separatorField.getText();
        if (StringUtils.isNotBlank(separator)) {
            return separator;
        }
        return null;

    }

    private String getFilter() {
        String filter = filterField.getText();
        if (StringUtils.isNotBlank(filter)) {
            return filter;
        }
        return "*";
    }

    void expandAll() {
        TreeUtil.expandAll(resultTableView.getTree());
    }

    void collapseAll() {
        TreeTableTree tree = resultTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }

    public void updateResultTableTree(RedisResult redisResult, String separator) {
        NoSqlTreeNode rootNode = RedisTreeModel.buildTree(redisResult);
        DefaultMutableTreeNode targetRootNode = RedisFragmentedKeyTreeModel.wrapNodes(rootNode, separator);
        resultTableView = new JsonTreeTableView(targetRootNode, JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultPanel.invalidate();
        resultPanel.removeAll();
        resultPanel.add(new JBScrollPane(resultTableView));
        resultPanel.validate();
    }

    @Override
    public void showResults() {

    }

    @Override
    public JPanel getResultPanel() {
        return resultPanel;
    }

    @Override
    public RedisResult getRecords() {
        return redisResult;
    }

    @Override
    public void executeQuery() {
        loadAndDisplayResults(getFilter(), getSeparator());
    }

    @Override
    public void dispose() {

    }

    public boolean isGroupDataEnabled() {
        return this.groupData;
    }

    public void toggleGroupData(boolean state) {
        this.groupData = state;
    }

    public void renderRecords() {
        if (this.groupData) {
            //TODO
        }
    }
}
