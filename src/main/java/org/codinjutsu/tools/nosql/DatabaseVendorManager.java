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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseManager;
import org.codinjutsu.tools.nosql.mongo.logic.MongoManager;
import org.codinjutsu.tools.nosql.redis.logic.RedisManager;

import java.util.HashMap;
import java.util.Map;

public class DatabaseVendorManager {

    private static final Map<DatabaseVendor, Class<? extends DatabaseClient>> dataClientByVendor = new HashMap<DatabaseVendor, Class<? extends DatabaseClient>>();

    static {
        dataClientByVendor.put(DatabaseVendor.MONGO, MongoManager.class);
        dataClientByVendor.put(DatabaseVendor.REDIS, RedisManager.class);
//        dataClientByVendor.put(DatabaseVendor.COUCHBASE, CouchbaseManager.class);
    }

    private final Project project;

    public DatabaseVendorManager(Project project) {
        this.project = project;
    }

    public static DatabaseVendorManager getInstance(Project project) {
        return ServiceManager.getService(project, DatabaseVendorManager.class);
    }

    public DatabaseClient get(DatabaseVendor databaseVendor) {
        return ServiceManager.getService(project, dataClientByVendor.get(databaseVendor));
    }

    public void cleanUpServers() {
        for (DatabaseVendor databaseVendor: dataClientByVendor.keySet()) {
            this.get(databaseVendor).cleanUpServers();
        }
    }

    public void registerServer(DatabaseServer databaseServer) {
        this.get(databaseServer.getConfiguration().getDatabaseVendor()).registerServer(databaseServer);
    }

    public void loadServer(DatabaseServer databaseServer) {
        this.get(databaseServer.getConfiguration().getDatabaseVendor()).loadServer(databaseServer);
    }
}
