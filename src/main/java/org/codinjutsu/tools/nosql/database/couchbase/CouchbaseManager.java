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

package org.codinjutsu.tools.nosql.database.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.DatabaseClient;
import org.codinjutsu.tools.nosql.database.DatabaseServer;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.logic.ConfigurationException;

import java.util.List;

public class CouchbaseManager implements DatabaseClient {

    private static final Logger LOG = Logger.getLogger(CouchbaseManager.class);

    public static CouchbaseManager getInstance(Project project) {
        return ServiceManager.getService(project, CouchbaseManager.class);
    }

    @Override
    public void connect(ServerConfiguration serverConfiguration) {
        CouchbaseCluster cluster = CouchbaseCluster.create(serverConfiguration.getServerUrls());
        String userDatabase = serverConfiguration.getUserDatabase();
        Bucket bucket = null;
        try {
            if (StringUtils.isEmpty(userDatabase)) {
                bucket = cluster.openBucket();
            } else {
                bucket = cluster.openBucket(userDatabase);
            }

        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (bucket != null) {
                bucket.close();
            }
            cluster.disconnect();
        }

    }

    @Override
    public void loadServer(DatabaseServer databaseServer) {
        CouchbaseCluster cluster = CouchbaseCluster.create(databaseServer.getServerUrls());
        ClusterManager clusterManager = cluster.clusterManager(databaseServer.getConfiguration().getUsername(), databaseServer.getConfiguration().getPassword());
        List<BucketSettings> buckets = clusterManager.getBuckets();
        for (BucketSettings bucket : buckets) {
            System.out.println("bucket = " + bucket.name());
        }

        cluster.disconnect();
    }

    @Override
    public void dropCollection(ServerConfiguration configuration, MongoCollection selectedCollection) {

    }

    @Override
    public void dropDatabase(ServerConfiguration configuration, MongoDatabase selectedDatabase) {

    }
}
