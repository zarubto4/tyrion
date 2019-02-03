package mongo.mongo_services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.cache.CacheMongoFinder;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;
import utilities.model._Abstract_MongoModel;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;
import xyz.morphia.annotations.Entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Singleton
public class _MongoDBConnector {

    private static final Logger logger = new Logger(_MongoDBConnector.class);

    private Config config;
    private String mode;

    private final String url; // Main Database URL
    private final String name; // Main Database Name;

    private HashMap<String, MongoClient> databases = new HashMap<>(); // < URL < Name , DB > >


    @Inject
    public _MongoDBConnector(Config config, ServerLogger serverLogger) {

        this.config = config;

        // SET Values
        this.mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        this.name = config.getString("MongoDB." + mode + ".main_database_name");
        this.url = config.getString("MongoDB." + mode + ".url"); // Cluster


        // CONNECT TO MONGO CLUSTER
        MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
        options_builder.maxConnectionIdleTime(1000 * 60 * 60 * 24);
        MongoClient mongoClient = new MongoClient(new MongoClientURI(this.url, options_builder));

        this.databases.put(url, mongoClient);

        // TRY TO CONNECT
        try {
            this.databases.get(url).getAddress();
        } catch (Exception e) {
            logger.error("constructor - Mongo is down");
            this.databases.get(url).close();
            return;
        }

        // Kontrola kolekcí nad Mongo Databází
        this.parseClass();

    }

    @SuppressWarnings("unchecked")
    private void parseClass(){
        getClasses().forEach(cls -> {
            try {

                Class<? extends _Abstract_MongoModel> model = (Class<? extends _Abstract_MongoModel>) cls; // Cast to model
                Entity annotation = model.getAnnotation(Entity.class);


                String collection_name = getProperName(annotation, model);
                ConnectionConfig connection_config = getConnectionConfig(model);

               // System.out.println("CONNECTION URL: " + connection_config.database_url);
               //  System.out.println("Colllection name: " + collection_name);
               // System.out.println("Datatabe name: " + connection_config.database_name);


                if(!databases.containsKey(connection_config.database_url)) {
                    System.out.println("MEW TO HASH");
                    MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
                    options_builder.maxConnectionIdleTime(1000 * 60 * 60 * 24);
                    MongoClient mongoClient = new MongoClient(new MongoClientURI(connection_config.database_url, options_builder));
                    databases.put(connection_config.database_url,mongoClient);
                }


                if (!this.databases.get(this.url).getDatabase(this.getConnectionConfig(model).database_name).listCollectionNames().into(new ArrayList<>()).contains(collection_name)) {
                    logger.error("constructor - {} {} Collection:: {}  - not exist. System will create that! ", model.getSimpleName(),  model.getCanonicalName(), collection_name);
                    this.databases.get(this.url).getDatabase(this.getConnectionConfig(model).database_name).createCollection(collection_name);
                }



                for (Field field : model.getFields()) {
                    if (field.isAnnotationPresent(InjectStore.class) && field.get(null) instanceof CacheMongoFinder) {

                        if(!model.getCanonicalName().contains("mongo") && !model.getSimpleName().contains("ModelMongo")) {
                            logger.error("constructor - {} Collection:: {}  - Class must start with ModelMongo prefix ", model.getSimpleName(), collection_name);
                            return;
                        }

                        // Mongo ORM zástupný onbjekt pro lepší práci s databází
                        this.setFiledForCacheMongoFinder(field, connection_config.database_name);
                    }
                }

            } catch (Exception e) {
                logger.error("constructor - {} shit happens! ", cls.getSimpleName());
                logger.error("constructor - {} shit happens! ", cls.getSimpleName());
                logger.error("constructor - {} shit happens! ", cls.getSimpleName());
                logger.internalServerError(e);
            }
        });


    }


    private void setFiledForCacheMongoFinder(Field field, String database_name) throws IllegalAccessException {
        Datastore datastore = new Morphia().createDatastore(this.databases.get(this.url), database_name);
        ((CacheMongoFinder)field.get(null)).setDatastore(datastore);
    }


    /**
     * Get All Classes with Mongo Anotation
     * @return Set<Class<?>> list of clases
     */
    private Set<Class<?>> getClasses() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("mongo"))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);
        return classes;
    }

    /**
     * Get proper name of Class
     * @param annotation
     * @return
     */
    private String getProperName( Entity annotation,  Class<? extends _Abstract_MongoModel> model ) {
        String value = annotation.value();
        if(value.equals(".")) {
            value = model.getSimpleName();
        }

        return value;
    }

    /**
     * Get Database name
     * @param model Class for parsing
     * @return ConnectionConfig - name of database
     */
    private ConnectionConfig getConnectionConfig(Class<? extends _Abstract_MongoModel> model) {

        _MongoCollectionConfig collectionConfig = model.getAnnotation(_MongoCollectionConfig.class);

        ConnectionConfig connection = new ConnectionConfig();

        // If database Name is missing - set Defailt Database
        if(collectionConfig == null) {
            //System.out.println("getConnectionConfig:: _MongoCollectionConfig anotation is missing Return default");
            connection.database_url  = this.config.getString("MongoDB." + mode + ".url");
            connection.database_name = this.config.getString("MongoDB." + mode + ".main_database_name");
            return connection;
        }


        // If DatabaseName is not missing, but URL is missing, set defailt URL but diferent Database
        if(collectionConfig.database_name().length() > 0 && collectionConfig.database_url().equals("")) {

           // System.out.println("getConnectionConfig:: _MongoCollectionConfig Database Name" + collectionConfig.database_name() + " url is null");

            connection.database_url  = this.config.getString("MongoDB." + mode + ".url");
            connection.database_name = collectionConfig.database_name();
            return connection;
        }


        // If DatabaseName is not missing, but URL is missing, set defailt URL but diferent Database
        if(collectionConfig.database_name().length() > 0 && collectionConfig.database_url().length() > 0) {

           // System.out.println("getConnectionConfig:: _MongoCollectionConfig Database Name" + collectionConfig.database_name() + " there is a url:: " + collectionConfig.database_url());

            connection.database_url  = collectionConfig.database_name();
            connection.database_name = collectionConfig.database_url();
            return connection;
        }

        throw new IllegalStateException("_MongoDBConnector getDatabaseName Error, unsupported configuration");

    }

    private class ConnectionConfig {
        public String database_name;
        public String database_url;
    }

    public MongoClient getMainMongoClient() {

        return this.databases.get(this.url);
    }

    /**
     * Return Main Database for Tyrion
     * @return
     */
    public MongoDatabase getDatabase() {
        return this.databases.get(this.url).getDatabase(this.name);
    }

    /**
     * Return  Database by name in Tyrion Main Connection URL
     * @return
     */
    public MongoDatabase getDatabase(String name) {
        return this.databases.get(this.url).getDatabase(name);
    }

    /**
     * Return Database on Specific connetion URL
     * @param name
     * @param url
     * @return
     */
    public MongoDatabase getDatabase(String url, String name) {
        return this.databases.get(url).getDatabase(name);
    }
}
