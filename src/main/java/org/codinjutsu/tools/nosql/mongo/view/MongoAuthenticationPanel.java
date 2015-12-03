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

import com.mongodb.AuthenticationMechanism;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.view.AuthenticationView;
import org.codinjutsu.tools.nosql.mongo.logic.MongoExtraSettings;

import javax.swing.*;

public class MongoAuthenticationPanel implements AuthenticationView {
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField authenticationDatabaseField;
    private JRadioButton scramSHA1AuthRadioButton;
    private JRadioButton mongoCRAuthRadioButton;
    private JRadioButton defaultAuthMethodRadioButton;
    private JCheckBox sslConnectionField;


    public MongoAuthenticationPanel() {
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        authenticationDatabaseField.setName("authenticationDatabaseField");
        mongoCRAuthRadioButton.setName("mongoCRAuthField");
        scramSHA1AuthRadioButton.setName("scramSHA1AuthField");
        defaultAuthMethodRadioButton.setName("defaultAuthMethod");
        sslConnectionField.setName("sslConnectionField");

        ButtonGroup authMethodGroup = new ButtonGroup();
        authMethodGroup.add(mongoCRAuthRadioButton);
        authMethodGroup.add(scramSHA1AuthRadioButton);
        authMethodGroup.add(defaultAuthMethodRadioButton);

        defaultAuthMethodRadioButton.setSelected(true);
        defaultAuthMethodRadioButton.setToolTipText("Let the driver resolves the auth. mecanism");
    }

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public AuthenticationSettings create() {
        AuthenticationSettings authenticationSettings = new AuthenticationSettings();
        authenticationSettings.setUsername(getUsername());
        authenticationSettings.setPassword(getPassword());

        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings();
        mongoExtraSettings.setSsl(isSslConnection());
        mongoExtraSettings.setAuthenticationDatabase(getAuthenticationDatabase());
        mongoExtraSettings.setAuthenticationMechanism(getAuthenticationMechanism());
        authenticationSettings.setExtras(mongoExtraSettings.get());

        return authenticationSettings;
    }

    @Override
    public void load(AuthenticationSettings settings) {
        usernameField.setText(settings.getUsername());
        passwordField.setText(settings.getPassword());
        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings(settings.getExtras());
        authenticationDatabaseField.setText(mongoExtraSettings.getAuthenticationDatabase());
        sslConnectionField.setSelected(mongoExtraSettings.isSsl());
        AuthenticationMechanism authentificationMethod = mongoExtraSettings.getAuthenticationMechanism();
        if (AuthenticationMechanism.MONGODB_CR.equals(authentificationMethod)) {
            mongoCRAuthRadioButton.setSelected(true);
        } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authentificationMethod)) {
            scramSHA1AuthRadioButton.setSelected(true);
        } else {
            defaultAuthMethodRadioButton.setSelected(true);
        }
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

    private AuthenticationMechanism getAuthenticationMechanism() {
        if (mongoCRAuthRadioButton.isSelected()) {
            return AuthenticationMechanism.MONGODB_CR;
        } else if (scramSHA1AuthRadioButton.isSelected()) {
            return AuthenticationMechanism.SCRAM_SHA_1;
        }
        return null;
    }

}
