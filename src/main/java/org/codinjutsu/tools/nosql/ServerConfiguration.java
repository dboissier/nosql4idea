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

import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;

public class ServerConfiguration implements Cloneable {

    private String label;

    private DatabaseVendor databaseVendor = DatabaseVendor.MONGO;

    private String serverUrl;

    private String userDatabase;

    private boolean connectOnIdeStartup = false;

    private String shellArgumentsLine;
    private String shellWorkingDir;

    private AuthenticationSettings authenticationSettings = new AuthenticationSettings();

    public DatabaseVendor getDatabaseVendor() {
        return databaseVendor;
    }

    public void setDatabaseVendor(DatabaseVendor databaseVendor) {
        this.databaseVendor = databaseVendor;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setUserDatabase(String userDatabase) {
        this.userDatabase = userDatabase;
    }

    public String getUserDatabase() {
        return userDatabase;
    }

    public boolean isConnectOnIdeStartup() {
        return connectOnIdeStartup;
    }

    public void setConnectOnIdeStartup(boolean connectOnIdeStartup) {
        this.connectOnIdeStartup = connectOnIdeStartup;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShellArgumentsLine() {
        return shellArgumentsLine;
    }

    public void setShellArgumentsLine(String shellArgumentsLine) {
        this.shellArgumentsLine = shellArgumentsLine;
    }

    public String getShellWorkingDir() {
        return shellWorkingDir;
    }

    public void setShellWorkingDir(String shellWorkingDir) {
        this.shellWorkingDir = shellWorkingDir;
    }

    public boolean isSingleServer() {
        return serverUrl.split(",").length == 1;
    }

    public void setAuthenticationSettings(AuthenticationSettings authenticationSettings) {
        this.authenticationSettings = authenticationSettings;
    }


    public AuthenticationSettings getAuthenticationSettings() {
        return authenticationSettings;
    }

    public ServerConfiguration clone() {
        try {
            return (ServerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfiguration that = (ServerConfiguration) o;

        if (connectOnIdeStartup != that.connectOnIdeStartup) return false;
        if (!label.equals(that.label)) return false;
        if (databaseVendor != that.databaseVendor) return false;
        if (!serverUrl.equals(that.serverUrl)) return false;
        if (userDatabase != null ? !userDatabase.equals(that.userDatabase) : that.userDatabase != null) return false;
        if (shellArgumentsLine != null ? !shellArgumentsLine.equals(that.shellArgumentsLine) : that.shellArgumentsLine != null)
            return false;
        if (shellWorkingDir != null ? !shellWorkingDir.equals(that.shellWorkingDir) : that.shellWorkingDir != null)
            return false;
        return !(authenticationSettings != null ? !authenticationSettings.equals(that.authenticationSettings) : that.authenticationSettings != null);

    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + databaseVendor.hashCode();
        result = 31 * result + serverUrl.hashCode();
        result = 31 * result + (userDatabase != null ? userDatabase.hashCode() : 0);
        result = 31 * result + (connectOnIdeStartup ? 1 : 0);
        result = 31 * result + (shellArgumentsLine != null ? shellArgumentsLine.hashCode() : 0);
        result = 31 * result + (shellWorkingDir != null ? shellWorkingDir.hashCode() : 0);
        result = 31 * result + (authenticationSettings != null ? authenticationSettings.hashCode() : 0);
        return result;
    }
}
