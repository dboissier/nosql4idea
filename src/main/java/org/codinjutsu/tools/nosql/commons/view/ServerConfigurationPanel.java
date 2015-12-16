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

package org.codinjutsu.tools.nosql.commons.view;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerConfigurationPanel extends JPanel {

    private JPanel rootPanel;
    private JTextField labelField;
    private JPanel authenticationContainer;
    private JTextField serverUrlField;
    private JTextField userDatabaseField;
    private JCheckBox autoConnectCheckBox;
    private JButton testConnectionButton;
    private JPanel mongoShellOptionsPanel;
    private TextFieldWithBrowseButton shellWorkingDirField;
    private RawCommandLineEditor shellArgumentsLineField;
    private JLabel databaseTipsLabel;
    private JLabel databaseVendorLabel;

    private final Project project;

    private final DatabaseClient databaseClient;
    private final DatabaseVendor databaseVendor;
    private final AuthenticationView authenticationView;


    public ServerConfigurationPanel(Project project,
                                    DatabaseVendor databaseVendor,
                                    DatabaseClient databaseClient,
                                    AuthenticationView authenticationView) {
        this.project = project;
        this.databaseClient = databaseClient;
        this.databaseVendor = databaseVendor;
        this.authenticationView = authenticationView;

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        authenticationContainer.add(authenticationView.getComponent());

        labelField.setName("labelField");
        databaseVendorLabel.setName("databaseVendorLabel");
        databaseVendorLabel.setText(databaseVendor.name);
        databaseVendorLabel.setIcon(databaseVendor.icon);

        databaseTipsLabel.setName("databaseTipsLabel");
        databaseTipsLabel.setText(databaseVendor.tips);

        serverUrlField.setName("serverUrlField");

        authenticationContainer.setBorder(IdeBorderFactory.createTitledBorder("Authentication settings", true));
        userDatabaseField.setName("userDatabaseField");
        userDatabaseField.setToolTipText("If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here");

        autoConnectCheckBox.setName("autoConnectField");

        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));
        shellArgumentsLineField.setDialogCaption("Mongo arguments");

        testConnectionButton.setName("testConnection");

        shellWorkingDirField.setText(null);
        initListeners();
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                final Ref<Exception> excRef = new Ref<>();
                final ProgressManager progressManager = ProgressManager.getInstance();
                progressManager.runProcessWithProgressSynchronously(new Runnable() {
                    @Override
                    public void run() {
                        ServerConfiguration configuration = createServerConfigurationForTesting();

                        final ProgressIndicator progressIndicator = progressManager.getProgressIndicator();
                        if (progressIndicator != null) {
                            progressIndicator.setText("Connecting to " + configuration.getServerUrl());
                        }
                        try {
                            databaseClient.connect(configuration);
                        } catch (Exception ex) {
                            excRef.set(ex);
                        }
                    }

                }, "Testing connection for " + databaseVendor.name, true, ServerConfigurationPanel.this.project);

                if (!excRef.isNull()) {
                    Messages.showErrorDialog(rootPanel, excRef.get().getMessage(), "Connection test failed");
                } else {
                    Messages.showInfoMessage(rootPanel, "Connection test successful for " + databaseVendor.name, "Connection test successful");
                }
            }
        });
    }

    public void loadConfigurationData(ServerConfiguration configuration) {
        labelField.setText(configuration.getLabel());
        serverUrlField.setText(configuration.getServerUrl());
        userDatabaseField.setText(configuration.getUserDatabase());
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());

        authenticationView.load(configuration.getAuthenticationSettings());
    }

    @NotNull
    private ServerConfiguration createServerConfigurationForTesting() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setDatabaseVendor(databaseVendor);
        configuration.setServerUrl(getServerUrls());
        configuration.setAuthenticationSettings(authenticationView.create());
        configuration.setUserDatabase(getUserDatabase());
        return configuration;
    }

    public void applyConfigurationData(ServerConfiguration configuration) {

        configuration.setLabel(getLabel());
        configuration.setDatabaseVendor(databaseVendor);
        configuration.setServerUrl(getServerUrls());
        configuration.setAuthenticationSettings(authenticationView.create());

        configuration.setUserDatabase(getUserDatabase());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());
    }

    public ValidationInfo validateInputs() {
        if (StringUtils.isEmpty(getLabel())) {
            return new ValidationInfo("Label should be set");
        }
        String serverUrl = getServerUrls();
        if (serverUrl == null) {
            return new ValidationInfo("URL(s) should be set");
        }
        return null;
    }


    private void validateUrls() {
        String serverUrl = getServerUrls();
        if (serverUrl == null) {
            throw new ConfigurationException("URL(s) should be set");
        }
        for (String subServerUrl : serverUrl.split(",")) {
            String[] host_port = subServerUrl.split(":");
            if (host_port.length < 2) {
                throw new ConfigurationException(String.format("URL '%s' format is incorrect. It should be 'host:port'", subServerUrl));
            }

            try {
                Integer.valueOf(host_port[1].trim());
            } catch (NumberFormatException e) {
                throw new ConfigurationException(String.format("Port in the URL '%s' is incorrect. It should be a number", subServerUrl));
            }
        }

    }

    private String getLabel() {
        String label = labelField.getText();
        if (StringUtils.isNotBlank(label)) {
            return label;
        }
        return null;
    }

    private String getServerUrls() {
        String serverUrl = serverUrlField.getText();
        if (StringUtils.isNotBlank(serverUrl)) {
            return serverUrl;
        }
        return null;
    }

    private String getUserDatabase() {
        String userDatabase = userDatabaseField.getText();
        if (StringUtils.isNotBlank(userDatabase)) {
            return userDatabase;
        }
        return null;
    }


    private String getShellArgumentsLine() {
        String shellArgumentsLine = shellArgumentsLineField.getText();
        if (StringUtils.isNotBlank(shellArgumentsLine)) {
            return shellArgumentsLine;
        }

        return null;
    }

    private String getShellWorkingDir() {
        String shellWorkingDir = shellWorkingDirField.getText();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            return shellWorkingDir;
        }

        return null;
    }

    private boolean isAutoConnect() {
        return autoConnectCheckBox.isSelected();
    }

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderActionListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Shell working directory",
                        null,
                        shellWorkingDirField,
                        null,
                        fileChooserDescriptor,
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        shellWorkingDirField.addBrowseFolderListener(null, browseFolderActionListener, false);
        shellWorkingDirField.setName("shellWorkingDirField");
    }
}
