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

package org.codinjutsu.tools.nosql.mongo.model;

import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public class MongoResult {

    private final String collectionName;
    private final List<Document> mongoObjects = new LinkedList<>();

    public MongoResult(String collectionName) {
        this.collectionName = collectionName;
    }

    public void add(Document dbObject) {
        mongoObjects.add(dbObject);
    }

    public List<Document> getMongoObjects() {
        return mongoObjects;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
