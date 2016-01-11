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

package org.codinjutsu.tools.nosql.mongo.view;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.bson.Document;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.codinjutsu.tools.nosql.commons.view.ErrorPanel;
import org.codinjutsu.tools.nosql.commons.view.NoSqlResultView;
import org.codinjutsu.tools.nosql.commons.view.action.ExecuteQuery;
import org.codinjutsu.tools.nosql.mongo.logic.SingleMongoClient;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoCollection;
import org.codinjutsu.tools.nosql.mongo.view.action.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MongoPanel extends NoSqlResultView<SingleMongoCollection> {

    private final LoadingDecorator loadingDecorator;
    private JPanel rootPanel;
    private Splitter splitter;
    private JPanel toolBar;
    private JPanel errorPanel;
    private final JTextField rowLimitField = new JTextField("");
    private final MongoResultPanel resultPanel;
    private final QueryPanel queryPanel;

    private final Project project;
    private final SingleMongoClient singleMongoClient;
    private final ServerConfiguration configuration;
    private final SingleMongoCollection singleMongoCollection;

    public MongoPanel(Project project, final SingleMongoClient singleMongoClient, final ServerConfiguration configuration, final SingleMongoCollection singleMongoCollection) {
        this.project = project;
        this.singleMongoClient = singleMongoClient;
        this.singleMongoCollection = singleMongoCollection;
        this.configuration = configuration;

        errorPanel.setLayout(new BorderLayout());

        queryPanel = new QueryPanel(project);
        queryPanel.setVisible(false);

        resultPanel = createResultPanel(project, new MongoDocumentOperations() {

            public Document getMongoDocument(Object _id) {
                return singleMongoClient.findMongoDocument(configuration, singleMongoCollection, _id);
            }

            public void updateMongoDocument(Document mongoDocument) {
                singleMongoClient.update(configuration, singleMongoCollection, mongoDocument);
                executeQuery();
            }

            public void deleteMongoDocument(Object objectId) {
                singleMongoClient.delete(configuration, singleMongoCollection, objectId);
                executeQuery();
            }
        });

        loadingDecorator = new LoadingDecorator(resultPanel, this, 0);


        splitter.setOrientation(true);
        splitter.setProportion(0.2f);
        splitter.setSecondComponent(loadingDecorator.getComponent());

        setLayout(new BorderLayout());
        add(rootPanel);

        initToolBar();
    }

    private void initToolBar() {
        toolBar.setLayout(new BorderLayout());

        rowLimitField.setColumns(5);
        rowLimitField.setDocument(new NumberDocument());

        JPanel rowLimitPanel = new NonOpaquePanel();
        rowLimitPanel.add(new JLabel("Row limit:"), BorderLayout.WEST);
        rowLimitPanel.add(rowLimitField, BorderLayout.CENTER);
        rowLimitPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        toolBar.add(rowLimitPanel, BorderLayout.WEST);

        installResultPanelActions();
    }

    private MongoResultPanel createResultPanel(Project project, MongoDocumentOperations mongoDocumentOperations) {
        return new MongoResultPanel(project, mongoDocumentOperations);
    }


    void installResultPanelActions() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionResultGroup.add(new ExecuteQuery<>(this));
            actionResultGroup.add(new OpenFindAction(this));
            actionResultGroup.add(new EnableAggregateAction(queryPanel));
            actionResultGroup.addSeparator();
            actionResultGroup.add(new AddMongoDocumentAction(resultPanel));
            actionResultGroup.add(new EditMongoDocumentAction(resultPanel));
            actionResultGroup.add(new CopyResultAction(resultPanel));
        }
        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                resultPanel.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                resultPanel.collapseAll();
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

        actionResultGroup.addSeparator();
        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);
        actionResultGroup.add(new CloseFindEditorAction(this));

        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("MongoResultGroupActions", actionResultGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);

        toolBar.add(actionToolBarComponent, BorderLayout.CENTER);
    }

    public SingleMongoCollection getRecords() {
        return singleMongoCollection;
    }


    public void showResults() {
        executeQuery();
    }

    public void executeQuery() {
        errorPanel.setVisible(false);
        validateQuery();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Executing Query", true)  {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                try {
                    GuiUtils.runInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDecorator.startLoading(false);
                        }
                    });

                    final MongoResult mongoResult = singleMongoClient.loadCollectionValues(configuration, singleMongoCollection, queryPanel.getQueryOptions(rowLimitField.getText()));
                    GuiUtils.runInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            resultPanel.updateResultTableTree(mongoResult);
                        }
                    });
                } catch (final Exception ex) {
                    GuiUtils.runInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            errorPanel.invalidate();
                            errorPanel.removeAll();
                            errorPanel.add(new ErrorPanel(ex), BorderLayout.CENTER);
                            errorPanel.validate();
                            errorPanel.setVisible(true);
                        }
                    });
                } finally {
                    GuiUtils.runInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDecorator.stopLoading();
                        }
                    });
                }
            }
        });
    }

    private void validateQuery() {
        queryPanel.validateQuery();
    }

    @Override
    public void dispose() {
        resultPanel.dispose();
    }

    public MongoResultPanel getResultPanel() {
        return resultPanel;
    }

    public void openFindEditor() {
        queryPanel.setVisible(true);
        splitter.setFirstComponent(queryPanel);
        GuiUtils.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                focusOnEditor();
            }
        });
    }

    public void closeFindEditor() {
        splitter.setFirstComponent(null);
        queryPanel.setVisible(false);
    }

    public void focusOnEditor() {
        queryPanel.requestFocusOnEditor();
    }

    public boolean isFindEditorOpened() {
        return splitter.getFirstComponent() == queryPanel;
    }

    interface MongoDocumentOperations {
        Document getMongoDocument(Object _id);

        void deleteMongoDocument(Object mongoDocumentID);

        void updateMongoDocument(Document mongoDocument);
    }
}
