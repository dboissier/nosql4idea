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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoCollection;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoDatabase;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SingleMongoClient implements DatabaseClient {

    private static final Logger LOG = Logger.getLogger(SingleMongoClient.class);
    private final List<DatabaseServer> databaseServers = new LinkedList<>();

    public static SingleMongoClient getInstance(Project project) {
        return ServiceManager.getService(project, SingleMongoClient.class);
    }

    public void connect(ServerConfiguration configuration) {
        MongoClient mongo = null;
        try {
            String userDatabase = configuration.getUserDatabase();
            mongo = createMongoClient(configuration);

            MongoIterable<String> collectionNames;
            if (StringUtils.isNotEmpty(userDatabase)) {
                collectionNames = mongo.getDatabase(userDatabase).listCollectionNames();
            } else {
                collectionNames = mongo.getDatabase("test").listCollectionNames();
            }
            collectionNames.first();

        } catch (IOException ex) {
            throw new MongoConnectionException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new MongoConnectionException(ex.getMessage());
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void cleanUpServers() {
        databaseServers.clear();
    }

    public void registerServer(DatabaseServer databaseServer) {
        databaseServers.add(databaseServer);
    }

    @Override
    public ServerConfiguration defaultConfiguration() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDatabaseVendor(DatabaseVendor.MONGO);
        serverConfiguration.setServerUrl(DatabaseVendor.MONGO.defaultUrl);
        return serverConfiguration;
    }

    public List<DatabaseServer> getServers() {
        return databaseServers;
    }

    public void loadServer(DatabaseServer databaseServer) {
        databaseServer.setStatus(DatabaseServer.Status.LOADING);
        List<Database> mongoDatabases = loadDatabaseCollections(databaseServer.getConfiguration());
        databaseServer.setDatabases(mongoDatabases);
        databaseServer.setStatus(DatabaseServer.Status.OK);
    }

    List<Database> loadDatabaseCollections(ServerConfiguration configuration) {
        MongoClient mongo = null;
        List<Database> mongoDatabases = new LinkedList<>();
        try {
            String userDatabase = configuration.getUserDatabase();

            mongo = createMongoClient(configuration);

            if (StringUtils.isNotEmpty(userDatabase)) {
                MongoDatabase database = mongo.getDatabase(userDatabase);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
            } else {
                MongoIterable<String> databaseNames = mongo.listDatabaseNames();
                for (String databaseName : databaseNames) {
                    MongoDatabase database = mongo.getDatabase(databaseName);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                }

                Collections.sort(mongoDatabases, new Comparator<Database>() {
                    @Override
                    public int compare(Database o1, Database o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }

            return mongoDatabases;
        } catch (MongoException | UnknownHostException mongoEx) {
            throw new ConfigurationException(mongoEx);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private SingleMongoDatabase createMongoDatabaseAndItsCollections(MongoDatabase database) {
        SingleMongoDatabase singleMongoDatabase = new SingleMongoDatabase(database.getName());


        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (String collectionName : collectionNames) {
            singleMongoDatabase.addCollection(new SingleMongoCollection(collectionName, database.getName()));
        }
        return singleMongoDatabase;
    }

    public void update(ServerConfiguration configuration, SingleMongoCollection singleMongoCollection, Document mongoDocument) {
        MongoClient mongo = null;
        try {
            String databaseName = singleMongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            MongoDatabase database = mongo.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(singleMongoCollection.getName());

            final Object id = mongoDocument.get("_id");
            if (id == null) {
                collection.insertOne(mongoDocument);
            } else {
                collection.replaceOne(Filters.eq("_id", id), mongoDocument);
            }
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void delete(ServerConfiguration configuration, SingleMongoCollection singleMongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = singleMongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            MongoDatabase database = mongo.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(singleMongoCollection.getName());
            collection.deleteOne(Filters.eq("_id", _id));
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void dropCollection(ServerConfiguration configuration, SingleMongoCollection singleMongoCollection) {
        MongoClient mongo = null;
        try {
            String databaseName = singleMongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            mongo.getDatabase(databaseName)
                    .getCollection(singleMongoCollection.getName())
                    .drop();

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void dropDatabase(ServerConfiguration configuration, SingleMongoDatabase selectedDatabase) {
        MongoClient mongo = null;
        try {
            mongo = createMongoClient(configuration);
            mongo.dropDatabase(selectedDatabase.getName());
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public MongoResult loadCollectionValues(ServerConfiguration configuration, SingleMongoCollection singleMongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoClient mongo = null;
        try {
            String databaseName = singleMongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            MongoDatabase database = mongo.getDatabase(databaseName);

            MongoCollection<Document> collection = database.getCollection(singleMongoCollection.getName());

            MongoResult mongoResult = new MongoResult(singleMongoCollection.getName());
            if (mongoQueryOptions.isAggregate()) {
                return aggregate(mongoQueryOptions, mongoResult, collection);
            }

            return find(mongoQueryOptions, mongoResult, collection);

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public Document findMongoDocument(ServerConfiguration configuration, SingleMongoCollection singleMongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = singleMongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            MongoDatabase database = mongo.getDatabase(databaseName);
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(singleMongoCollection.getName());
            return collection.find(Filters.eq("_id", _id)).first();
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoResult aggregate(MongoQueryOptions mongoQueryOptions, MongoResult mongoResult, MongoCollection<Document> collection) {
        AggregateIterable<Document> aggregate = collection.aggregate(mongoQueryOptions.getOperations());
        aggregate.useCursor(true);

        int index = 0;
        MongoCursor<Document> iterator = aggregate.iterator();
        while (iterator.hasNext() && index < mongoQueryOptions.getResultLimit()) {
            mongoResult.add(iterator.next());
        }
        return mongoResult;
    }

    private MongoResult find(MongoQueryOptions mongoQueryOptions, final MongoResult mongoResult, MongoCollection<Document> collection) {
        Bson filter = mongoQueryOptions.getFilter();
        Bson projection = mongoQueryOptions.getProjection();
        Bson sort = mongoQueryOptions.getSort();

        FindIterable<Document> findIterable;
        if (projection == null) {
            findIterable = collection.find(filter);
        } else {
            findIterable = collection.find(filter).projection(projection);
        }

        if (sort != null) {
            findIterable = findIterable.sort(sort);
        }

        findIterable.limit(mongoQueryOptions.getResultLimit());

        findIterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                mongoResult.add(document);
            }
        });

        return mongoResult;
    }

    private MongoClient createMongoClient(ServerConfiguration configuration) throws UnknownHostException {
        String serverUrl = configuration.getServerUrl();
        if (StringUtils.isEmpty(serverUrl)) {
            throw new ConfigurationException("server host is not set");
        }

        MongoClientURIBuilder uriBuilder = MongoClientURIBuilder.builder();
        uriBuilder.setServerAddresses(serverUrl);
        AuthenticationSettings authenticationSettings = configuration.getAuthenticationSettings();
        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings(authenticationSettings.getExtras());
        if (StringUtils.isNotEmpty(authenticationSettings.getUsername())) {
            uriBuilder.setCredential(authenticationSettings.getUsername(), authenticationSettings.getPassword(), mongoExtraSettings.getAuthenticationDatabase());
        }


        if (mongoExtraSettings.getAuthenticationMechanism() != null) {
            uriBuilder.setAuthenticationMecanism(mongoExtraSettings.getAuthenticationMechanism());
        }

        if (mongoExtraSettings.isSsl()) {
            uriBuilder.sslEnabled();
        }

        return new MongoClient(new MongoClientURI(uriBuilder.build()));
    }

}
