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

import com.intellij.openapi.command.impl.DummyProject;
import com.mongodb.AuthenticationMechanism;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.view.ServerConfigurationPanel;
import org.codinjutsu.tools.nosql.mongo.logic.MongoExtraSettings;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private DatabaseClient databaseClientMock;

    private FrameFixture frameFixture;

    @Before
    public void setUp() throws Exception {
        databaseClientMock = Mockito.mock(DatabaseClient.class);
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(DummyProject.getInstance(),
                        DatabaseVendor.MONGO,
                        databaseClientMock,
                        new MongoAuthenticationPanel()
                );
            }
        });

        frameFixture = Containers.showInFrame(configurationPanel);
    }

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Test
    public void createMongoConfiguration() throws Exception {
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.label("databaseVendorLabel").requireText("MongoDB");
        frameFixture.label("databaseTipsLabel").requireText("format: host:port. If replicat set: host:port1,host:port2,...");

        frameFixture.textBox("serverUrlField").setText("localhost:25");
        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");

        frameFixture.textBox("userDatabaseField").setText("mydatabase");

        frameFixture.textBox("authenticationDatabaseField").setText("admin");
        frameFixture.radioButton("defaultAuthMethod").requireSelected();
        frameFixture.radioButton("mongoCRAuthField").click();

        frameFixture.checkBox("sslConnectionField").check();
        frameFixture.checkBox("autoConnectField").check();
        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("Localhost", configuration.getLabel());
        assertEquals(DatabaseVendor.MONGO, configuration.getDatabaseVendor());
        assertEquals("localhost:25", configuration.getServerUrl());
        AuthenticationSettings authenticationSettings = configuration.getAuthenticationSettings();
        assertEquals("john", authenticationSettings.getUsername());
        assertEquals("johnpassword", authenticationSettings.getPassword());

        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings(authenticationSettings.getExtras());

        assertEquals("admin", mongoExtraSettings.getAuthenticationDatabase());
        assertEquals(AuthenticationMechanism.MONGODB_CR, mongoExtraSettings.getAuthenticationMechanism());
        assertEquals("mydatabase", configuration.getUserDatabase());
        assertTrue(configuration.isConnectOnIdeStartup());
    }

    @Test
    public void loadMongoConfiguration() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setLabel("Localhost");
        configuration.setDatabaseVendor(DatabaseVendor.MONGO);
        configuration.setServerUrl("localhost:25");

        AuthenticationSettings authenticationSettings = new AuthenticationSettings();
        authenticationSettings.setUsername("john");
        authenticationSettings.setPassword("johnpassword");
        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings();
        mongoExtraSettings.setAuthenticationDatabase("admin");
        mongoExtraSettings.setAuthenticationMechanism(AuthenticationMechanism.SCRAM_SHA_1);
        mongoExtraSettings.setSsl(true);

        authenticationSettings.setExtras(mongoExtraSettings.get());

        configuration.setAuthenticationSettings(authenticationSettings);
        configuration.setUserDatabase("mydatabase");

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("labelField").requireText("Localhost");
        frameFixture.textBox("serverUrlField").requireText("localhost:25");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");
        frameFixture.textBox("authenticationDatabaseField").requireText("admin");
        frameFixture.checkBox("sslConnectionField").requireSelected();
        frameFixture.radioButton("scramSHA1AuthField").requireSelected();
    }

    @Test
    public void validateFormWithEmptyLabelShouldReturnAValidationInfo() {
        assertEquals("Label should be set", configurationPanel.validateInputs().message);
    }

    @Test
    public void validateFormWithMissingMongoUrlShouldThrowAConfigurationException() {
        frameFixture.textBox("labelField").setText("Localhost");
        frameFixture.textBox("serverUrlField").setText(null);

        assertEquals("URL(s) should be set", configurationPanel.validateInputs().message);
    }

    @Test
    public void validateFormWithEmptyMongoUrlShouldReturnAValidationInfo() {
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.textBox("serverUrlField").setText("");

        assertEquals("URL(s) should be set", configurationPanel.validateInputs().message);
    }

    @Test
    @Ignore
    public void validateFormWithBadMongoUrlShouldReturnAValidationInfo() {
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.textBox("serverUrlField").setText("host");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    @Ignore
    public void validateFormWithBadMongoPortShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Port in the URL 'host:port' is incorrect. It should be a number");
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.textBox("serverUrlField").setText("host:port");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithReplicatSet() throws Exception {
        frameFixture.textBox("labelField").setText("Localhost");
        frameFixture.textBox("serverUrlField").setText("localhost:25, localhost:26");

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("localhost:25, localhost:26", configuration.getServerUrl());
    }

    @Test
    public void loadFormWithReplicatSet() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setAuthenticationSettings(new AuthenticationSettings());

        configuration.setServerUrl("localhost:25, localhost:26");

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlField").requireText("localhost:25, localhost:26");
    }
}
