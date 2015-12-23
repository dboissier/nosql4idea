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

package org.codinjutsu.tools.nosql.mongo.logic;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoCollection;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.model.MongoQueryOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class SingleMongoClientTest {

    private SingleMongoClient singleMongoClient;
    private ServerConfiguration serverConfiguration;


    @Test
    public void loadCollectionsWithEmptyFilter() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setResultLimit(3);
        MongoResult mongoResult = singleMongoClient.loadCollectionValues(serverConfiguration, new SingleMongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoResult);
        assertEquals(3, mongoResult.getMongoObjects().size());
    }

    @Test
    public void loadCollectionsWithFilterAndProjection() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
        mongoQueryOptions.setProjection("{\"label\":1, \"_id\": 0}");
        mongoQueryOptions.setResultLimit(3);
        MongoResult mongoResult = singleMongoClient.loadCollectionValues(serverConfiguration, new SingleMongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoResult);
        assertEquals(2, mongoResult.getMongoObjects().size());
        assertEquals("[Document{{label=tata}}, Document{{label=tata}}]", mongoResult.getMongoObjects().toString());
    }

    @Test
    public void loadCollectionsWithFilterAndProjectionAndSortByPrice() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
        mongoQueryOptions.setProjection("{\"label\": 1, \"_id\": 0, \"price\": 1}");
        mongoQueryOptions.setSort("{\"price\": 1}");
        mongoQueryOptions.setResultLimit(3);
        MongoResult mongoResult = singleMongoClient.loadCollectionValues(serverConfiguration, new SingleMongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoResult);
        assertEquals(2, mongoResult.getMongoObjects().size());
        assertEquals("[Document{{label=tata, price=10}}, Document{{label=tata, price=15}}]", mongoResult.getMongoObjects().toString());
    }

    @Test
    public void updateMongoDocument() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'label': 'tete'}");
        SingleMongoCollection singleMongoCollection = new SingleMongoCollection("dummyCollection", "test");
        MongoResult initialData = singleMongoClient.loadCollectionValues(serverConfiguration, singleMongoCollection, mongoQueryOptions);
        assertEquals(1, initialData.getMongoObjects().size());
        Document initialMongoDocument = initialData.getMongoObjects().get(0);

        initialMongoDocument.put("price", 25);
        singleMongoClient.update(serverConfiguration, singleMongoCollection, initialMongoDocument);

        MongoResult updatedResult = singleMongoClient.loadCollectionValues(serverConfiguration, singleMongoCollection, mongoQueryOptions);
        List<Document> updatedMongoDocuments = updatedResult.getMongoObjects();
        assertEquals(1, updatedMongoDocuments.size());
        Document updatedMongoDocument = updatedMongoDocuments.get(0);

        assertEquals(25, updatedMongoDocument.get("price"));
    }


    @Test
    public void deleteMongoDocument() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'label': 'tete'}");
        SingleMongoCollection singleMongoCollection = new SingleMongoCollection("dummyCollection", "test");
        MongoResult initialData = singleMongoClient.loadCollectionValues(serverConfiguration, singleMongoCollection, mongoQueryOptions);
        assertEquals(1, initialData.getMongoObjects().size());
        Document initialMongoDocument = initialData.getMongoObjects().get(0);

        singleMongoClient.delete(serverConfiguration, singleMongoCollection, initialMongoDocument.get("_id"));

        MongoResult deleteResult = singleMongoClient.loadCollectionValues(serverConfiguration, singleMongoCollection, mongoQueryOptions);
        List<Document> updatedMongoDocuments = deleteResult.getMongoObjects();
        assertEquals(0, updatedMongoDocuments.size());
    }


    @Test
    public void loadCollectionsWithAggregateOperators() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setOperations("[{'$match': {'price': 15}}, {'$project': {'label': 1, 'price': 1}}, {'$group': {'_id': '$label', 'total': {'$sum': '$price'}}}]");
        MongoResult mongoResult = singleMongoClient.loadCollectionValues(serverConfiguration, new SingleMongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoResult);

        List<Document> mongoObjects = mongoResult.getMongoObjects();

        assertEquals(2, mongoObjects.size());
        assertEquals("Document{{_id=tutu, total=15}}", mongoObjects.get(0).toString());
        assertEquals("Document{{_id=tata, total=15}}", mongoObjects.get(1).toString());
    }

    @Before
    public void setUp() throws Exception {
        com.mongodb.MongoClient mongo = new com.mongodb.MongoClient("localhost:27017");
        MongoDatabase db = mongo.getDatabase("test");

        com.mongodb.client.MongoCollection<Document> dummyCollection = db.getCollection("dummyCollection");
        dummyCollection.deleteMany(new BasicDBObject());
        fillCollectionWithJsonData(dummyCollection, IOUtils.toString(getClass().getResourceAsStream("dummyCollection.json")));

        singleMongoClient = new SingleMongoClient();
        serverConfiguration = singleMongoClient.defaultConfiguration();
        serverConfiguration.setServerUrl("localhost:27017");
    }

    private static void fillCollectionWithJsonData(com.mongodb.client.MongoCollection<Document> collection, String jsonResource) throws IOException {
        Object jsonParsed = JSON.parse(jsonResource);
        if (jsonParsed instanceof BasicDBList) {
            BasicDBList jsonObject = (BasicDBList) jsonParsed;
            for (Object o : jsonObject) {
                DBObject dbObject = (DBObject) o;
                Document document = new Document();
                for (String key : dbObject.keySet()) {
                    document.append(key, dbObject.get(key));
                }
                collection.insertOne(document);
            }
        } else {
            DBObject dbObject = (DBObject) jsonParsed;
            Document document = new Document();
            for (String key : dbObject.keySet()) {
                document.append(key, dbObject.get(key));
            }
            collection.insertOne(document);
        }
    }
}

