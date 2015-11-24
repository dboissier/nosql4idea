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

package org.codinjutsu.tools.nosql.couchbase.view;

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
import com.intellij.ui.NumberDocument;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.NoSqlResultView;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.commons.view.action.ExecuteQuery;
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseClient;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseQuery;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseResult;
import org.codinjutsu.tools.nosql.mongo.view.JsonTreeTableView;

import javax.swing.*;
import java.awt.*;

public class CouchbasePanel extends NoSqlResultView<CouchbaseDatabase> {
    private JPanel mainPanel;
    private JPanel toolBarPanel;
    private JPanel containerPanel;
    private JPanel resultPanel;
    private final LoadingDecorator loadingDecorator;
    private final JTextField rowLimitField = new JTextField("");

    private final Project project;

    private final CouchbaseClient couchbaseClient;
    private final ServerConfiguration configuration;
    private final CouchbaseDatabase database;
    private CouchbaseResult couchbaseResult;
    private JsonTreeTableView resultTableView;


    public CouchbasePanel(Project project, CouchbaseClient couchbaseClient, ServerConfiguration configuration, CouchbaseDatabase database) {
        this.project = project;
        this.couchbaseClient = couchbaseClient;
        this.configuration = configuration;
        this.database = database;
        this.resultPanel = new JPanel(new BorderLayout());

        loadingDecorator = new LoadingDecorator(resultPanel, this, 0);

        containerPanel.add(loadingDecorator.getComponent());

        initToolbar();

        loadAndDisplayResults(getLimit());

        setLayout(new BorderLayout());
        add(mainPanel);
    }

    private void loadAndDisplayResults(int limit) {
        couchbaseResult = couchbaseClient.loadRecords(configuration, database, new CouchbaseQuery(limit));
        updateResultTableTree(couchbaseResult);
    }

    void updateResultTableTree(CouchbaseResult couchbaseResult) {
        NoSqlTreeNode rootNode = CouchbaseTreeModel.buildTree(couchbaseResult);
        resultTableView = new JsonTreeTableView(rootNode, JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultPanel.invalidate();
        resultPanel.removeAll();
        resultPanel.add(new JBScrollPane(resultTableView));
        resultPanel.validate();
    }

    private void initToolbar() {
        toolBarPanel.setLayout(new BorderLayout());

        rowLimitField.setColumns(5);
        rowLimitField.setDocument(new NumberDocument());
        rowLimitField.setText("100");

        JPanel rowLimitPanel = new NonOpaquePanel();
        rowLimitPanel.add(new JLabel("Row limit:"), BorderLayout.WEST);
        rowLimitPanel.add(rowLimitField, BorderLayout.CENTER);
        rowLimitPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        toolBarPanel.add(rowLimitPanel, BorderLayout.WEST);

        addCommonsActions();
    }

    protected void addCommonsActions() {
        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                CouchbasePanel.this.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                CouchbasePanel.this.collapseAll();
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

        DefaultActionGroup actionResultGroup = new DefaultActionGroup("CouchbaseResultGroup", true);
        actionResultGroup.add(new ExecuteQuery<>(this));
        actionResultGroup.addSeparator();
        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);

        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("CouchbaseResultGroupActions", actionResultGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);

        toolBarPanel.add(actionToolBarComponent, BorderLayout.CENTER);
    }


    private int getLimit() {
        return Integer.parseInt(rowLimitField.getText());
    }

    void expandAll() {
        TreeUtil.expandAll(resultTableView.getTree());
    }

    void collapseAll() {
        TreeTableTree tree = resultTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }
    
    @Override
    public void showResults() {

    }

    @Override
    public JPanel getResultPanel() {
        return resultPanel;
    }

    @Override
    public CouchbaseDatabase getRecords() {
        return database;
    }

    @Override
    public void executeQuery() {
        loadAndDisplayResults(getLimit());
    }

    @Override
    public void dispose() {

    }
}
