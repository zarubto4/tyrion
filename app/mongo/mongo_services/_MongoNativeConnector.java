package mongo.mongo_services;


import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;

import java.util.HashMap;

import utilities.enums.*;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;

import com.typesafe.config.Config;
import com.google.inject.Inject;


@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class _MongoNativeConnector {

    private static final Logger logger = new Logger(_MongoNativeConnector.class);

    private Config config;
    private String mode;
    private static String url;

    private static MongoClient mongoClient;
    private static HashMap<String, MongoDatabase> databases = new HashMap<>();


    @Inject
    public _MongoNativeConnector(Config config, ServerLogger serverLogger) {

        // SET Values
        this.mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        this.config = config;
        this.url = config.getString("MongoDB." + mode + ".url"); // Cluster

        mongoClient = MongoClients.create(url);
        this.getDatabase(config.getString("MongoDB." + mode + ".main_database_name"));

    }

    public MongoDatabase getDatabase(String database_name) {

        if(!databases.containsKey(database_name)) {
            MongoDatabase database = mongoClient.getDatabase(database_name);
            databases.put(database_name, database);
        }

        return databases.get(database_name);
    }

    public _MongoNativeCollection getDatabaseCollection(String database_name, String collection_name) {

        MongoDatabase database = getDatabase(database_name);
        return  new _MongoNativeCollection(database, collection_name);
    }

}
