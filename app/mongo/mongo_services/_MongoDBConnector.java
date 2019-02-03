package mongo.mongo_services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
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
import java.util.Set;

@Singleton
public class _MongoDBConnector {

    private static final Logger logger = new Logger(_MongoDBConnector.class);

    private Config config;
    private String mode;
    private final String url;

    private MongoClient mongoClient;
    @Inject
    public _MongoDBConnector(Config config, ServerLogger serverLogger) {

        // SET Values
        this.mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        this.config = config;
        this.url = config.getString("MongoDB." + mode + ".url"); // Cluster


        // CONNECT TO MONGO CLUSTER
        MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
        options_builder.maxConnectionIdleTime(1000 * 60 * 60 * 24);
        this.mongoClient = new MongoClient(new MongoClientURI(this.url, options_builder));


        // TRY TO CONNECT
        try {
            this.mongoClient.getAddress();
        } catch (Exception e) {
            logger.error("constructor - Mongo is down");
            this.mongoClient.close();
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

                String value = annotation.value();
                if(value.equals(".")) {
                    value = model.getSimpleName();
                }

                for (Field field : model.getFields()) {
                    if (field.isAnnotationPresent(InjectStore.class) && field.get(null) instanceof CacheMongoFinder) {

                        if(!model.getCanonicalName().contains("mongo") && !model.getSimpleName().contains("ModelMongo")) {
                            logger.error("constructor - {} Collection:: {}  - Class must start with ModelMongo prefix ", model.getSimpleName(), value);
                            return;
                        }

                        if (!this.mongoClient.getDatabase(this.getDatabaseName(model)).listCollectionNames().into(new ArrayList<>()).contains(value)) {
                            logger.error("constructor - {} {} Collection:: {}  - not exist. System will create that! ", model.getSimpleName(),  model.getCanonicalName(), value);
                            this.mongoClient.getDatabase(this.getDatabaseName(model)).createCollection(value);
                        }

                        // Check Database
                        // Kontrola databáze
                        if (!this.mongoClient.listDatabaseNames().into(new ArrayList<>()).contains(this.getDatabaseName(model))) {
                            logger.error("constructor - Required Main Database not Exist!");
                            return;
                        }

                        // Mongo ORM zástupný onbjekt pro lepší práci s databází
                        Datastore datastore = new Morphia().createDatastore(this.mongoClient, this.getDatabaseName(model));
                        ((CacheMongoFinder)field.get(null)).setDatastore(datastore);
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
     * Get Database name
     * @param model Class for parsing
     * @return String - name of database
     */
    private String getDatabaseName(Class<? extends _Abstract_MongoModel> model) {
        try {
            _MongoCollectionConfig collectionConfig = model.getAnnotation(_MongoCollectionConfig.class);

            if(collectionConfig.database_name().equals("")) throw new NullPointerException("Default Database");
            return collectionConfig.database_name();

        } catch (NullPointerException e) {
            return  this.config.getString("MongoDB." + this.mode + ".main_database_name");
        }
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }
}
