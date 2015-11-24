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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import org.codinjutsu.tools.nosql.mongo.logic.MongoConnectionException;
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

    private JCheckBox sslConnectionField;

    private JTextField labelField;
    private JComboBox databaseVendorField;

    private JPanel authenticationPanel;
    private JTextField serverUrlField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField authenticationDatabaseField;
    private final ButtonGroup authMethodGroup;
    private JRadioButton mongoCRAuthRadioButton;
    private JRadioButton scramSHA1AuthRadioButton;

    private JRadioButton defaultAuthMethodRadioButton;

    private JTextField userDatabaseField;

    private JCheckBox autoConnectCheckBox;

    private JButton testConnectionButton;
    private JTextField collectionsToIgnoreField;
    private JPanel mongoShellOptionsPanel;
    private TextFieldWithBrowseButton shellWorkingDirField;
    private RawCommandLineEditor shellArgumentsLineField;
    private JLabel databaseTipsLabel;

    private final Project project;
    private final DatabaseVendorManager databaseVendorManager;


    public ServerConfigurationPanel(Project project, DatabaseVendorManager databaseVendorManager) {
        this.project = project;
        this.databaseVendorManager = databaseVendorManager;
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        labelField.setName("labelField");
        databaseVendorField.setName("databaseVendorField");
        databaseTipsLabel.setName("databaseTipsLabel");
        feedbackLabel.setName("feedbackLabel");

        sslConnectionField.setName("sslConnectionField");
        authenticationPanel.setBorder(IdeBorderFactory.createTitledBorder("Authentication settings", true));
        serverUrlField.setName("serverUrlField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        mongoCRAuthRadioButton.setName("mongoCRAuthField");
        scramSHA1AuthRadioButton.setName("scramSHA1AuthField");
        defaultAuthMethodRadioButton.setName("defaultAuthMethod");

        userDatabaseField.setName("userDatabaseField");
        userDatabaseField.setToolTipText("If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here");

        autoConnectCheckBox.setName("autoConnectField");

        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));
        shellArgumentsLineField.setDialogCaption("Mongo arguments");

        testConnectionButton.setName("testConnection");

        authMethodGroup = new ButtonGroup();
        authMethodGroup.add(mongoCRAuthRadioButton);
        authMethodGroup.add(scramSHA1AuthRadioButton);
        authMethodGroup.add(defaultAuthMethodRadioButton);

        defaultAuthMethodRadioButton.setSelected(true);
        defaultAuthMethodRadioButton.setToolTipText("Let the driver resolves the auth. mecanism");
        shellWorkingDirField.setText(null);
        initListeners();
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
            }
        });

        databaseVendorField.setSelectedItem(DatabaseVendor.MONGO);
    }

    @NotNull
    private ServerConfiguration createServerConfigurationForTesting() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setDatabaseVendor(getDatabaseVendor());
        configuration.setServerUrl(getServerUrls());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setAuthenticationDatabase(getAuthenticationDatabase());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setAuthenticationMecanism(getAuthenticationMethod());
        configuration.setSslConnection(isSslConnection());
        return configuration;
    }

    public void applyConfigurationData(ServerConfiguration configuration) {
        validateLabel();
        validateUrls();

        configuration.setLabel(getLabel());
        configuration.setDatabaseVendor(getDatabaseVendor());
        configuration.setServerUrl(getServerUrls());
        configuration.setSslConnection(isSslConnection());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setAuthenticationDatabase(getAuthenticationDatabase());
        configuration.setCollectionsToIgnore(getCollectionsToIgnore());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());
        configuration.setAuthenticationMecanism(getAuthenticationMethod());
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
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
        userDatabaseField.setText(configuration.getUserDatabase());
        authenticationDatabaseField.setText(configuration.getAuthenticationDatabase());
        sslConnectionField.setSelected(configuration.isSslConnection());
        collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());

        AuthenticationMechanism authentificationMethod = configuration.getAuthenticationMecanism();
        if (AuthenticationMechanism.MONGODB_CR.equals(authentificationMethod)) {
            mongoCRAuthRadioButton.setSelected(true);
        } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authentificationMethod)) {
            scramSHA1AuthRadioButton.setSelected(true);
        } else {
            defaultAuthMethodRadioButton.setSelected(true);
        }
    }


    private List<String> getCollectionsToIgnore() {
        String collectionsToIgnoreText = collectionsToIgnoreField.getText();
        if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
            String[] collectionsToIgnore = collectionsToIgnoreText.split(",");

            List<String> collections = new LinkedList<String>();
            for (String collectionToIgnore : collectionsToIgnore) {
                collections.add(StringUtils.trim(collectionToIgnore));
            }
            return collections;
        }
        return Collections.emptyList();
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

    private boolean isSslConnection() {
        return sslConnectionField.isSelected();
    }

    private String getUsername() {
        String username = usernameField.getText();
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return null;
    }

    private String getPassword() {
        char[] password = passwordField.getPassword();
        if (password != null && password.length != 0) {
            return String.valueOf(password);
        }
        return null;
    }

    private String getAuthenticationDatabase() {
        String authenticationDatabase = authenticationDatabaseField.getText();
        if (StringUtils.isNotBlank(authenticationDatabase)) {
            return authenticationDatabase;
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

    private AuthenticationMechanism getAuthenticationMethod() {
        if (mongoCRAuthRadioButton.isSelected()) {
            return AuthenticationMechanism.MONGODB_CR;
        } else if (scramSHA1AuthRadioButton.isSelected()) {
            return AuthenticationMechanism.SCRAM_SHA_1;
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
                new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Mongo shell working directory",
                        null,
                        shellWorkingDirField,
                        null,
                        fileChooserDescriptor,
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        shellWorkingDirField.addBrowseFolderListener(null, browseFolderActionListener, false);
        shellWorkingDirField.setName("shellWorkingDirField");
    }

    public void setErrorMessage(String message) {
        feedbackLabel.setText(message);
    }
}
