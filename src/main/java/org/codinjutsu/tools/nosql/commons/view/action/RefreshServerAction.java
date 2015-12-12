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

package org.codinjutsu.tools.nosql.commons.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.nosql.NoSqlExplorerPanel;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class RefreshServerAction extends AnAction implements DumbAware {

    private static final Icon CONNECT_ICON = GuiUtils.loadIcon("connector.png", "connector_dark.png");
    private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;
    private static final String REFRESH_TEXT = "Refresh this server";
    private static final String CONNECT_TEXT = "Connect to this server";

    private final NoSqlExplorerPanel noSqlExplorerPanel;

    public RefreshServerAction(NoSqlExplorerPanel noSqlExplorerPanel) {
        super(REFRESH_TEXT);
        this.noSqlExplorerPanel = noSqlExplorerPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        noSqlExplorerPanel.reloadServerConfiguration(noSqlExplorerPanel.getSelectedServerNode(), true);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DefaultMutableTreeNode selectedServerNode = noSqlExplorerPanel.getSelectedServerNode();
        event.getPresentation().setVisible(selectedServerNode != null);
        if (selectedServerNode == null) {
            return;
        }
        boolean isConnected = selectedServerNode.getChildCount() > 0;
        event.getPresentation().setIcon(isConnected ? REFRESH_ICON : CONNECT_ICON);
        event.getPresentation().setText(isConnected ? REFRESH_TEXT : CONNECT_TEXT);
    }
}
