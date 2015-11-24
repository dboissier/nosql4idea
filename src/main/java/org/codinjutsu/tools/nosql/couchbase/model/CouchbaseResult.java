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

package org.codinjutsu.tools.nosql.couchbase.model;

import com.couchbase.client.java.document.json.JsonObject;

import java.util.LinkedList;
import java.util.List;

public class CouchbaseResult {
    private final List<JsonObject> jsonObjects = new LinkedList<>();
    private final List<JsonObject> errors = new LinkedList<>();
    private final String name;

    public CouchbaseResult(String name) {
        this.name = name;
    }

    public void add(JsonObject jsonObject) {
        this.jsonObjects.add(jsonObject);
    }

    public void addErrors(List<JsonObject> errors) {
        this.errors.addAll(errors);
    }

    public String getName() {
        return null;
    }

    public List<JsonObject> getRecords() {
        return jsonObjects;
    }
}
