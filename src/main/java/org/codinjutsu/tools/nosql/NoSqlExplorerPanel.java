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

package org.codinjutsu.tools.nosql;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.codinjutsu.tools.nosql.commons.view.action.NoSqlDatabaseConsoleAction;
import org.codinjutsu.tools.nosql.commons.view.action.OpenPluginSettingsAction;
import org.codinjutsu.tools.nosql.commons.view.action.RefreshServerAction;
import org.codinjutsu.tools.nosql.commons.view.action.ViewCollectionValuesAction;
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseFileSystem;
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseObjectFile;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.codinjutsu.tools.nosql.couchbase.view.editor.CouchbaseObjectFile;
import org.codinjutsu.tools.nosql.mongo.logic.MongoClient;
import org.codinjutsu.tools.nosql.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.mongo.view.action.DropCollectionAction;
import org.codinjutsu.tools.nosql.mongo.view.action.DropDatabaseAction;
import org.codinjutsu.tools.nosql.mongo.view.editor.MongoObjectFile;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.redis.view.editor.RedisObjectFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

import static org.codinjutsu.tools.nosql.commons.utils.GuiUtils.showNotification;

public class NoSqlExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.isUnderDarcula() ? GuiUtils.getIconResource("pluginSettings_dark.png") : GuiUtils.getIconResource("pluginSettings.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private Tree databaseTree;

    private JPanel toolBarPanel;

    private final Project project;
    private final DatabaseVendorClientManager databaseVendorClientManager;

    public NoSqlExplorerPanel(Project project, DatabaseVendorClientManager databaseVendorClientManager) {
        this.project = project;
        this.databaseVendorClientManager = databaseVendorClientManager;

        treePanel.setLayout(new BorderLayout());

        databaseTree = createTree();
        databaseTree.setCellRenderer(new NoSqlTreeRenderer());
        databaseTree.setName("databaseTree");

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(databaseTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                reloadAllServerConfigurations();
            }
        });
    }

    public void reloadAllServerConfigurations() {
        this.databaseVendorClientManager.cleanUpServers();
        databaseTree.setRootVisible(false);

        List<ServerConfiguration> serverConfigurations = getServerConfigurations();
        if (serverConfigurations.size() == 0) {
            databaseTree.setModel(null);
            return;
        }

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        databaseTree.setModel(new DefaultTreeModel(rootNode));

        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            DatabaseServer mongoServer = new DatabaseServer(serverConfiguration);
            this.databaseVendorClientManager.registerServer(mongoServer);
            DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(mongoServer);
            rootNode.add(serverNode);
            if (serverConfiguration.isConnectOnIdeStartup()) {
                this.reloadServerConfiguration(serverNode, false);
            }
        }

        TreeUtil.expand(databaseTree, 2);
    }


    public void reloadServerConfiguration(final DefaultMutableTreeNode serverNode, final boolean expandAfterLoading) {
        databaseTree.setPaintBusy(true);

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {

            @Override
            public void run() {
                final DatabaseServer databaseServer = (DatabaseServer) serverNode.getUserObject();
                try {
                    databaseVendorClientManager.loadServer(databaseServer);

                    GuiUtils.runInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            databaseTree.invalidate();

                            serverNode.removeAllChildren();
                            addDatabasesIfAny(databaseServer, serverNode);

                            ((DefaultTreeModel) databaseTree.getModel()).reload(serverNode);

                            databaseTree.revalidate();

                            if (expandAfterLoading) {
                                GuiUtils.expand(databaseTree, TreeUtil.getPathFromRoot(serverNode), 1);
                            }

                        }
                    });

                } catch (ConfigurationException confEx) {
                    databaseServer.setStatus(DatabaseServer.Status.ERROR);
                    showNotification(treePanel,
                            MessageType.ERROR,
                            String.format("Error when connecting on %s", databaseServer.getLabel()),
                            Balloon.Position.atLeft);
                } finally {
                    databaseTree.setPaintBusy(false);
                }
            }
        });
    }

    private void addDatabasesIfAny(DatabaseServer databaseServer, DefaultMutableTreeNode serverNode) {
        for (Database database : databaseServer.getDatabases()) {
            DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(database);
            if (database instanceof MongoDatabase) {
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


        DefaultActionGroup actionGroup = new DefaultActionGroup("NoSqlExplorerGroup", false);
        ViewCollectionValuesAction viewCollectionValuesAction = new ViewCollectionValuesAction(this);
        RefreshServerAction refreshServerAction = new RefreshServerAction(this);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(refreshServerAction);
            actionGroup.add(new NoSqlDatabaseConsoleAction(this));
            actionGroup.add(viewCollectionValuesAction);
            actionGroup.add(expandAllAction);
            actionGroup.add(collapseAllAction);
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }

        GuiUtils.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "NoSqlExplorerActions", true);

        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("NoSqlExplorerPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(refreshServerAction);
            actionPopupGroup.add(viewCollectionValuesAction);
            actionPopupGroup.add(new DropCollectionAction(this));
            actionPopupGroup.add(new DropDatabaseAction(this));
        }

        PopupHandler.installPopupHandler(databaseTree, actionPopupGroup, "POPUP", ActionManager.getInstance());

        databaseTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (!(mouseEvent.getSource() instanceof JTree)) {
                    return;
                }

                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) databaseTree.getLastSelectedPathComponent();
                if (treeNode == null) {
                    return;
                }

                if (mouseEvent.getClickCount() == 2) {
                    if (treeNode.getUserObject() instanceof DatabaseServer && treeNode.getChildCount() == 0) {
                        reloadServerConfiguration(getSelectedServerNode(), true);
                    }
                    if (treeNode.getUserObject() instanceof MongoCollection) {
                        loadRecords();
                    }
                    if (treeNode.getUserObject() instanceof RedisDatabase) {
                        loadRecords();
                    }

                    if (treeNode.getUserObject() instanceof CouchbaseDatabase) {
                        loadRecords();
                    }
                }
            }
        });
    }

    private void expandAll() {
        TreeUtil.expandAll(databaseTree);
    }

    private void collapseAll() {
        TreeUtil.collapseAll(databaseTree, 1);
    }

    @Override
    public void dispose() {
        databaseTree = null;
    }

    public DefaultMutableTreeNode getSelectedServerNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) databaseTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return (DefaultMutableTreeNode) treeNode.getParent().getParent();
            }

            if (userObject instanceof Database) {
                return (DefaultMutableTreeNode) treeNode.getParent();
            }

            if (userObject instanceof DatabaseServer) {
                return treeNode;
            }
        }
        return null;
    }


    private DefaultMutableTreeNode getSelectedDatabaseNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) databaseTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
//            if (userObject instanceof MongoCollection) {
//                return (DefaultMutableTreeNode) treeNode.getParent();
//            }

            if (userObject instanceof Database) {
                return treeNode;
            }
        }

        return null;
    }

    private DefaultMutableTreeNode getSelectedCollectionNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) databaseTree.getLastSelectedPathComponent();
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

    public RedisDatabase getSelectedRedisDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        Object database = databaseNode.getUserObject();
        if (!(database instanceof RedisDatabase)) {
            return null;
        }
        return (RedisDatabase) database;

    }

    public CouchbaseDatabase getSelectedCouchaseDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        Object database = databaseNode.getUserObject();
        if (!(database instanceof CouchbaseDatabase)) {
            return null;
        }
        return (CouchbaseDatabase) database;
    }

    public MongoDatabase getSelectedMongoDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        Object database = databaseNode.getUserObject();
        if (!(database instanceof MongoDatabase)) {
            return null;
        }

        return (MongoDatabase) databaseNode.getUserObject();

    }

    public MongoCollection getSelectedCollection() {
        DefaultMutableTreeNode collectionNode = getSelectedCollectionNode();
        if (collectionNode == null) {
            return null;
        }

        return (MongoCollection) collectionNode.getUserObject();
    }

    public void loadRecords() {
        NoSqlDatabaseFileSystem.getInstance().openEditor(createNoSqlObjectFile());
    }

    @NotNull
    private NoSqlDatabaseObjectFile createNoSqlObjectFile() { // TODO need to put in the database UI manager
        ServerConfiguration selectedConfiguration = getConfiguration();
        if (DatabaseVendor.MONGO.equals(selectedConfiguration.getDatabaseVendor())) {
            return new MongoObjectFile(project, selectedConfiguration, getSelectedCollection());
        } else if (DatabaseVendor.COUCHBASE.equals(selectedConfiguration.getDatabaseVendor())) {
            return new CouchbaseObjectFile(project, selectedConfiguration, getSelectedCouchaseDatabase());
        }
        return new RedisObjectFile(project, selectedConfiguration, getSelectedRedisDatabase());
    }

    public void dropCollection() {// TODO need to put in a customizer
        MongoClient databaseClient = (MongoClient) databaseVendorClientManager.get(DatabaseVendor.MONGO);
        databaseClient.dropCollection(getConfiguration(), getSelectedCollection());
        reloadServerConfiguration(getSelectedServerNode(), true);
    }

    public void dropDatabase() {// TODO need to put in a customizer
        MongoClient databaseClient = (MongoClient) databaseVendorClientManager.get(DatabaseVendor.MONGO);
        databaseClient.dropDatabase(getConfiguration(), getSelectedMongoDatabase());
        reloadServerConfiguration(getSelectedServerNode(), true);
    }

    private Tree createTree() {

        Tree tree = new Tree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>NoSql server list is empty<br><br>You may use <img src=\"%s\"> to add configuration</center></html>", pluginSettingsUrl)
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
