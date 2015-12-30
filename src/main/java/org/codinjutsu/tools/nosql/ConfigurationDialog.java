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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.codinjutsu.tools.nosql.commons.view.ServerConfigurationPanel;
import org.codinjutsu.tools.nosql.commons.view.ServerConfigurationPanelFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class ConfigurationDialog extends DialogWrapper {

    private final ServerConfigurationPanelFactory serverConfigurationPanelFactory;
    private final ServerConfiguration configuration;
    private ServerConfigurationPanel serverConfigurationPanel;

    ConfigurationDialog(Component parent,
                        ServerConfigurationPanelFactory serverConfigurationPanelFactory,
                        ServerConfiguration configuration) {
        super(parent, true);
        this.serverConfigurationPanelFactory = serverConfigurationPanelFactory;
        this.configuration = configuration;

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        serverConfigurationPanel = this.serverConfigurationPanelFactory.create(configuration.getDatabaseVendor());
        serverConfigurationPanel.loadConfigurationData(configuration);
        return serverConfigurationPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        return serverConfigurationPanel.validateInputs();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        serverConfigurationPanel.applyConfigurationData(configuration);
    }
}
