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

package org.codinjutsu.tools.nosql.database;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.mongo.MongoManager;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.database.redis.RedisManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatabaseVendorManager {

    private static final Map<DatabaseVendor, Class<? extends DatabaseClient>> dataClientByVendor = new HashMap<DatabaseVendor, Class<? extends DatabaseClient>>();

    private List<DatabaseServer> databaseServers = new LinkedList<DatabaseServer>();

    static {
        dataClientByVendor.put(DatabaseVendor.MONGO, MongoManager.class);
        dataClientByVendor.put(DatabaseVendor.REDIS, RedisManager.class);
    }

    public static DatabaseVendorManager getInstance(Project project) {
        return ServiceManager.getService(project, DatabaseVendorManager.class);
    }

    public DatabaseClient get(Project project, DatabaseVendor databaseVendor) {
        return ServiceManager.getService(project, dataClientByVendor.get(databaseVendor));
    }

    public List<DatabaseServer> loadServers(Project project, List<ServerConfiguration> serverConfigurations, boolean loadOnStartup) {
        if (loadOnStartup) {
            databaseServers.clear();
        }

        if (!databaseServers.isEmpty()) {
            return databaseServers;
        }

        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            DatabaseServer databaseServer = new DatabaseServer(serverConfiguration);
            databaseServers.add(databaseServer);

            ServerConfiguration configuration = databaseServer.getConfiguration();
            if (loadOnStartup && !configuration.isConnectOnIdeStartup()) {
                continue;
            }
            this.get(project, configuration.getDatabaseVendor()).loadServer(databaseServer);

        }
        return databaseServers;
    }
}
