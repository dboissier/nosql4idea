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

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.nosql.NoSqlConfiguration;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.DatabaseServer;
import org.codinjutsu.tools.nosql.database.DatabaseVendor;
import org.codinjutsu.tools.nosql.database.DatabaseVendorManager;
import org.codinjutsu.tools.nosql.database.NoSqlDatabase;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.utils.GuiUtils;
import org.codinjutsu.tools.nosql.view.action.*;
import org.codinjutsu.tools.nosql.view.editor.NoSqlFileSystem;
import org.codinjutsu.tools.nosql.view.editor.NoSqlObjectFile;
import org.codinjutsu.tools.nosql.view.editor.mongo.MongoObjectFile;
import org.codinjutsu.tools.nosql.view.editor.redis.RedisObjectFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

public class NoSqlExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.isUnderDarcula() ? GuiUtils.getIconResource("pluginSettings_dark.png") : GuiUtils.getIconResource("pluginSettings.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private Tree nosqlServerTree;

    private JPanel toolBarPanel;

    private final Project project;
    private final DatabaseVendorManager databaseVendorManager;

    public NoSqlExplorerPanel(Project project, DatabaseVendorManager databaseVendorManager) {
        this.project = project;
        this.databaseVendorManager = databaseVendorManager;

        treePanel.setLayout(new BorderLayout());

        nosqlServerTree = createTree();
        nosqlServerTree.setCellRenderer(new NoSqlTreeRenderer());
        nosqlServerTree.setName("nosqlTree");

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(nosqlServerTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                reloadAllServerConfigurations(true);
            }
        });
    }

    public void reloadSelectedServerConfiguration() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {

            @Override
            public void run() {
                nosqlServerTree.invalidate();

                DefaultMutableTreeNode serverNode = getSelectedServerNode();
                if (serverNode == null) {
                    return;
                }

                serverNode.removeAllChildren();

                DatabaseServer databaseServer = (DatabaseServer) serverNode.getUserObject();
                databaseVendorManager.get(project, databaseServer.getConfiguration().getDatabaseVendor()).loadServer(databaseServer);

                addIfPossibleDatabase(databaseServer, serverNode);

                ((DefaultTreeModel) nosqlServerTree.getModel()).reload(serverNode);

                nosqlServerTree.revalidate();

                GuiUtils.expand(nosqlServerTree, TreeUtil.getPathFromRoot(serverNode), 1);
            }
        });
    }


    public void reloadAllServerConfigurations(final boolean loadOnStartup) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    nosqlServerTree.setRootVisible(false);
                    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

                    List<DatabaseServer> databaseServers = databaseVendorManager.loadServers(project, getServerConfigurations(), loadOnStartup);
                    for (DatabaseServer databaseServer : databaseServers) {

                        final DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(databaseServer);
                        rootNode.add(serverNode);

                        if (databaseServer.hasDatabases()) {
                            addIfPossibleDatabase(databaseServer, serverNode);
                        }
                    }

                    nosqlServerTree.invalidate();
                    nosqlServerTree.setModel(new DefaultTreeModel(rootNode));
                    nosqlServerTree.revalidate();

                    TreeUtil.expand(nosqlServerTree, 2);

                } catch (ConfigurationException confEx) {
                    nosqlServerTree.setModel(null);
                    nosqlServerTree.setRootVisible(false);
                }
            }
        });
    }

    private void addIfPossibleDatabase(DatabaseServer databaseServer, DefaultMutableTreeNode serverNode) {
        for (NoSqlDatabase database : databaseServer.getDatabases()) {

            DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(database);
            if (DatabaseVendor.MONGO.equals(databaseServer.getConfiguration().getDatabaseVendor())) {
                MongoDatabase mongoDatabase = (MongoDatabase) database;
                for (MongoCollection collection : mongoDatabase.getCollections()) {
                    databaseNode.add(new DefaultMutableTreeNode(collection));
                }
            }
            serverNode.add(databaseNode);
        }
    }

    private List<ServerConfiguration> getServerConfigurations() {
        return NoSqlConfiguration.getInstance(project).getServerConfigurations();
    }

    public void installActions() {

        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                NoSqlExplorerPanel.this.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                NoSqlExplorerPanel.this.collapseAll();
            }

            @Override
            public boolean canCollapse() {
                return true;
            }
        };

        CommonActionsManager actionsManager = CommonActionsManager.getInstance();

        final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, rootPanel);
        final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, rootPanel);

        Disposer.register(this, new Disposable() {
            @Override
            public void dispose() {
                collapseAllAction.unregisterCustomShortcutSet(rootPanel);
                expandAllAction.unregisterCustomShortcutSet(rootPanel);
            }
        });


        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", false);
        ViewCollectionValuesAction viewCollectionValuesAction = new ViewCollectionValuesAction(this);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshAllServerAction(this));
            actionGroup.add(viewCollectionValuesAction);
            actionGroup.add(expandAllAction);
            actionGroup.add(collapseAllAction);
            actionGroup.add(new MongoConsoleAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }

        GuiUtils.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoExplorerActions", true);

        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoExplorerPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new RefreshServerAction(this));
            actionPopupGroup.add(viewCollectionValuesAction);
            actionPopupGroup.add(new DropCollectionAction(this));
            actionPopupGroup.add(new DropDatabaseAction(this));
        }

        PopupHandler.installPopupHandler(nosqlServerTree, actionPopupGroup, "POPUP", ActionManager.getInstance());

        nosqlServerTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (!(mouseEvent.getSource() instanceof JTree)) {
                    return;
                }

                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nosqlServerTree.getLastSelectedPathComponent();
                if (treeNode == null) {
                    return;
                }

                if (mouseEvent.getClickCount() == 2) {
                    if (treeNode.getUserObject() instanceof DatabaseServer && treeNode.getChildCount() == 0) {
                        reloadSelectedServerConfiguration();
                    } else if (treeNode.getUserObject() instanceof MongoCollection) {
                        loadRecords();
                    } else if (treeNode.getUserObject() instanceof RedisDatabase) {
                        loadRecords();
                    }
                }
            }
        });
    }

    private void expandAll() {
        TreeUtil.expandAll(nosqlServerTree);
    }

    private void collapseAll() {
        TreeUtil.collapseAll(nosqlServerTree, 1);
    }

    @Override
    public void dispose() {
        nosqlServerTree = null;
    }

    public DefaultMutableTreeNode getSelectedServerNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nosqlServerTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return (DefaultMutableTreeNode) treeNode.getParent().getParent();
            }

            if (userObject instanceof NoSqlDatabase) {
                return (DefaultMutableTreeNode) treeNode.getParent();
            }

            if (userObject instanceof DatabaseServer) {
                return treeNode;
            }
        }
        return null;
    }


    private DefaultMutableTreeNode getSelectedDatabaseNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nosqlServerTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
//            if (userObject instanceof MongoCollection) {
//                return (DefaultMutableTreeNode) treeNode.getParent();
//            }

            if (userObject instanceof NoSqlDatabase) {
                return treeNode;
            }
        }

        return null;
    }

    private DefaultMutableTreeNode getSelectedCollectionNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nosqlServerTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return treeNode;
            }
        }
        return null;
    }

    public ServerConfiguration getConfiguration() {

        DefaultMutableTreeNode serverNode = getSelectedServerNode();
        if (serverNode == null) {
            return null;
        }

        return ((DatabaseServer) serverNode.getUserObject()).getConfiguration();
    }

    public MongoDatabase getSelectedDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        return (MongoDatabase) databaseNode.getUserObject();

    }

    public RedisDatabase getSelectedRedisDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        return (RedisDatabase) databaseNode.getUserObject();

    }

    public MongoCollection getSelectedCollection() {
        DefaultMutableTreeNode collectionNode = getSelectedCollectionNode();
        if (collectionNode == null) {
            return null;
        }

        return (MongoCollection) collectionNode.getUserObject();
    }

    public void loadSelectedCollectionValues() {
        NoSqlFileSystem.getInstance().openEditor(createNoSqlObjectFile());
    }

    public void loadRecords() {
        NoSqlFileSystem.getInstance().openEditor(createNoSqlObjectFile());
    }

    private NoSqlObjectFile createNoSqlObjectFile() {

        ServerConfiguration selectedConfiguration = getConfiguration();
        if (DatabaseVendor.MONGO.equals(selectedConfiguration.getDatabaseVendor())) {
            return new MongoObjectFile(project, selectedConfiguration, getSelectedCollection());
        }
        return new RedisObjectFile(project, selectedConfiguration, getSelectedRedisDatabase());
    }

    public void dropCollection() {
        databaseVendorManager.get(project, DatabaseVendor.MONGO).dropCollection(getConfiguration(), getSelectedCollection());
        reloadSelectedServerConfiguration();
    }

    public void dropDatabase() {
        databaseVendorManager.get(project, DatabaseVendor.MONGO).dropDatabase(getConfiguration(), getSelectedDatabase());
        reloadSelectedServerConfiguration();
    }

    private Tree createTree() {

        Tree tree = new Tree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>No NoSql server available<br><br>You may use <img src=\"%s\"> to add configuration</center></html>", pluginSettingsUrl)
            );

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!getServerConfigurations().isEmpty()) return;

                myLabel.setFont(getFont());
                myLabel.setBackground(getBackground());
                myLabel.setForeground(getForeground());
                Rectangle bounds = getBounds();
                Dimension size = myLabel.getPreferredSize();
                myLabel.setBounds(0, 0, size.width, size.height);

                int x = (bounds.width - size.width) / 2;
                Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
                try {
                    myLabel.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        };

        tree.getEmptyText().clear();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return tree;
    }
}
