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

package org.codinjutsu.tools.nosql.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseClient;
import org.junit.Test;

import static com.couchbase.client.java.query.Select.*;
import static com.couchbase.client.java.query.dsl.Expression.*;

public class CouchbaseClientTest {
    @Test
    public void loadServers() throws Exception {
        CouchbaseClient couchbaseClient = new CouchbaseClient();
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerUrl("localhost");
        configuration.setUsername("Administrator");
        configuration.setPassword("helloworld");
        couchbaseClient.loadServer(new DatabaseServer(configuration));
    }

    @Test
    public void testName() throws Exception {


    }

    public static void main(String[] args) {
        Cluster cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment
                .builder()
                .queryEnabled(true)
                .build());

        Bucket defaultBucket = cluster.openBucket("default");
        defaultBucket.remove("user:walter");

        JsonArray friends = JsonArray.empty()
                .add(JsonObject.empty().put("name", "Mike Ehrmantraut"))
                .add(JsonObject.empty().put("name", "Jesse Pinkman"));

        JsonObject content = JsonObject.empty()
                .put("firstname", "Walter")
                .put("lastname", "White")
                .put("age", 52)
                .put("aliases", JsonArray.from("Walt Jackson", "Mr. Mayhew", "David Lynn"))
                .put("friends", friends);
        JsonDocument walter = JsonDocument.create("user:walter", content);
        JsonDocument inserted = defaultBucket.insert(walter);

        JsonDocument foundGuy = defaultBucket.get("user:walter");
        System.out.println(foundGuy.content().toMap());


        Bucket beerBucket = cluster.openBucket("beer-sample");
        N1qlQueryResult result = beerBucket.query(N1qlQuery.simple(select("*").from(i("beer-sample")).limit(10)));

        System.out.println("Errors found: " + result.errors());

        for (N1qlQueryRow row : result.allRows()) {
            JsonObject jsonObject = row.value();
            System.out.println(jsonObject.toMap());
        }

        cluster.disconnect();
    }
}
