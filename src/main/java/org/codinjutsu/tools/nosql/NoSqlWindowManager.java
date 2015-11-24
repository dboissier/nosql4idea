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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;

import javax.swing.*;


public class NoSqlWindowManager {

    private static final Icon NOSQL_ICON = GuiUtils.loadIcon("nosql_13x13.png");

    public static final String NOSQL_RUNNER = "NoSql Runner";
    private static final String NOSQL_EXPLORER = "NoSql Explorer";

    private final Project project;
    private final NoSqlExplorerPanel noSqlExplorerPanel;

    public static NoSqlWindowManager getInstance(Project project) {
        return ServiceManager.getService(project, NoSqlWindowManager.class);
    }

    public NoSqlWindowManager(Project project) {
        this.project = project;

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        noSqlExplorerPanel = new NoSqlExplorerPanel(project, DatabaseVendorManager.getInstance(project));
        noSqlExplorerPanel.installActions();
        Content nosqlExplorer = ContentFactory.SERVICE.getInstance().createContent(noSqlExplorerPanel, null, false);

        ToolWindow toolNoSqlExplorerWindow = toolWindowManager.registerToolWindow(NOSQL_EXPLORER, false, ToolWindowAnchor.RIGHT);
        toolNoSqlExplorerWindow.getContentManager().addContent(nosqlExplorer);
        toolNoSqlExplorerWindow.setIcon(NOSQL_ICON);
    }

    public void unregisterMyself() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(NOSQL_RUNNER);
        ToolWindowManager.getInstance(project).unregisterToolWindow(NOSQL_EXPLORER);
    }

    public void apply() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                noSqlExplorerPanel.reloadAllServerConfigurations();
            }
        });
    }
}
