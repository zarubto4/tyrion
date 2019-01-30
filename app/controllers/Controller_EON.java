package controllers;


import com.mongodb.client.model.*;
import mongo.mongo_services._MongoNativeCollection;
import mongo.mongo_services._MongoNativeConnector;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.conversions.Bson;
import play.libs.concurrent.HttpExecutionContext;
import responses.*;
import utilities.logger.Logger;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.*;

import play.libs.ws.WSClient;
import play.mvc.Result;

import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.Swagger_EON_data_request;
import utilities.swagger.input.Swagger_NameAndDescription;
import utilities.swagger.output.Swagger_EON_data_values;

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


    @ApiOperation(
            value = "get grouped values",
            tags = {"EON"},
            notes = "values in given date range, from given hardware, averaged inside time intervals given in minutes"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_EON_data_request",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_EON_data_values.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result getValues(){
        try {
            Swagger_EON_data_request request = formFromRequestWithValidation(Swagger_EON_data_request.class);
            _MongoNativeCollection collection = new _MongoNativeCollection("EON_LOCAL_TEST", "TEST9");

            List<? extends Bson> pipeline = Arrays.asList(
                    new Document()
                            .append("$match", new Document()
                                    .append("obic_code", request.obis_code) // TODO Fixnout chybu v obic_code na obis_code
                                    .append("timestamp", new Document()
                                            .append("$gte", new Date(request.startDate))
                                            .append("$lte", new Date(request.endDate))
                                    )
                                    .append("metter_id", new Document()
                                            .append("$in", request.hardwares)
                                    )
                            ),
                    new Document()
                            .append("$group", new Document()
                                    .append("_id", new Document()
                                            .append("hardware", "$metter_id")
                                            .append("date", new Document()
                                                    .append("$toDate", new Document()
                                                            .append("$subtract", Arrays.asList(
                                                                    new Document()
                                                                            .append("$toLong", "$timestamp"),
                                                                    new Document()
                                                                            .append("$mod", Arrays.asList(
                                                                                    new Document()
                                                                                            .append("$toLong", "$timestamp"),
                                                                                    request.interval*1000            // minutes to miliseconds
                                                                                    )
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                                    .append("avg", new Document()
                                            .append("$avg", "$value")
                                    )
                            )
            );
            ObservableSubscriber subscriber = new ObservableSubscriber<Document>();
            collection.getCollection()
                    .aggregate(pipeline)
                    .subscribe(subscriber);

            List<Document> documents3 = subscriber.get(4000, TimeUnit.SECONDS);

            List<Swagger_EON_data_values> res = documents3.stream().map(document -> {
                Swagger_EON_data_values result = new Swagger_EON_data_values();
                Document id = document.get("_id", Document.class);
                result.avg = document.getDouble("avg");
                result.date = id.getDate("date");
                result.hardware = id.getString("hardware");
                return result;
            }).collect(Collectors.toList());


            return ok(res);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return badRequest();
        }
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

            List<? extends Bson> pipeline = Arrays.asList(
                    new Document()
                            .append("$match", new Document()
                                    .append("obic_code", "1.0.1.8.0.255")
                            ),
                    new Document()
                            .append("$group", new Document()
                                    .append("_id", new Document()
                                            .append("year", new Document()
                                                    .append("$year", "$timestamp")
                                            )
                                            .append("dayOfYear", new Document()
                                                    .append("$dayOfYear", "$timestamp")
                                            )
                                            .append("hour", new Document()
                                                    .append("$hour", "$timestamp")
                                            )
                                    )
                                    .append("avg", new Document()
                                            .append("$avg", "$value")
                                    )
                            )
            );

            ObservableSubscriber subscriber = new ObservableSubscriber<Document>();
            collection.getCollection()
                    .aggregate(pipeline)
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
