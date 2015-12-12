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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@State(
        name = "NoSqlConfiguration",
        storages = {
                @Storage(file = "$PROJECT_FILE$"),
                @Storage(file = "$PROJECT_CONFIG_DIR$/noSqlSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class NoSqlConfiguration implements PersistentStateComponent<NoSqlConfiguration> {

    private List<ServerConfiguration> serverConfigurations = new LinkedList<ServerConfiguration>();
    private Map<DatabaseVendor, String> shellPathByDatabaseVendor = new HashMap<DatabaseVendor, String>();

    public static NoSqlConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, NoSqlConfiguration.class);
    }


    public NoSqlConfiguration getState() {
        return this;
    }

    public void loadState(NoSqlConfiguration noSqlConfiguration) {
        XmlSerializerUtil.copyBean(noSqlConfiguration, this);
    }

    public void setServerConfigurations(List<ServerConfiguration> serverConfigurations) {
        this.serverConfigurations = serverConfigurations;
    }

    public List<ServerConfiguration> getServerConfigurations() {
        return serverConfigurations;
    }

    public String getShellPath(DatabaseVendor databaseVendor) {
        return shellPathByDatabaseVendor.get(databaseVendor);
    }

    public void setShellPathByDatabaseVendor(Map<DatabaseVendor, String> shellPathByDatabaseVendor) {
        this.shellPathByDatabaseVendor = shellPathByDatabaseVendor;
    }

    public Map<DatabaseVendor, String> getShellPathByDatabaseVendor() {
        return shellPathByDatabaseVendor;
    }

    public void setShellPath(DatabaseVendor databaseVendor, String shellPath) {
        shellPathByDatabaseVendor.put(databaseVendor, shellPath);
    }
}
