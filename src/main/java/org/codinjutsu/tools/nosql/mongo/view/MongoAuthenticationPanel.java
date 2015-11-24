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

package org.codinjutsu.tools.nosql.mongo.view;

import com.intellij.ui.IdeBorderFactory;
import com.mongodb.AuthenticationMechanism;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.AuthenticationView;

import javax.swing.*;
import java.awt.*;

public class MongoAuthenticationPanel extends JPanel implements AuthenticationView {
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField authenticationDatabaseField;
    private JRadioButton scramSHA1AuthRadioButton;
    private JRadioButton mongoCRAuthRadioButton;
    private JRadioButton defaultAuthMethodRadioButton;
    private JCheckBox sslConnectionField;

    private final ButtonGroup authMethodGroup;

    public MongoAuthenticationPanel() {
        setLayout(new BorderLayout());
        mainPanel.setBorder(IdeBorderFactory.createTitledBorder("Authentication settings", true));
        add(mainPanel);

        authMethodGroup = new ButtonGroup();
        authMethodGroup.add(mongoCRAuthRadioButton);
        authMethodGroup.add(scramSHA1AuthRadioButton);
        authMethodGroup.add(defaultAuthMethodRadioButton);

        sslConnectionField.setName("sslConnectionField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        mongoCRAuthRadioButton.setName("mongoCRAuthField");
        scramSHA1AuthRadioButton.setName("scramSHA1AuthField");
        defaultAuthMethodRadioButton.setName("defaultAuthMethod");
        authenticationDatabaseField.setName("authenticationDatabaseField");

        defaultAuthMethodRadioButton.setSelected(true);
        defaultAuthMethodRadioButton.setToolTipText("Let the driver resolves the auth. mecanism");

    }

    @Override
    public void loadConfiguration(ServerConfiguration configuration) {
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());

        authenticationDatabaseField.setText(configuration.getAuthenticationDatabase());
        sslConnectionField.setSelected(configuration.isSslConnection());

        AuthenticationMechanism authentificationMethod = configuration.getAuthenticationMecanism();
        if (AuthenticationMechanism.MONGODB_CR.equals(authentificationMethod)) {
            mongoCRAuthRadioButton.setSelected(true);
        } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authentificationMethod)) {
            scramSHA1AuthRadioButton.setSelected(true);
        } else {
            defaultAuthMethodRadioButton.setSelected(true);
        }
    }

    @Override
    public void applyConfiguration(ServerConfiguration configuration) {
        configuration.setSslConnection(isSslConnection());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setAuthenticationDatabase(getAuthenticationDatabase());
        configuration.setAuthenticationMecanism(getAuthenticationMethod());
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


    private AuthenticationMechanism getAuthenticationMethod() {
        if (mongoCRAuthRadioButton.isSelected()) {
            return AuthenticationMechanism.MONGODB_CR;
        } else if (scramSHA1AuthRadioButton.isSelected()) {
            return AuthenticationMechanism.SCRAM_SHA_1;
        }
        return null;
    }

    private boolean isSslConnection() {
        return sslConnectionField.isSelected();
    }

}
