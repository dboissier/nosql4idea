/*
 * Copyright (c) 2013 David Boissier
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class ConfigurationDialog extends DialogWrapper {

    private final Project project;
    private final DatabaseVendorManager databaseVendorManager;
    private final ServerConfiguration configuration;
    private ServerConfigurationPanel serverConfigurationPanel;

    ConfigurationDialog(Component parent, Project project, DatabaseVendorManager databaseVendorManager, ServerConfiguration configuration) {
        super(parent, true);
        this.project = project;
        this.databaseVendorManager = databaseVendorManager;
        this.configuration = configuration;

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        serverConfigurationPanel = new ServerConfigurationPanel(project, databaseVendorManager);
        serverConfigurationPanel.loadConfigurationData(configuration);
        return serverConfigurationPanel;
    }


    @Override
    protected void doOKAction() {
        try {
            serverConfigurationPanel.applyConfigurationData(configuration);
        } catch (ConfigurationException confEx) {
            serverConfigurationPanel.setErrorMessage(confEx.getMessage());
        }
        super.doOKAction();
    }
}
