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

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.codinjutsu.tools.nosql.mongo.model.MongoCollection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class NoSqlTreeRenderer extends ColoredTreeCellRenderer {

    private static final Icon DATABASE = GuiUtils.loadIcon("database.png");
    private static final Icon MONGO_COLLECTION = AllIcons.Nodes.Folder;

    @Override
    public void customizeCellRenderer(@NotNull JTree mongoTree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row, boolean focus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object userObject = node.getUserObject();
        if (userObject instanceof DatabaseServer) {
            DatabaseServer mongoServer = (DatabaseServer) userObject;
            String label = mongoServer.getLabel();
            String host = mongoServer.getServerUrl();
            append(StringUtils.isBlank(label) ? host : label);

            if (DatabaseServer.Status.OK.equals(mongoServer.getStatus())) {
                setToolTipText(host);
                setIcon(mongoServer.getConfiguration().getDatabaseVendor().icon);
            } else {
                setForeground(JBColor.RED);
                setToolTipText("Unable to connect");
            }
        } else if (userObject instanceof Database) {
            Database noSqlDatabase = (Database) userObject;
            append(noSqlDatabase.getName());
            setIcon(DATABASE);
        } else if (userObject instanceof MongoCollection) {
            MongoCollection mongoCollection = (MongoCollection) userObject;
            append(mongoCollection.getName());
            setIcon(MONGO_COLLECTION);
        }
    }
}
