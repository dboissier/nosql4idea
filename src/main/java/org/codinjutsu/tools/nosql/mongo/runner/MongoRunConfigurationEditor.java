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

package org.codinjutsu.tools.nosql.mongo.runner;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.mongo.logic.SingleMongoClient;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoDatabase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

public class MongoRunConfigurationEditor extends SettingsEditor<MongoRunConfiguration> {

    private JPanel mainPanel;

    private JTextField scriptPathField;
    private ComboBox serverConfigurationCombobox;
    private ComboBox databaseCombobox;
    private JPanel mongoShellOptionsPanel;
    private RawCommandLineEditor shellParametersField;
    private TextFieldWithBrowseButton shellWorkingDirField;


    public MongoRunConfigurationEditor(Project project) {
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));

        shellParametersField.setDialogCaption("Mongo arguments");

        DatabaseServer[] mongoServers = getAvailableMongoServers(project);

        if (mongoServers.length == 0) {
            serverConfigurationCombobox.setEnabled(false);
            databaseCombobox.setEnabled(false);
            return;
        }

        serverConfigurationCombobox.setModel(new DefaultComboBoxModel(mongoServers));

        serverConfigurationCombobox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                DatabaseServer serverConfiguration = (DatabaseServer) value;
                append(serverConfiguration.getLabel());
            }
        });


        databaseCombobox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                SingleMongoDatabase singleMongoDatabase = (SingleMongoDatabase) value;
                if (value == null) {
                    return;
                }
                append(singleMongoDatabase.getName());
            }
        });


        serverConfigurationCombobox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                DatabaseServer selectedServer = (DatabaseServer) serverConfigurationCombobox.getSelectedItem();
                if (selectedServer == null) {
                    return;
                }
                databaseCombobox.removeAllItems();
                for (Database mongoDatabase : selectedServer.getDatabases()) {
                    databaseCombobox.addItem(mongoDatabase);
                }
            }
        });

        serverConfigurationCombobox.setSelectedIndex(-1);
        serverConfigurationCombobox.setSelectedIndex(0);
    }

    private DatabaseServer[] getAvailableMongoServers(Project project) {
        List<DatabaseServer> mongoServers = SingleMongoClient.getInstance(project).getServers();
        List<DatabaseServer> availableMongoServers = new LinkedList<>();
        for (DatabaseServer mongoServer : mongoServers) {
            if (mongoServer.hasDatabases()) {
                availableMongoServers.add(mongoServer);
            }
        }
        return availableMongoServers.toArray(new DatabaseServer[availableMongoServers.size()]);
    }

    @Override
    protected void resetEditorFrom(MongoRunConfiguration configuration) {
        scriptPathField.setText(configuration.getScriptPath() != null ? configuration.getScriptPath().getPath() : null);
        shellParametersField.setText(configuration.getShellParameters());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
    }

    @Override
    protected void applyEditorTo(MongoRunConfiguration configuration) throws ConfigurationException {
        configuration.setScriptPath(getScriptPath());
        configuration.setServerConfiguration(getSelectedConfiguration());
        configuration.setDatabase(getSelectedDatabase());
        configuration.setShellParameters(getShellParameters());
        configuration.setShellWorkingDir(getShellWorkingDir());
    }

    private String getScriptPath() {
        return scriptPathField.getText();
    }

    private String getShellParameters() {
        return shellParametersField.getText();
    }

    private ServerConfiguration getSelectedConfiguration() {
        DatabaseServer selectedServer = (DatabaseServer) serverConfigurationCombobox.getSelectedItem();
        return selectedServer == null ? null : selectedServer.getConfiguration();
    }

    public SingleMongoDatabase getSelectedDatabase() {
        return (SingleMongoDatabase) databaseCombobox.getSelectedItem();
    }

    private String getShellWorkingDir() {
        String shellWorkingDir = shellWorkingDirField.getText();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            return shellWorkingDir;
        }

        return null;
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    @Override
    protected void disposeEditor() {

    }

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        shellWorkingDirField.addBrowseFolderListener("Mongo shell working directory", "", null,
                new FileChooserDescriptor(false, true, false, false, false, false));
        shellWorkingDirField.setName("shellWorkingDirField");
    }
}
