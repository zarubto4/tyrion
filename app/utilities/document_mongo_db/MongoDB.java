package utilities.document_mongo_db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.Model_HardwareBatch;
import models.Model_HardwareRegistrationEntity;
import org.bson.Document;
import utilities.logger.Logger;

import java.util.HashMap;

public class MongoDB {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(MongoDB.class);

    // Referenční proměnné
    public static final String DATABASE__HARDWARE_REGISTRATION_AUTHORITY = "hardware-registration-authority-database";

/* Mongo VALUE  -----------------------------------------------------------------------------------------------------*/
    // Privátní proměnné
    private static MongoClient client;
    private static HashMap<String, MongoDatabase> databaseHashMap = new HashMap<>();
    private static HashMap<String,  MongoCollection<Document>> collectionHashMap = new HashMap<>();


    public static MongoCollection<Document> get_collection(String collection) {

        if(collection.equals(Model_HardwareBatch.COLLECTION_NAME)){
            return collectionHashMap.get(Model_HardwareBatch.COLLECTION_NAME);
        }

        if(collection.equals(Model_HardwareRegistrationEntity.COLLECTION_NAME)){
            return collectionHashMap.get(Model_HardwareRegistrationEntity.COLLECTION_NAME);
        }

        logger.error("MongoDB: get_collection " + collection + " is not supported on  MongoClient or By Tyrion. Check Class MongoDB please");
        throw new UnsupportedOperationException("MongoDB: get_collection " + collection + " is not supported on  MongoClient or By Tyrion. Check Class MongoDB please");

    }

    public static void init() {

        // Připojení na MongoClient v Azure
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
        client = mongoClient;

        // Připojení na konkrétní Databázi clienta
        MongoDatabase database_hardware_registration = mongoClient.getDatabase(DATABASE__HARDWARE_REGISTRATION_AUTHORITY);
        databaseHashMap.remove(DATABASE__HARDWARE_REGISTRATION_AUTHORITY);
        databaseHashMap.put(DATABASE__HARDWARE_REGISTRATION_AUTHORITY, database_hardware_registration);

        // Remove Collection
        databaseHashMap.remove(Model_HardwareBatch.COLLECTION_NAME);
        databaseHashMap.remove(Model_HardwareRegistrationEntity.COLLECTION_NAME);

        // Set Collections
        collectionHashMap.put(Model_HardwareBatch.COLLECTION_NAME, database_hardware_registration.getCollection(Model_HardwareBatch.COLLECTION_NAME));
        collectionHashMap.put(Model_HardwareRegistrationEntity.COLLECTION_NAME, database_hardware_registration.getCollection(Model_HardwareRegistrationEntity.COLLECTION_NAME));

    }



}
