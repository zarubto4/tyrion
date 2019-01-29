package controllers;


import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.connection.netty.NettyStreamFactoryFactory;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.descending;
import static java.util.Arrays.asList;
import org.bson.Document;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.network.JsonNetworkStatus;
import utilities.network.Networkable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;
import utilities.swagger.output.Swagger_Short_Reference;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.*;

import play.libs.ws.WSClient;
import play.mvc.Result;

import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import static mongo._SubscriberHelpers.ObservableSubscriber;
import static mongo._SubscriberHelpers.OperationSubscriber;
import static mongo._SubscriberHelpers.PrintDocumentSubscriber;
import static mongo._SubscriberHelpers.PrintSubscriber;


// @Security.Authenticated(Authentication.class)
@Api(value = "EON")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_EON extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Database.class);

    @Inject
    public Controller_EON(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                               NotificationService notificationService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);

    }

    /**
     * K předělání po vzru MongoDBCOnnector
     * @param cll
     * @return
     */
    public MongoCollection<Document> getDatabase(String cll) {

        // SET Values
        String mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        String url = config.getString("MongoDB." + mode + ".url"); // Cluster

        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(asList(new ServerAddress(url)))
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings).build();

        MongoClient mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase("EON_LOCAL_TEST");
        MongoCollection<Document> collection = database.getCollection(cll);

        return collection;
    }

    public Result get_data() {
        try {


            MongoCollection<Document> collection = getDatabase("TEST10");

            /*
            // drop all the data in it
            ObservableSubscriber subscriber = new ObservableSubscriber<Success>();
            collection.drop().subscribe(subscriber);
            subscriber.await();

            // make a document and insert it
            Document doc = new Document("name", "MongoDB")
                    .append("type", "database")
                    .append("count", 1)
                    .append("info", new Document("x", 203).append("y", 102));

            collection.insertOne(doc).subscribe(new OperationSubscriber<Success>());

            // get it (since it's the only one in there since we dropped the rest earlier on)
            collection.find().first().subscribe(new PrintDocumentSubscriber());

            // now, lets add lots of little documents to the collection so we can explore queries and cursors
            List<Document> documents = new ArrayList<Document>();
            for (int i = 0; i < 100; i++) {
                documents.add(new Document("i", i));
            }

            subscriber = new ObservableSubscriber<Success>();
            collection.insertMany(documents).subscribe(subscriber);
            subscriber.await();

            collection.countDocuments()
                    .subscribe(new PrintSubscriber<Long>("total # of documents after inserting 100 small ones (should be 101): %s"));

            subscriber = new PrintDocumentSubscriber();
            collection.find().first().subscribe(subscriber);
            subscriber.await();

            subscriber = new PrintDocumentSubscriber();
            collection.find().subscribe(subscriber);
            subscriber.await();

            // Query Filters
            // now use a query to get 1 document out
            collection.find(eq("i", 71)).first().subscribe(new PrintDocumentSubscriber());

            // now use a range query to get a larger subset
            collection.find(gt("i", 50)).subscribe(new PrintDocumentSubscriber());

            // range query with multiple constraints
            collection.find(and(gt("i", 50), lte("i", 100))).subscribe(new PrintDocumentSubscriber());

            // Sorting
            collection.find(exists("i")).sort(descending("i")).first().subscribe(new PrintDocumentSubscriber());

            // Projection
            collection.find().projection(excludeId()).first().subscribe(new PrintDocumentSubscriber());

            // Update One
            collection.updateOne(eq("i", 10), new Document("$set", new Document("i", 110)))
                    .subscribe(new PrintSubscriber<UpdateResult>("Update Result: %s"));


            // Update Many
            subscriber = new PrintSubscriber<UpdateResult>("Update Result: %s");
            collection.updateMany(lt("i", 100), new Document("$inc", new Document("i", 100))).subscribe(subscriber);
            subscriber.await();

            // Delete One
            collection.deleteOne(eq("i", 110)).subscribe(new PrintSubscriber<DeleteResult>("Delete Result: %s"));

            // Delete Many
            collection.deleteMany(gte("i", 100)).subscribe(new PrintSubscriber<DeleteResult>("Delete Result: %s"));

            subscriber = new ObservableSubscriber<Success>();
            collection.drop().subscribe(subscriber);
            subscriber.await();

            // ordered bulk writes
            List<WriteModel<Document>> writes = new ArrayList<WriteModel<Document>>();
            writes.add(new InsertOneModel<Document>(new Document("_id", 4)));
            writes.add(new InsertOneModel<Document>(new Document("_id", 5)));
            writes.add(new InsertOneModel<Document>(new Document("_id", 6)));
            writes.add(new UpdateOneModel<Document>(new Document("_id", 1), new Document("$set", new Document("x", 2))));
            writes.add(new DeleteOneModel<Document>(new Document("_id", 2)));
            writes.add(new ReplaceOneModel<Document>(new Document("_id", 3), new Document("_id", 3).append("x", 4)));

            subscriber = new PrintSubscriber<BulkWriteResult>("Bulk write results: %s");
            collection.bulkWrite(writes).subscribe(subscriber);
            subscriber.await();

            subscriber = new ObservableSubscriber<Success>();
            collection.drop().subscribe(subscriber);
            subscriber.await();

            subscriber = new PrintSubscriber<BulkWriteResult>("Bulk write results: %s");
            collection.bulkWrite(writes, new BulkWriteOptions().ordered(false)).subscribe(subscriber);
            subscriber.await();

            subscriber = new PrintDocumentSubscriber();
            collection.find().subscribe(subscriber);
            subscriber.await();

            // Clean up
            subscriber = new PrintSubscriber("Collection Dropped");
            collection.drop().subscribe(subscriber);
            subscriber.await();

            // release resources
            // mongoClient.close();
            */

            return ok();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return badRequest();
        }
    }

}
