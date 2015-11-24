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

package org.codinjutsu.tools.nosql.couchbase.view;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.AuthenticationView;

import javax.swing.*;
import java.awt.*;

public class CouchbaseAuthenticationPanel extends JPanel implements AuthenticationView {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel mainPanel;

    public CouchbaseAuthenticationPanel() {
        setLayout(new BorderLayout());
        add(mainPanel);

        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
    }

    @Override
    public void loadConfiguration(ServerConfiguration configuration) {
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
    }

    @Override
    public void applyConfiguration(ServerConfiguration configuration) {
        configuration.setUserDatabase(getUsername());
        configuration.setPassword(getPassword());
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
}
