package mongo.mongo_services;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import utilities.logger.Logger;

import java.util.List;

import static mongo.mongo_services._SubscriberHelpers.ObservableSubscriber;
import static mongo.mongo_services._SubscriberHelpers.OperationSubscriber;

@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class _MongoNativeCollection {

    private static final Logger logger = new Logger(_MongoNativeCollection.class);

    private MongoClient mongoClient;
    private MongoDatabase database;
    private  MongoCollection<Document> collection;

    @Inject
    public _MongoNativeCollection(String database_name, String collection_name) {

        database = _MongoNativeConnector.getDatabase(database_name);
        collection = database.getCollection(collection_name);

    }

    // Diferent Connection URL then default
    @Inject
    public _MongoNativeCollection(String database_name, String url, String collection_name) {
        database = _MongoNativeConnector.getDatabase(database_name);
        collection = database.getCollection(collection_name);
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }


    /**
     * Drop all the data in it
     * @throws Throwable
     */
    public ObservableSubscriber<Success> dropCollection() throws Throwable {

        logger.trace("dropCollection:: collection name: {}", this.collection.getNamespace());
        // Create subscriberr
        ObservableSubscriber subscriber = new ObservableSubscriber<Success>();

        logger.trace("dropCollection:: subscriber created");
        // Call Action
        this.collection.drop().subscribe(subscriber);

        logger.trace("dropCollection:: subscribed");

        // Provide Action
        return  subscriber;

    }


    /**
     * @param doc
     * @throws Throwable
     */
    public ObservableSubscriber<Success> insertDocumentToCollection(Document doc) throws Throwable {

        logger.trace("insertDocumentToCollection:: collection name: {}",  this.collection.getNamespace());
        // Create subscriber
        OperationSubscriber subscriber = new OperationSubscriber<Success>();

        logger.trace("insertDocumentToCollection:: subscriber created");
        // Call Action
        collection.insertOne(doc).subscribe(subscriber);

        logger.trace("insertDocumentToCollection:: subscribed");

        // Provide Action
        return subscriber;

    }

    /**
     *
     * @param docs
     * @throws Throwable
     */
    public ObservableSubscriber<Success> insertDocumentToCollection(List<Document> docs) throws Throwable {

        logger.trace("insertDocumentToCollection:: collection name: {}, documents: {}",  this.collection.getNamespace(), docs.size());
        // Create subscriber
        ObservableSubscriber subscriber = new ObservableSubscriber<Success>();

        logger.trace("insertDocumentToCollection::  subscriber created");
        // Call Action
        this.collection.insertMany(docs).subscribe(subscriber);

        logger.trace("insertDocumentToCollection::  subscribed");

        // Provide Action
        return subscriber;

    }


    public ObservableSubscriber<Document> getDocumentNonBlocking( Bson filter) {

        // Create subscriber
        ObservableSubscriber subscriber = new ObservableSubscriber<Document>();

        // Call Action
        this.collection.find(filter).subscribe(subscriber);

        // Provide Action
        return subscriber;
    }


    public ObservableSubscriber<DeleteResult> deleteDocument( Bson filter) {

        logger.trace("deleteDocument:: collection name: {}, filter: {}", this.collection.getNamespace(), filter.toString());
        // Create subscriber
        OperationSubscriber subscriber = new OperationSubscriber<DeleteResult>();

        logger.trace("deleteDocument:: subscriber created");
        // Call Action
        this.collection.deleteMany(filter).subscribe(subscriber);

        // Provide Action
        return subscriber;

    }


    public MongoDatabase getMongoDatabase() {
        return this.database;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }
}
