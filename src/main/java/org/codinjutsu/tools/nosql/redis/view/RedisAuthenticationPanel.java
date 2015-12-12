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

package org.codinjutsu.tools.nosql.redis.view;

import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.view.AuthenticationView;

import javax.swing.*;

public class RedisAuthenticationPanel implements AuthenticationView {

    private JPasswordField passwordField;
    private JPanel mainPanel;

    public RedisAuthenticationPanel() {
        passwordField.setName("passwordField");
    }

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public AuthenticationSettings create() {
        AuthenticationSettings authenticationSettings = new AuthenticationSettings();
        authenticationSettings.setPassword(getPassword());
        return authenticationSettings;
    }

    @Override
    public void load(AuthenticationSettings settings) {
        passwordField.setText(settings.getPassword());
    }

    private String getPassword() {
        char[] password = passwordField.getPassword();
        if (password != null && password.length != 0) {
            return String.valueOf(password);
        }
        return null;
    }
}
