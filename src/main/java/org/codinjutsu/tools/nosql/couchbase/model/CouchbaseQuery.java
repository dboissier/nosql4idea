package org.codinjutsu.tools.nosql.couchbase.model;

public class CouchbaseQuery {

    private final int limit;

    public CouchbaseQuery(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}
