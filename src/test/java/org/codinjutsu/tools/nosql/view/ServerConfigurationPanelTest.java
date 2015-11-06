/*
 * Copyright (c) 2013 David Boissier
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

package org.codinjutsu.tools.nosql.view;

import com.intellij.openapi.command.impl.DummyProject;
import com.mongodb.AuthenticationMechanism;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.DatabaseVendorManager;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.ServerConfigurationPanel;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private DatabaseVendorManager databaseVendorManager;

    private FrameFixture frameFixture;

    @Before
    public void setUp() throws Exception {
        databaseVendorManager = Mockito.spy(new DatabaseVendorManager(DummyProject.getInstance()));
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(DummyProject.getInstance(), databaseVendorManager);
            }
        });

        frameFixture = Containers.showInFrame(configurationPanel);
    }

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Test
    public void validateFormWithOneServerUrl() throws Exception {
        frameFixture.textBox("labelField").setText("Localhost");

        JComboBoxFixture databaseVendorField = frameFixture.comboBox("databaseVendorField");
        assertArrayEquals(new String[]{
                "DatabaseVendor{name='MongoDB'}",
                "DatabaseVendor{name='RedisDB'}",
                "DatabaseVendor{name='Couchbase'}"
        }, databaseVendorField.contents());

        databaseVendorField.requireSelection("DatabaseVendor{name='MongoDB'}");

        databaseVendorField.selectItem("DatabaseVendor{name='RedisDB'}");

        frameFixture.label("databaseTipsLabel").requireText("format: host:port. If cluster: host:port1,host:port2,...");

        frameFixture.textBox("serverUrlField").setText("localhost:25");
        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");

        frameFixture.textBox("userDatabaseField").setText("mydatabase");
        frameFixture.checkBox("sslConnectionField").check();
        frameFixture.checkBox("autoConnectField").check();

        frameFixture.radioButton("defaultAuthMethod").requireSelected();
        frameFixture.radioButton("mongoCRAuthField").click();

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("Localhost", configuration.getLabel());
        assertEquals(DatabaseVendor.REDIS, configuration.getDatabaseVendor());
        assertEquals("localhost:25", configuration.getServerUrl());
        assertEquals("john", configuration.getUsername());
        assertEquals("mydatabase", configuration.getUserDatabase());
        assertTrue(configuration.isSslConnection());
        assertTrue(configuration.isConnectOnIdeStartup());
        assertEquals(AuthenticationMechanism.MONGODB_CR, configuration.getAuthenticationMecanism());
    }

    @Test
    public void loadFormWithOneServerUrl() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setLabel("Localhost");
        configuration.setDatabaseVendor(DatabaseVendor.COUCHBASE);
        configuration.setServerUrl("localhost:25");
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("labelField").requireText("Localhost");
        frameFixture.comboBox("databaseVendorField").requireSelection("DatabaseVendor{name='Couchbase'}");
        frameFixture.label("databaseTipsLabel").requireText("format: host:port. If cluster: host:port1,host:port2,...");
        frameFixture.textBox("serverUrlField").requireText("localhost:25");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");
    }

    @Test
    public void validateFormWithEmptyLabelShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Label should be set");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithMissingMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL(s) should be set");

        frameFixture.textBox("labelField").setText("Localhost");
        frameFixture.textBox("serverUrlField").setText(null);

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithEmptyMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL(s) should be set");
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.textBox("serverUrlField").setText("");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithBadMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL 'host' format is incorrect. It should be 'host:port'");
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.textBox("serverUrlField").setText("host");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }


    @Test
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

        configuration.setServerUrl("localhost:25, localhost:26");

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlField").requireText("localhost:25, localhost:26");
    }
}
