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
