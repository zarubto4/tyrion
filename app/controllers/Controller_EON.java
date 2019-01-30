package controllers;


import com.mongodb.client.model.*;
import mongo.mongo_services._MongoNativeCollection;
import mongo.mongo_services._MongoNativeConnector;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.conversions.Bson;
import play.libs.concurrent.HttpExecutionContext;
import utilities.logger.Logger;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.*;

import play.libs.ws.WSClient;
import play.mvc.Result;

import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;

import static mongo.mongo_services._SubscriberHelpers.ObservableSubscriber;


// @Security.Authenticated(Authentication.class)
@Api(value = "EON")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_EON extends _BaseController {

    private HttpExecutionContext httpExecutionContext;

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_EON.class);

    @Inject
    public Controller_EON(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                          NotificationService notificationService, EchoService echoService, _MongoNativeConnector mongoNativConnector, HttpExecutionContext httpExecutionContext) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.httpExecutionContext = httpExecutionContext;
    }




    public Result get_data() {
        try {


            _MongoNativeCollection collection = new _MongoNativeCollection("EON_LOCAL_TEST", "TEST9");

            /*
            Bson b = and(gt("i", 4), lte("i", 10));

            ObservableSubscriber<Document> o = collection.getDocumentNonBlocking(b);
            List<Document> documents1 = o.await().getReceived();
            System.out.println("documents " + documents1.size());


            ObservableSubscriber<Document> o2 = collection.getDocumentNonBlocking(b);
            List<Document> documents2 = o2.get(4000, TimeUnit.SECONDS);

            System.out.println("documents " + documents2.size());
            */


            LocalDateTime datetime = LocalDateTime.now();
            Integer interValue = 15*1000*60;


            ObservableSubscriber subscriber = new ObservableSubscriber<Document>();
            collection.getCollection()
                    .aggregate(
                            Arrays.asList(
                                    Aggregates.match(
                                            and(
                                                eq("obic_code","1.0.1.8.0.255"),
                                                eq("matter_id","0A014C4F4700044AA26C"),
                                                    gte("timestamp",LocalDateTime.now().minusMonths(1)),
                                                    lte("timestamp", LocalDateTime.now() )
                                            )
                                    ),
                                    Aggregates.group("_id",
                                            Accumulators.avg("comsuption", "$value"),
                                            Accumulators.avg("time", "$timestamp")
                                    ),
                                    Aggregates.project(
                                            Projections.include("matter_id", "comsuption", "time")
                                    )
                            ))
                    .subscribe(subscriber);

            List<Document> documents3 = subscriber.get(4000, TimeUnit.SECONDS);


            return ok_mongo(documents3);


            /*
            subscriber = new PrintDocumentSubscriber();
            collection.find().first().subscribe(subscriber);
            subscriber.await();

            subscriber = new PrintDocumentSubscriber();
            collection.find().subscribe(subscriber);
            subscriber.await();
            */

            /*
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

            */

            /*
            // ordered bulk writes
            List<WriteModel<Document>> writes = new ArrayList<WriteModel<Document>>();
            writes.add(new InsertOneModel<Document>(new Document("_id", 4)));
            writes.add(new InsertOneModel<Document>(new Document("_id", 5)));
            writes.add(new InsertOneModel<Document>(new Document("_id", 6)));
            writes.add(new UpdateOneModel<Document>(new Document("_id", 1), new Document("$set", new Document("x", 2))));
            writes.add(new DeleteOneModel<Document>(new Document("_id", 2)));
            writes.add(new ReplaceOneModel<Document>(new Document("_id", 3), new Document("_id", 3).append("x", 4)));

            ObservableSubscriber subscriber = new PrintSubscriber<BulkWriteResult>("Bulk write results: %s");
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
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return badRequest();
        }
    }

}
