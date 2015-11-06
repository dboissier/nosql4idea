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

package org.codinjutsu.tools.nosql.commons.model;

import org.codinjutsu.tools.nosql.ServerConfiguration;

import java.util.LinkedList;
import java.util.List;

public class DatabaseServer {

    public enum Status {
        OK, LOADING, ERROR
    }

    private List<Database> databases = new LinkedList<Database>();

    private final ServerConfiguration configuration;

    private Status status = Status.OK;

    public DatabaseServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getServerUrl() {
        return configuration.getServerUrl();
    }

    public String getLabel() {
        return configuration.getLabel();
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public boolean hasDatabases() {
        return !databases.isEmpty();
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
