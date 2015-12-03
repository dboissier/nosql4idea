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
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.nosql.commons.DatabaseUI;
import org.codinjutsu.tools.nosql.couchbase.CouchbaseUI;
import org.codinjutsu.tools.nosql.couchbase.view.editor.CouchbaseObjectFile;
import org.codinjutsu.tools.nosql.mongo.MongoUI;
import org.codinjutsu.tools.nosql.mongo.view.editor.MongoObjectFile;
import org.codinjutsu.tools.nosql.redis.RedisUI;
import org.codinjutsu.tools.nosql.redis.view.editor.RedisObjectFile;

import java.util.HashMap;
import java.util.Map;

public class DatabaseVendorUIManager {

    private static final Map<DatabaseVendor, Class<? extends DatabaseUI>> databaseUIByVendor = new HashMap<>();

    static {
        databaseUIByVendor.put(DatabaseVendor.MONGO, MongoUI.class);
        databaseUIByVendor.put(DatabaseVendor.REDIS, RedisUI.class);
        databaseUIByVendor.put(DatabaseVendor.COUCHBASE, CouchbaseUI.class);
    }

    private final Project project;

    public DatabaseVendorUIManager(Project project) {
        this.project = project;
    }

    public static DatabaseVendorUIManager getInstance(Project project) {
        return ServiceManager.getService(project, DatabaseVendorUIManager.class);
    }

    public DatabaseUI get(DatabaseVendor databaseVendor) {
        return ServiceManager.getService(project, databaseUIByVendor.get(databaseVendor));
    }

    public boolean accept(VirtualFile file) {
        return file instanceof MongoObjectFile || file instanceof RedisObjectFile || file instanceof CouchbaseObjectFile;

    }
}