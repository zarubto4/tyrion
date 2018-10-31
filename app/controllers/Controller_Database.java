package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.swagger.annotations.*;
import models.Model_Product;
import models.Model_ProductExtension;
import play.Environment;
import play.api.libs.ws.WSRequest;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.enums.ExtensionType;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.mongo_cloud_api.MongoCloudApi;
import utilities.mongo_cloud_api.SwaggerMongoCloudUser;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_Database_New;
import utilities.swagger.output.Swagger_Database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

//@Security.Authenticated(Authentication.class)
@Api(value = "Database")
public class Controller_Database extends _BaseController {


// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Database.class);

// CONTROLLER CONFIGURATION ############################################################################################


    @Inject
    public Controller_Database(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }


    @ApiOperation(
            value = "create database",
            tags = {"Database"},
            notes = "Create database with collection",
            code = 201  // Only if we have 201 Code (for 200 its not required, its default value)
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.output.Swagger_Database", // Class that describes what I consume
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values" // Description about body in http request
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Swagger_Database.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result create_db() {
        try {
            // Get and Validate Object
            Swagger_Database_New info = formFromRequestWithValidation(Swagger_Database_New.class);

            Model_Product product = Model_Product.find.byId(info.product_id);
  //          checkUpdatePermission(product);

            Optional<Model_ProductExtension> existingDatabase = Model_ProductExtension.find.query().where()
                    .eq("product.id", info.product_id)
                    .eq("active", true)
                    .eq("type", ExtensionType.DATABASE).findOneOrEmpty();




            // Creeate and set Object
            Model_ProductExtension extension  = new Model_ProductExtension();
            extension.product     = product;
            extension.name        = info.name;
            extension.description = info.description;
            extension.type        = ExtensionType.DATABASE;
            extension.active      = false;
            extension.save();

            MongoClient client = Server.mongoClient;
            MongoDatabase database = client.getDatabase("" + extension.id);
            database.createCollection(info.collection_name);


             SwaggerMongoCloudUser user;
            MongoCloudApi mongoApi = new MongoCloudApi(ws, baseFormFactory);
            if(existingDatabase.isPresent()) {
                JsonNode json = Json.parse(existingDatabase.get().configuration);
                user = this.baseFormFactory.formFromJsonWithValidation(SwaggerMongoCloudUser.class, json);
                user = mongoApi.addRole(user, ""+extension.id );
            } else {
                user = mongoApi.createUser(info.product_id, "" + extension.id);

            }

            extension.configuration = Json.toJson(user).toString();
            extension.update();

            Swagger_Database created_database = new Swagger_Database();
            created_database.name = extension.name;
            created_database.description = extension.description;
            created_database.id = extension.id;
            created_database.conectionString = mongoApi.getConnectionStringForUser(user);

            extension.active = true;
            return created(created_database);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(
            value = "get databases",
            tags = {"Database"},
            notes = "List all databases by product_id"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Database.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result list_db(UUID product_id) {
        try {
            Model_Product product = Model_Product.find.byId(product_id);
            checkReadPermission(product);

            List<Model_ProductExtension> extensionList = Model_ProductExtension.find.query().where()
                                                                                            .eq("product.id", product_id)
                                                                                            .eq("active", true)
                                                                                            .eq("type", ExtensionType.DATABASE)
                                                                                            .findList();

            List<Swagger_Database> result = extensionList.stream()
                                                         .map(Controller_Database::extensionToSwaggerDatabase)
                                                         .collect(Collectors.toList());


            return ok(result);
        } catch (Exception e) {
            return  controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Database",
            tags = {"Database"},
            notes = "delete Database by id"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result drop_db(UUID db_id) {
        try {
            Model_ProductExtension productExtension = Model_ProductExtension.find.byId(db_id);
            delete(productExtension);

            productExtension.active = false;

            Server.mongoClient.dropDatabase("" + db_id);
            return(ok());
        } catch ( Exception e ) {
            return controllerServerError(e);
        }
    }

    private static Swagger_Database extensionToSwaggerDatabase(Model_ProductExtension extension) {
        Swagger_Database result = new Swagger_Database();
        result.name = extension.name;
        result.description = extension.description;
        result.id = extension.id;
        return result;
    }
}