package org.codinjutsu.tools.nosql.mongo.logic;

public class MongoConnectionException extends RuntimeException {
    public MongoConnectionException(String message) {
        super(message);
    }

    public MongoConnectionException(Exception ex) {
        super(ex);
    }
}
