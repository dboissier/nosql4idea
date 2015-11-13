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

package org.codinjutsu.tools.nosql.couchbase.logic;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseQuery;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseResult;

import java.util.LinkedList;
import java.util.List;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;

public class CouchbaseClient implements DatabaseClient {

    private static final Logger LOG = Logger.getLogger(CouchbaseClient.class);

    public static CouchbaseClient getInstance(Project project) {
        return ServiceManager.getService(project, CouchbaseClient.class);
    }

    @Override
    public void connect(ServerConfiguration serverConfiguration) {
        CouchbaseCluster cluster = CouchbaseCluster.create(serverConfiguration.getServerUrl());
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
        Cluster cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment
                .builder()
                .queryEnabled(true)
                .build());
        ClusterManager clusterManager = cluster.clusterManager(databaseServer.getConfiguration().getUsername(), databaseServer.getConfiguration().getPassword());

        List<Database> couchbaseDatabases = new LinkedList<>();
        String userBucket = databaseServer.getConfiguration().getUserDatabase();
        if (StringUtils.isNotBlank(userBucket)) {
            BucketSettings bucketSettings = clusterManager.getBucket(userBucket);
            couchbaseDatabases.add(new CouchbaseDatabase(bucketSettings.name()));
        } else {
            List<BucketSettings> buckets = clusterManager.getBuckets();

            for (BucketSettings bucketSettings : buckets) {
                CouchbaseDatabase database = new CouchbaseDatabase(bucketSettings.name());
                couchbaseDatabases.add(database);
            }
        }

        databaseServer.setDatabases(couchbaseDatabases);

        cluster.disconnect();
    }

    @Override
    public void cleanUpServers() {

    }

    @Override
    public void registerServer(DatabaseServer databaseServer) {

    }

    public CouchbaseResult loadRecords(ServerConfiguration configuration, CouchbaseDatabase database, CouchbaseQuery couchbaseQuery) {
        Cluster cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment
                .builder()
                .queryEnabled(true)
                .build());

        Bucket beerBucket = cluster.openBucket(database.getName());
        N1qlQueryResult queryResult = beerBucket.query(N1qlQuery.simple(select("*").from(i(database.getName())).limit(couchbaseQuery.getLimit())));


        CouchbaseResult result = new CouchbaseResult(database.getName());
        List<JsonObject> errors = queryResult.errors();
        if (!errors.isEmpty()) {
            result.addErrors(errors);
            return result;
        }

        for (N1qlQueryRow row : queryResult.allRows()) {
            result.add(row.value());
        }

        cluster.disconnect();

        return result;
    }
}
