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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import com.mongodb.AuthenticationMechanism;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.view.AuthenticationView;
import org.codinjutsu.tools.nosql.couchbase.view.CouchbaseAuthenticationPanel;
import org.codinjutsu.tools.nosql.mongo.view.MongoAuthenticationPanel;
import org.codinjutsu.tools.nosql.redis.view.RedisAuthenticationPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerConfigurationPanel extends JPanel {

    public static final Icon FAIL = AllIcons.General.Error;
    private JPanel rootPanel;

    private JLabel feedbackLabel;

    private JTextField labelField;

    private JComboBox databaseVendorField;
    private JTextField serverUrlField;
    private JTextField userDatabaseField;
    private JCheckBox autoConnectCheckBox;
    private JButton testConnectionButton;
    private JPanel mongoShellOptionsPanel;
    private TextFieldWithBrowseButton shellWorkingDirField;

    private RawCommandLineEditor shellArgumentsLineField;
    private JLabel databaseTipsLabel;
    private final Project project;

    private final DatabaseVendorManager databaseVendorManager;

    private JPanel authenticationParentPanel;
    private AuthenticationView currentAuthenticationView = null;


    public ServerConfigurationPanel(Project project, DatabaseVendorManager databaseVendorManager) {
        this.project = project;
        this.databaseVendorManager = databaseVendorManager;

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Shell options", true));

        labelField.setName("labelField");
        databaseVendorField.setName("databaseVendorField");
        databaseTipsLabel.setName("databaseTipsLabel");
        feedbackLabel.setName("feedbackLabel");
        serverUrlField.setName("serverUrlField");
        authenticationParentPanel.setBorder(IdeBorderFactory.createTitledBorder("Authentication settings", true));
        userDatabaseField.setName("userDatabaseField");
        userDatabaseField.setToolTipText("If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here");
        autoConnectCheckBox.setName("autoConnectField");
        shellArgumentsLineField.setDialogCaption("CLI arguments");
        testConnectionButton.setName("testConnection");

        shellWorkingDirField.setText(null);

        registerDatabaseAuthentication();
        initListeners();
    }

    private void registerDatabaseAuthentication() {
        authenticationParentPanel.add(new MongoAuthenticationPanel(), DatabaseVendor.MONGO.name);
        authenticationParentPanel.add(new RedisAuthenticationPanel(), DatabaseVendor.REDIS.name);
        authenticationParentPanel.add(new CouchbaseAuthenticationPanel(), DatabaseVendor.COUCHBASE.name);
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    validateUrls();
                    final DatabaseVendor databaseVendor = getDatabaseVendor();
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
                                databaseVendorManager.get(databaseVendor).connect(configuration);
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
                } catch (ConfigurationException ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });

        databaseVendorField.setModel(new DefaultComboBoxModel(DatabaseVendor.values()));
        databaseVendorField.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                DatabaseVendor databaseVendor = (DatabaseVendor) value;
                setIcon(databaseVendor.icon);
                append(databaseVendor.name);
            }
        });

        databaseVendorField.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                DatabaseVendor selected = (DatabaseVendor) databaseVendorField.getSelectedItem();
                if (selected == null) {
                    return;
                }
                serverUrlField.setText(selected.defaultUrl);
                databaseTipsLabel.setText(selected.tips);
                CardLayout cardLayout = (CardLayout)(authenticationParentPanel.getLayout());
                cardLayout.show(authenticationParentPanel, selected.name);
            }
        });

        databaseVendorField.setSelectedItem(DatabaseVendor.MONGO);
    }

    @NotNull
    private ServerConfiguration createServerConfigurationForTesting() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setDatabaseVendor(getDatabaseVendor());
        configuration.setServerUrl(getServerUrls());
        //TODO
        return configuration;
    }

    public void applyConfigurationData(ServerConfiguration configuration) {
        validateLabel();
        validateUrls();

        configuration.setLabel(getLabel());
        configuration.setDatabaseVendor(getDatabaseVendor());
        configuration.setServerUrl(getServerUrls());

        configuration.setUserDatabase(getUserDatabase());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());


    }

    private void validateLabel() {
        if (StringUtils.isEmpty(getLabel())) {
            throw new ConfigurationException("Label should be set");
        }
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


    public void loadConfigurationData(ServerConfiguration configuration) {
        labelField.setText(configuration.getLabel());
        databaseVendorField.setSelectedItem(configuration.getDatabaseVendor());
        serverUrlField.setText(configuration.getServerUrl());
        userDatabaseField.setText(configuration.getUserDatabase());
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());

        //TODO
    }

    private DatabaseVendor getDatabaseVendor() {
        return (DatabaseVendor) databaseVendorField.getSelectedItem();
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

    public void setErrorMessage(String message) {
        feedbackLabel.setIcon(AllIcons.General.Error);
        feedbackLabel.setText(message);
    }
}
