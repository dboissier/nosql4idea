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

package org.codinjutsu.tools.nosql.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.nosql.NoSqlExplorerPanel;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;

import javax.swing.*;

/**
 * Created by piddubnyi on 06.11.14 .
 */
public class DropDatabaseAction extends AnAction implements DumbAware {

    private static final Icon REMOVE_ICON = AllIcons.Actions.GC;

    private final NoSqlExplorerPanel noSqlExplorerPanel;

    public DropDatabaseAction(NoSqlExplorerPanel noSqlExplorerPanel) {
        super("Drop Database", "Drop the selected database", REMOVE_ICON);
        this.noSqlExplorerPanel = noSqlExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        int result = JOptionPane.showConfirmDialog (null, String.format("Do you REALLY want to drop the '%s' database?", noSqlExplorerPanel.getSelectedMongoDatabase().getName()),"Warning",JOptionPane.YES_NO_OPTION);

        if(result == JOptionPane.YES_OPTION){
            noSqlExplorerPanel.dropDatabase();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(noSqlExplorerPanel.getSelectedMongoDatabase() != null);
    }
}