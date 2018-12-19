package mongo;

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
import java.util.Set;

@Singleton
public class MongoDBConnector {

    private static final Logger logger = new Logger(MongoDBConnector.class);

    private final String url;
    private final String name;

    private MongoClient mongoClient;

    private Morphia morphia;
    private Datastore datastore;
    private MongoDatabase database;

    @Inject
    @SuppressWarnings("unchecked")
    public MongoDBConnector(Config config, ServerLogger serverLogger) {

        String mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();

        this.url = config.getString("MongoDB." + mode + ".url");
        this.name = config.getString("MongoDB." + mode + ".main_database_name");

        MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
        options_builder.maxConnectionIdleTime(1000 * 60 * 60 *24);
        // options_builder.retryWrites(true);

        this.mongoClient = new MongoClient(new MongoClientURI(this.url, options_builder));

        try {

            this.mongoClient.getAddress();

        } catch (Exception e) {
            logger.error("constructor - Mongo is down");
            this.mongoClient.close();
            return;
        }

        this.morphia = new Morphia();

        // Mongo ORM zástupný onbjekt pro lepší práci s databází
        this.datastore = this.morphia.createDatastore(this.mongoClient, this.name);

        // Připojení na konkrétní Databázi clienta
        this.database = this.mongoClient.getDatabase(this.name);


        // Kontrola databáze
        if (!this.mongoClient.listDatabaseNames().into(new ArrayList<>()).contains(this.name)){
            logger.error("constructor - Required Main Database not Exist!");
        }

        // Kontrola kolekcí nad Mongo Databází
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("mongo"))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);

        classes.forEach(cls -> {
            try {

                Class<? extends _Abstract_MongoModel> model = (Class<? extends _Abstract_MongoModel>) cls; // Cast to model
                Entity annotation = model.getAnnotation(Entity.class);

                String value = annotation.value();

                if (!this.database.listCollectionNames().into(new ArrayList<>()).contains(value)) {
                    logger.warn("constructor - {} Collection:: {}  - not exist. System will create that! ", model.getSimpleName(), value);
                    this.database.createCollection(value);
                }

                for (Field field : model.getFields()) {
                    if (field.isAnnotationPresent(InjectStore.class) && field.get(null) instanceof CacheMongoFinder) {
                        ((CacheMongoFinder)field.get(null)).setDatastore(this.datastore);
                    }
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        });
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }
}
