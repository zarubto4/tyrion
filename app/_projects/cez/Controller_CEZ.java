package _projects.cez;

import _projects.cez.swagger_model.in.Swagger_CEZ_Sensor_filter;
import _projects.cez.swagger_model.in.Swagger_CEZ_data_request;
import _projects.eon.swagger_model.in.Swagger_EON_data_request;
import _projects.eon.swagger_model.out.Swagger_EON_data_values;
import _projects.eon.swagger_model.out.filter.Swagger_EON_Electricity_Metter_List;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_Hardware;
import models.Model_InstanceSnapshot;
import mongo.mongo_services._MongoNativeCollection;
import mongo.mongo_services._MongoNativeConnector;
import mongo.mongo_services._SubscriberHelpers;
import org.bson.Document;
import org.bson.conversions.Bson;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Result;
import responses.*;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.Swagger_Board_Filter;
import utilities.swagger.output.filter_results.Swagger_Hardware_List;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


// @Security.Authenticated(Authentication.class)
@Api(value = "CEZ")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_CEZ extends _BaseController {

    private HttpExecutionContext httpExecutionContext;
    private _MongoNativeConnector mongoNativeConnector;
    private _MongoNativeCollection collection;

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_projects.eon.Controller_EON.class);

    @Inject
    public Controller_CEZ(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                          NotificationService notificationService, EchoService echoService, _MongoNativeConnector mongoNativeConnector, HttpExecutionContext httpExecutionContext) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.httpExecutionContext = httpExecutionContext;
        this.mongoNativeConnector = mongoNativeConnector;
        collection = this.mongoNativeConnector.getDatabaseCollection("4d7fd102-a3b4-4dde-914c-c0dcfaa545ac", "PRIME");
    }


    @ApiOperation(
            value = "get Sensor_Data",
            tags = {"CEZ"},
            notes = "values in given date range, from given hardware, averaged inside time intervals given in minutes"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.cez.swagger_model.in.Swagger_CEZ_data_request",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_EON_data_values.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result getValues() {
        try {

            Swagger_CEZ_data_request request = formFromRequestWithValidation(Swagger_CEZ_data_request.class);

            List<? extends Bson> pipeline = Arrays.asList(
                    new Document()
                            .append("$match", new Document()
                                    .append("type_of_values", request.data_typ)
                                    .append("timestamp", new Document()
                                            .append("$gte", new Date(request.startDate))
                                            .append("$lte", new Date(request.endDate))
                                    )
                                    .append("hardware_id", new Document()
                                            .append("$in", request.hardwares)
                                    )
                            ),
                    new Document()
                            .append("$group", new Document()
                                    .append("_id", new Document()
                                            .append("hardware", "$type_of_values")
                                            .append("date", new Document()
                                                    .append("$toDate", new Document()
                                                            .append("$subtract", Arrays.asList(
                                                                    new Document()
                                                                            .append("$toLong", "$timestamp"),
                                                                    new Document()
                                                                            .append("$mod", Arrays.asList(
                                                                                    new Document()
                                                                                            .append("$toLong", "$timestamp"),
                                                                                            request.interval*1000                     // minutes to miliseconds
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
                            ),
                    new Document()
                            .append("$sort", new Document()
                                    .append("_id.date", 1.0)
                            ),
                    new Document()
                            .append("$group", new Document()
                                    .append("_id", "$_id.hardware")
                                    .append("data", new Document()
                                            .append("$push", new Document()
                                                    .append("time", "$_id.date")
                                                    .append("value", "$avg")
                                            )
                                    )
                            )
            );
            _SubscriberHelpers.ObservableSubscriber subscriber = new _SubscriberHelpers.ObservableSubscriber<Document>();
            collection.getCollection()
                    .aggregate(pipeline)
                    .subscribe(subscriber);

            List<Document> documents = subscriber.get(4000, TimeUnit.SECONDS);

            return ok_mongo(documents);

        } catch (Throwable e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(
            value = "get Sensors List",
            tags = {"CEZ"},
            notes = "Get List of Sensors"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.cez.swagger_model.in.Swagger_CEZ_Sensor_filter",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Hardware_List.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result getDevices(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true) Integer page_number) {
        try {

            // Get and Validate Object
            Swagger_CEZ_Sensor_filter help = formFromRequestWithValidation(Swagger_CEZ_Sensor_filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!

            // Tvorba parametru dotazu
            Query<Model_Hardware> query = Ebean.find(Model_Hardware.class);

            // not deleted
            query.where().ne("deleted", true);
            query.where().eq("project.id", UUID.fromString("9c0baacb-e702-47fe-b4ad-8fe2f167b0d8"));

            if (help.order_by != null) {

                if(help.order_by == Swagger_Board_Filter.Order_by.NAME) {
                    query.where().order("name" + " " + help.order_schema  );
                }

                if(help.order_by == Swagger_Board_Filter.Order_by.FULL_ID) {
                    query.where().order("full_id" + " " + help.order_schema );

                }

                if(help.order_by == Swagger_Board_Filter.Order_by.ID) {
                    query.where().order("id" + " " + help.order_schema );
                }

            }

            if (help.full_id != null && help.full_id.length() > 0) {
                System.out.println("Full ID vyplněno: " + help.full_id + " l: " + help.full_id.length());
                query.where().icontains("full_id", help.full_id);
            }

            if (help.id != null) {
                System.out.println("ID vyplněno: " + help.id);
                query.where().eq("id", help.id);
            }

            if (help.name != null && help.name.length() > 0) {
                System.out.println("name vyplněno: " + help.name + " l: " + help.name.length());
                query.where().icontains("name", help.name);
            }

            if (help.description != null && help.description.length() > 0) {
                System.out.println("description vyplněno: " + help.description + " l: " + help.description.length());
                query.where().icontains("description", help.description);
            }

            if (help.hardware_groups_id != null) {
                query.where().in("hardware_groups.id", help.hardware_groups_id);
            }

            // Vytvářím seznam podle stránky
            Swagger_Hardware_List result = new Swagger_Hardware_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }



}
