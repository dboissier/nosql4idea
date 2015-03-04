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

package org.codinjutsu.tools.nosql.database.mongo;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.DatabaseClient;
import org.codinjutsu.tools.nosql.database.DatabaseServer;
import org.codinjutsu.tools.nosql.database.NoSqlDatabase;
import org.codinjutsu.tools.nosql.database.mongo.model.*;
import org.codinjutsu.tools.nosql.logic.ConfigurationException;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class MongoManager implements DatabaseClient {

    private static final Logger LOG = Logger.getLogger(MongoManager.class);
    private List<DatabaseServer> databaseServers = new LinkedList<DatabaseServer>();

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public void connect(ServerConfiguration configuration) {

        MongoClient mongo = null;
        try {
            String userDatabase = configuration.getUserDatabase();
            mongo = createMongoClient(configuration);

            DB databaseForTesting;
            if (StringUtils.isNotEmpty(userDatabase)) {
                databaseForTesting = mongo.getDB(userDatabase);
            } else {
                databaseForTesting = mongo.getDB("test");
            }

            databaseForTesting.getLastError();

        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new ConfigurationException(ex.getMessage());
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public List<DatabaseServer> loadServers(List<ServerConfiguration> serverConfigurations, boolean loadOnStartup) {
        if (loadOnStartup) {
            databaseServers.clear();
        }

        if (!databaseServers.isEmpty()) {
            return databaseServers;
        }

        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            DatabaseServer databaseServer = new DatabaseServer(serverConfiguration);
            databaseServers.add(databaseServer);

            if (loadOnStartup && !databaseServer.getConfiguration().isConnectOnIdeStartup()) {
                continue;
            }
            loadServer(databaseServer);

        }
        return databaseServers;
    }

    public void loadServer(DatabaseServer databaseServer) {
        try {
            List<NoSqlDatabase> mongoDatabases = loadDatabaseCollections(databaseServer.getConfiguration());
            databaseServer.setDatabases(mongoDatabases);
            databaseServer.setStatus(DatabaseServer.Status.OK);
        } catch (ConfigurationException e) {
            databaseServer.setStatus(DatabaseServer.Status.ERROR);
        }
    }

    public List<NoSqlDatabase> loadDatabaseCollections(ServerConfiguration serverConfiguration) {
        MongoClient mongo = null;
        List<NoSqlDatabase> mongoDatabases = new LinkedList<NoSqlDatabase>();
        try {
            String userDatabase = serverConfiguration.getUserDatabase();

            mongo = createMongoClient(serverConfiguration);

            if (StringUtils.isNotEmpty(userDatabase) && serverConfiguration.isUserDatabaseAsMySingleDatabase()) {
                DB database = mongo.getDB(userDatabase);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = mongo.getDatabaseNames();
                Collections.sort(databaseNames);
                for (String databaseName : databaseNames) {
                    DB database = mongo.getDB(databaseName);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                }
            }

            return mongoDatabases;
        } catch (Exception ex) {
            LOG.error("Error when collecting Mongo databases", ex);
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoDatabase createMongoDatabaseAndItsCollections(DB database) {
        MongoDatabase mongoDatabase = new MongoDatabase(database.getName());


        Set<String> collectionNames = database.getCollectionNames();
        for (String collectionName : collectionNames) {
            mongoDatabase.addCollection(new MongoCollection(collectionName, database.getName()));
        }
        return mongoDatabase;
    }

    public void update(ServerConfiguration configuration, MongoCollection mongoCollection, DBObject mongoDocument) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.save(mongoDocument);
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void delete(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.remove(new BasicDBObject("_id", _id));
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void dropCollection(ServerConfiguration configuration, MongoCollection mongoCollection) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.drop();
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void dropDatabase(ServerConfiguration configuration, MongoDatabase selectedDatabase) {
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

    public MongoResult loadRecords(ServerConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            MongoResult mongoResult = new MongoResult(mongoCollection.getName());
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

    public DBObject findMongoDocument(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());
            return collection.findOne(new BasicDBObject("_id", _id));

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoResult aggregate(MongoQueryOptions mongoQueryOptions, MongoResult mongoResult, DBCollection collection) {
        AggregationOutput aggregate = collection.aggregate(mongoQueryOptions.getOperations());
        int index = 0;
        Iterator<DBObject> iterator = aggregate.results().iterator();
        while (iterator.hasNext() && index < mongoQueryOptions.getResultLimit()) {
            mongoResult.add(iterator.next());
        }
        return mongoResult;
    }

    private MongoResult find(MongoQueryOptions mongoQueryOptions, MongoResult mongoResult, DBCollection collection) {
        DBObject filter = mongoQueryOptions.getFilter();
        DBObject projection = mongoQueryOptions.getProjection();
        DBObject sort = mongoQueryOptions.getSort();

        DBCursor cursor;
        if (projection == null) {
            cursor = collection.find(filter);
        } else {
            cursor = collection.find(filter, projection);
        }

        if (sort != null) {
            cursor = cursor.sort(sort);
        }

        try {
            int index = 0;
            while (cursor.hasNext() && index < mongoQueryOptions.getResultLimit()) {
                mongoResult.add(cursor.next());
                index++;
            }
        } finally {
            cursor.close();
        }
        return mongoResult;
    }

    private MongoClient createMongoClient(ServerConfiguration configuration) throws UnknownHostException {
        List<String> serverUrls = configuration.getServerUrls();
        String username = configuration.getUsername();
        String password = configuration.getPassword();
        String userDatabase = configuration.getUserDatabase();
        if (serverUrls.isEmpty()) {
            throw new ConfigurationException("server host is not set");
        }

        List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
        for (String serverUrl : serverUrls) {
            String[] host_port = serverUrl.split(":");
            serverAddresses.add(new ServerAddress(host_port[0], Integer.valueOf(host_port[1])));
        }

        MongoClientOptions.Builder optionBuilder = MongoClientOptions.builder();
        if (configuration.isSslConnection()) {
            optionBuilder.socketFactory(SSLSocketFactory.getDefault());
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            MongoCredential credential = MongoCredential.createMongoCRCredential(username, userDatabase, password.toCharArray());
            return new MongoClient(serverAddresses, Arrays.asList(credential), optionBuilder.build());
        }
        return new MongoClient(serverAddresses, optionBuilder.build());
    }

    public static enum MongoQueryOperator {

        FILTER("Filter"),
        PROJECTION("Projection"),
        SORT("Sort");
        public final String label;

        MongoQueryOperator(String label) {
            this.label = label;
        }
    }
}
