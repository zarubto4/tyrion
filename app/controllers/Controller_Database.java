package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.ebeaninternal.server.core.Message;
import io.swagger.annotations.*;
import models.Model_Product;
import models.Model_ProductExtension;
import play.Environment;
import play.api.Configuration;
import play.api.libs.ws.WSRequest;
import play.libs.ws.WSAuthScheme;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.configurations.Configuration_Database;
import utilities.financial.extensions.extensions.Extension_Database;
import utilities.financial.products.ConfigurationProduct;
import utilities.financial.services.ProductService;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.mongo_cloud_api.MongoCloudApi;
import utilities.mongo_cloud_api.SwaggerMongoCloudUser;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_Database_New;
import utilities.swagger.input.Swagger_NameAndDescription;
import utilities.swagger.input.Swagger_ProductExtension_New;
import utilities.swagger.output.Swagger_Database;
import utilities.swagger.output.Swagger_DatabaseCollectionList;
import utilities.swagger.output.Swagger_Database_Collections;
import utilities.swagger.output.Swagger_Database_List;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Security.Authenticated(Authentication.class)
@Api(value = "Database")
public class Controller_Database extends _BaseController {
    private MongoCloudApi mongoApi;
    private ProductService productService;
// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Database.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_Database(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService, ProductService productService, MongoCloudApi mongoApi) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
        this.productService = productService;
        this.mongoApi = mongoApi;

    }


    @ApiOperation(
            value = "create database",
            tags = {"Database"},
            notes = "Create database with collection",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Database_New", // Class that describes what I consume
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

            Swagger_Database_New info = formFromRequestWithValidation(Swagger_Database_New.class);

            Model_Product product = Model_Product.find.byId(info.product_id);
            checkUpdatePermission(product);

            List<Model_ProductExtension> existingDatabaseList = Model_ProductExtension.find.query().where()
                    .eq("product.id", info.product_id)
                    .eq("active", true)
                    .eq("type", ExtensionType.DATABASE)
                    .findList();

            SwaggerMongoCloudUser user;

            Swagger_ProductExtension_New extensionData = new Swagger_ProductExtension_New();
            extensionData.name = info.name;
            extensionData.description = info.description;
            extensionData.extension_type = ExtensionType.DATABASE.toString();
            extensionData.config = Configuration_Database.getDefault().toJsonString();
            extensionData.color = Extension_Database.defaultColour;

            Model_ProductExtension extension = productService.createAndActivateExtension(product,extensionData);
            extension.setActive(false); // extension should remain deactivated until database is actually created


            ConfigurationProduct configuration;
            if ( product.configuration != null && !product.configuration.isEmpty() ) {
                configuration = formFactory.formFromJsonWithValidation(ConfigurationProduct.class, Json.parse(product.configuration));
                if (configuration == null ) {
                    configuration = new ConfigurationProduct();
                }
            }
            else {
                configuration = new ConfigurationProduct();
            }


            if ( configuration.mongoDatabaseUserPassword == null || configuration.mongoDatabaseUserPassword.isEmpty()) { // if user not exist in database

                user = mongoApi.createUser(info.product_id, extension.id.toString());
                configuration.mongoDatabaseUserPassword = user.password;
                product.configuration = Json.toJson(configuration).toString();
                product.update();
            }
            else {
                mongoApi.addRole(product.id.toString(), extension.id.toString());
            }

            MongoClient client = Server.mongoClient;
            MongoDatabase database = client.getDatabase(extension.id.toString());
            database.createCollection(info.collection_name);

            Swagger_Database created_database = new Swagger_Database();
            created_database.name = extension.name;
            created_database.description = extension.description;
            created_database.id = extension.id;

            extension.setActive(true);
            extension.update();
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

            if (product.configuration != null && !product.configuration.isEmpty()) {
                String password = this.formFactory
                                      .formFromJsonWithValidation(ConfigurationProduct.class, Json.parse(product.configuration))
                                      .mongoDatabaseUserPassword;
                String baseConnectionString = MessageFormat.format(config.getString("mongoCloudAPI.connectionStringTemplate"),
                        product.id.toString(),  //login
                        password);

                List<Swagger_Database> result = extensionList.stream()
                        .map(databaseExtension -> extensionToSwaggerDatabase(databaseExtension, baseConnectionString))
                        .collect(Collectors.toList());


                return ok(result);
            }

            return ok(new ArrayList<Swagger_Database>());

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
            checkDeletePermission(productExtension);

            try {

                mongoApi.removeRole(productExtension.product.id.toString(), productExtension.id.toString());

            } catch (Exception e) {
                logger.error("drop_db: Shit happens with remove DB from Our mongo account! - Maybe its removed directly on MongoDB platform");
                logger.internalServerError(e);
            }

            productExtension.setActive(false);

            return(ok());
        } catch ( Exception e ) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Database",
            tags = {"Database"},
            notes = "Edit database",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result edit_db(UUID db_id){
        Swagger_NameAndDescription updated = formFromRequestWithValidation(Swagger_NameAndDescription.class);

        Model_ProductExtension productExtension = Model_ProductExtension.find.byId(db_id);
        checkUpdatePermission(productExtension);

        productExtension.name        = updated.name;
        productExtension.description = updated.description;

        productExtension.update();
        return ok();
    }


    @ApiOperation(
            value = "get collections",
            tags = {"Database"},
            notes = "List all collections by database id"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_DatabaseCollectionList.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_colections(UUID db_id){
        Swagger_DatabaseCollectionList collectionList = new Swagger_DatabaseCollectionList();
        Model_ProductExtension database = Model_ProductExtension.find.byId(db_id);
        checkReadPermission(database);
        try {
            collectionList.names = mongoApi.getCollections(db_id.toString());
            return ok(collectionList);
        }
        catch (Exception e) {
            return  externalServerError();
        }
    }

    @ApiOperation(
            value = "create collection",
            tags = {"Database"},
            notes = "Create collection in database"
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result create_collection(UUID db_id, String collection_name){
        Model_ProductExtension database = Model_ProductExtension.find.byId(db_id);
        checkReadPermission(database);
        mongoApi.createCollection(db_id.toString(), collection_name);
        return ok();
    }

    private Swagger_Database extensionToSwaggerDatabase(Model_ProductExtension extension, String baseConnectionString) {
        Swagger_Database result = new Swagger_Database();
        result.name = extension.name;
        result.description = extension.description;
        result.id = extension.id;
        result.connection_string = baseConnectionString + "/" + extension.id.toString();
        result.collections = mongoApi.getCollections(extension.id.toString());
        return result;
    }
}