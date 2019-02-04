package _projects.eon;


import _projects.eon.mongo_model.ModelMongo_Electricity_meter;
import _projects.eon.swagger_model.in.Swagger_EON_Electricity_meter_create_edit;
import _projects.eon.swagger_model.in.Swagger_EON_Electricity_meter_filter;
import _projects.eon.swagger_model.out.filter.Swagger_EON_Electricity_Metter_List;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.reactivestreams.client.MongoCollection;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.ebean.Ebean;
import io.ebean.Query;
import models.Model_Hardware;
import models.Model_InstanceSnapshot;
import mongo.ModelMongo_ThingsMobile_CRD;
import mongo.mongo_services._MongoNativeCollection;
import mongo.mongo_services._MongoNativeConnector;
import org.bson.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Sorts.descending;

import org.bson.conversions.Bson;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.BodyParser;
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
import _projects.eon.swagger_model.in.Swagger_EON_data_request;
import _projects.eon.swagger_model.out.Swagger_EON_data_values;
import utilities.swagger.input.Swagger_Board_Filter;
import utilities.swagger.output.filter_results.Swagger_Hardware_List;

import static mongo.mongo_services._SubscriberHelpers.ObservableSubscriber;


// @Security.Authenticated(Authentication.class)
@Api(value = "EON")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_EON extends _BaseController {

    private HttpExecutionContext httpExecutionContext;
    private _MongoNativeConnector mongoNativeConnector;
    private _MongoNativeCollection collection;

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_EON.class);

    @Inject
    public Controller_EON(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                          NotificationService notificationService, EchoService echoService, _MongoNativeConnector mongoNativeConnector, HttpExecutionContext httpExecutionContext) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.httpExecutionContext = httpExecutionContext;
        this.mongoNativeConnector = mongoNativeConnector;
        collection = this.mongoNativeConnector.getDatabaseCollection("EON_LOCAL_TEST", "TEST9");
    }


    @ApiOperation(
            value = "get Electricity_Meter_Data",
            tags = {"EON"},
            notes = "values in given date range, from given hardware, averaged inside time intervals given in minutes"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.eon.swagger_model.in.Swagger_EON_data_request",
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

            if( request.hardwares == null){
                request.hardwares = new ArrayList<>();

                request.hardwares.add("0A014C4F4700044AA26C");
                request.hardwares.add("0A014C4F4700044AA26D");
            }

            List<? extends Bson> pipeline = Arrays.asList(
                    new Document()
                            .append("$match", new Document()
                                    .append("obic_code", request.obis_code) // TODO Fixnout chybu v obic_code na obis_code
                                    .append("timestamp", new Document()
                                            .append("$gte", new Date(request.startDate * 1000))
                                            .append("$lte", new Date(request.endDate * 1000))
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
            ObservableSubscriber subscriber = new ObservableSubscriber<Document>();
            collection.getCollection()
                    .aggregate(pipeline)
                    .subscribe(subscriber);

            List<Document> documents3 = subscriber.get(4000, TimeUnit.SECONDS);


            return ok_mongo(documents3);
        } catch (Throwable e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(
            value = "get Electricity_Meter List",
            tags = {"EON"},
            notes = "Get List of Electricity Meters"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.eon.swagger_model.in.Swagger_EON_Electricity_meter_filter",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_EON_Electricity_Metter_List.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_electricity_meters(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true) Integer page_number){
        try {

            Swagger_EON_Electricity_meter_filter request = formFromRequestWithValidation(Swagger_EON_Electricity_meter_filter.class);

            xyz.morphia.query.Query<ModelMongo_Electricity_meter> query = ModelMongo_Electricity_meter.find.query();


            Swagger_EON_Electricity_Metter_List list = new Swagger_EON_Electricity_Metter_List(query, page_number, request);

            return ok(list);

        } catch (Throwable e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Gateway List",
            tags = { "EON"},
            notes = "Get List of hardware. According to permission - system return only hardware from project, where is user owner or" +
                    " all hardware if user have static Permission key",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Board_Filter",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Hardware_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result gateway_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true) Integer page_number) {
        try {

            // Get and Validate Object
            Swagger_Board_Filter help = formFromRequestWithValidation(Swagger_Board_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(
                    help.projects != null && !help.projects.isEmpty()
                            || ( help.producers != null && !help.producers.isEmpty() )
                            || ( help.processors != null && !help.processors.isEmpty())
                            || ( help.hardware_groups_id != null && !help.hardware_groups_id.isEmpty())
            ) && !isAdmin()) {
                return ok(new Swagger_Hardware_List());
            }

            // Tvorba parametru dotazu
            Query<Model_Hardware> query = Ebean.find(Model_Hardware.class);

            // not deleted
            query.where().ne("deleted", true);


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

            if (help.hardware_type_ids != null && !help.hardware_type_ids.isEmpty()) {
                query.where().in("hardware_type.id", help.hardware_type_ids);
            }

            // If contains confirms
            if (help.active != null) {
                query.where().eq("is_active", help.active.equals("true"));
            }

            if (help.projects != null && !help.projects.isEmpty()) {
                query.where().in("project.id", help.projects);
            }

            if (help.producers != null) {
                query.where().in("hardware_type.producer.id", help.producers);
            }

            if (help.processors != null) {
                query.where().in("hardware_type.processor.id", help.processors);
            }

            if (help.instance_snapshot != null) {
                query.where().in("id",  Model_InstanceSnapshot.find.byId(help.instance_snapshot).getHardwareIds());
            }

            if (help.hardware_groups_id != null) {
                query.where().in("hardware_groups.id", help.hardware_groups_id);
            }

            // From date
            if (help.start_time != null) {
                query.where().ge("created", help.start_time);
            }

            // To date
            if (help.end_time != null) {
                query.where().le("created", help.end_time);
            }

            // Vytvářím seznam podle stránky
            Swagger_Hardware_List result = new Swagger_Hardware_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(
            value = "create Electricity_Meter",
            tags = {"EON"},
            notes = "Create Electricity Meter"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.eon.swagger_model.in.Swagger_EON_Electricity_meter_create_edit",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = ModelMongo_Electricity_meter.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result create_electro_metters(){
        try {

            Swagger_EON_Electricity_meter_create_edit request = formFromRequestWithValidation(Swagger_EON_Electricity_meter_create_edit.class);

            ModelMongo_Electricity_meter meter = new ModelMongo_Electricity_meter();
            meter.identification_id = request.identification_id;
            meter.name = request.name;
            meter.description = request.description;
            meter.latitude = request.latitude;
            meter.longitude = request.longitude;
            meter.owner_id = request.owner_id;
            meter.gateway_id = request.gateway_id;
            meter.save();

            return ok(meter);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return badRequest();
        }
    }


// HELPERS ##############################################################################################################


    public void getGeoPointFromAddress(String locationAddress) throws MalformedURLException {


        String locationAddres = locationAddress.replaceAll(" ", "%20");
        String str = "http://maps.googleapis.com/maps/api/geocode/json?address="
                + locationAddres + "&sensor=true";


        JsonNode result = this.PUT(new URL(str), 2000, null, null);
        System.out.println("getGeoPointFromAddress" + result.toString());


    }
}
