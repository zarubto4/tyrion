package controllers;

import com.google.inject.Inject;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.typesafe.config.Config;
import io.ebean.*;
import io.swagger.annotations.*;
import models.*;
import org.omg.CORBA.ExceptionList;
import play.api.http.HttpEntity;
import play.api.libs.ws.WSBodyWritables;
import play.libs.Json;
import play.libs.ws.BodyReadable;
import play.libs.ws.StandaloneWSResponse;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.ResponseHeader;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.authentication.AuthenticationHomer;
import utilities.enums.CompilationStatus;
import utilities.enums.HardwareUpdateState;
import utilities.enums.HomerType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.homer_auto_deploy.SelfDeployedThreadRegister;
import utilities.homer_auto_deploy.models.common.Swagger_ServerRegistration_FormData;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_HomerServer_List;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ExternalServer extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ExternalServer.class);


// CONTROLLER CONFIGURATION ############################################################################################
    private WSClient ws;
    private Config config;
    private _BaseFormFactory baseFormFactory;

    @Inject public Controller_ExternalServer(WSClient ws, Config config, _BaseFormFactory formFactory) {
        this.ws = ws;
        this.config = config;
        this.baseFormFactory = formFactory;
    }


// HOMER SERVER ########################################################################################################

    @ApiOperation(value = "get Homer_Server Registration Components",
            tags = {"External-Server"},
            notes = "Get All data for User registration form in Portal",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",    response = Swagger_ServerRegistration_FormData.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result get_registration_data() {
        try {

            Swagger_ServerRegistration_FormData data = DigitalOceanTyrionService.get_data();

            // Vrácení objektu
            return ok(data);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Homer_Server Automatically",
            tags = {"External-Server"},
            notes = "Create new Homer_Server - private or public",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HomerServer_New_Auto",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_create_automaticaly() {
        try {

            // Get and Validate Object
            Swagger_HomerServer_New_Auto help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_New_Auto.class);

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.name = help.name;
            server.description = help.description;


            if(help.project_id == null) {
                server.server_type = HomerType.PUBLIC;
            }else {
                server.server_type = HomerType.PRIVATE;
                server.project =  Model_Project.getById(help.project_id);
            }

            server.save();

            server.setTags(help.tags);


            DigitalOceanTyrionService.create_server(server, help.size_slug, help.region_slug);

            // Vrácení objektu
            return created(server);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Homer_Server Manually",
            tags = {"External-Server"},
            notes = "Create new Homer_Server - private or public",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HomerServer_New_Manually",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_create_manualy() {
        try {

            // Get and Validate Object
            Swagger_HomerServer_New_Manually help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_New_Manually.class);

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.name = help.name;
            server.description = help.description;

            server.mqtt_port = help.mqtt_port;
            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.hardware_logger_port = help.hardware_logger_port;
            server.rest_api_port = help.rest_api_port;

            server.server_url = help.server_url;

            server.server_type = HomerType.PUBLIC;


            if(help.project_id == null) {
                server.server_type = HomerType.PUBLIC;
            }else {
                server.server_type = HomerType.PRIVATE;
                server.project =  Model_Project.getById(help.project_id);;
            }

            // Uložení objektu
            server.save();

            server.setTags(help.tags);

            new SelfDeployedThreadRegister(server).start();

            // Vrácení objektu
            return created(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Homer_Server Set Main Server ",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_set_main_server(UUID homer_server_id) {
        try {

            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);
            if (server.server_type != HomerType.PUBLIC) return badRequest("Server must be in public group!");

            Model_HomerServer main_server_not_cached = Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).select("id").findOne();
            if(main_server_not_cached != null) {
                Model_HomerServer main_server = Model_HomerServer.getById(main_server_not_cached.id);
                main_server.server_type = HomerType.PUBLIC;
                main_server.update();
            }

            server.server_type = HomerType.MAIN;
            server.update();

            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "edit Homer_Server Set Backup Server ",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",      response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_set_backup_server(UUID homer_server_id) {
        try {

            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);
            if (server.server_type != HomerType.PUBLIC) return badRequest("Server must be in public group!");

            Model_HomerServer main_server_not_cached = Model_HomerServer.find.query().where().eq("server_type", HomerType.BACKUP).select("id").findOne();
            if(main_server_not_cached != null) {
                Model_HomerServer main_server = Model_HomerServer.getById(main_server_not_cached.id);
                main_server.server_type = HomerType.PUBLIC;
                main_server.update();
            }

            server.server_type = HomerType.BACKUP;
            server.update();

            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Homer_Server",
            tags = {"External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HomerServer_New_Manually",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_edit(UUID homer_server_id) {
        try {

            // Get and Validate Object
            Swagger_HomerServer_New_Manually help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_New_Manually.class);

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            // Úprava objektu
            server.name = help.name;
            server.description = help.description;
            server.mqtt_port = help.mqtt_port;
            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.hardware_logger_port = help.hardware_logger_port;
            server.rest_api_port = help.rest_api_port;
            server.server_url = help.server_url;
            server.setTags(help.tags);

            // Uložení objektu
            server.update();

            // Vrácení objektu
            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Homer_Servers List",
            tags = {"Admin-External-Server"},
            notes = "get all Homer Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HomerServer_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_HomerServer_List.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result homer_server_by_filter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number) {
        try {

            // Get and Validate Object
            Swagger_HomerServer_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_HomerServer> query = Ebean.find(Model_HomerServer.class);

            query.orderBy("UPPER(name) ASC");
            query.orderBy("project.id");
            query.where().eq("deleted", false);

            ExpressionList<Model_HomerServer> list = query.where();

            // Junction!!! START ---------------------------------------------------------------------------------------

            // OR
            Junction<Model_HomerServer>  disjunction = list.disjunction();

            // AND && END AND
            if (!help.server_types.isEmpty() && help.server_types.contains(HomerType.PUBLIC) ) {
                disjunction
                        .conjunction()
                            .eq("server_type", HomerType.PUBLIC)
                            .isNull("project.id")
                        .endJunction();
            }

            if (!help.server_types.isEmpty() && help.server_types.contains(HomerType.MAIN) ) {
                disjunction
                        .conjunction()
                            .eq("server_type", HomerType.MAIN)
                            .isNull("project.id")
                        .endJunction();
            }

            if (!help.server_types.isEmpty() && help.server_types.contains(HomerType.BACKUP) ) {
                disjunction
                        .conjunction()
                            .eq("server_type", HomerType.BACKUP)
                            .isNull("project.id")
                        .endJunction();
            }

            // AND && END AND
            if (!help.server_types.isEmpty() && help.server_types.contains(HomerType.PRIVATE) ) {
                disjunction
                        .conjunction()
                            .eq("server_type", HomerType.PRIVATE)
                            .eq("project.id",  help.project_id)
                        .endJunction();
            }

            // END OR
            disjunction.endJunction();


            // Vyvoření odchozího JSON
            Swagger_HomerServer_List result = new Swagger_HomerServer_List(query, page_number, help);

            // Vrácení seznamu
            return ok(result);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Homer_Server",
            tags = {"Admin-External-Server"},
            notes = "get all Homer Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HomerServer.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result homer_server_get(UUID homer_server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            // Vrácení objektu
            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Homer_Server",
            tags = {"External-Server"},
            notes = "remove Compilation_Servers",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result homer_server_delete(UUID homer_server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            // Smzání objektu
            server.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "shut_down Homer_Server",
            tags = {"External-Server"},
            notes = "Shut Down Virtual Homer_Server if its supported",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result homer_server_power_down(UUID homer_server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            server.check_update_permission();

            DigitalOceanTyrionService.powerOff(server);

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "start Homer_Server",
            tags = {"External-Server"},
            notes = "Start Virtual Homer_Server machine if its supported",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result homer_server_power_on(UUID homer_server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            server.check_update_permission();

            DigitalOceanTyrionService.powerOn(server);

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "restart Homer_Server",
            tags = {"External-Server"},
            notes = "Restart Virtual Homer_Server machine if its supported",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result homer_server_restart(UUID homer_server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(homer_server_id);

            server.check_update_permission();

            DigitalOceanTyrionService.restartServer(server);

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }



// COMPILATION SERVER ##################################################################################################

    @ApiOperation(value = "create Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "Create new Gate for Compilation_Server",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_CompilationServer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result compilation_server_create() {
        try {

            // Get and Validate Object
            Swagger_CompilationServer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_CompilationServer_New.class);

            // Vytvářím objekt
            Model_CompilationServer server = new Model_CompilationServer();
            server.personal_server_name = help.personal_server_name;
            server.server_url = help.server_url;

            // Ukládám objekt
            server.save();

            // Vracím objekt
            return created(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_CompilationServer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Update successfuly",        response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result compilation_server_edit(UUID compilation_server_id) {
        try {

            // Get and Validate Object
            Swagger_CompilationServer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_CompilationServer_New.class);

            // Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(compilation_server_id);

            // Upravím objekt
            server.personal_server_name = help.personal_server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Compilation_Servers List",
            tags = {"Admin-External-Server"},
            notes = "get Compilation_Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_CompilationServer.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result compilation_server_get_all() {
        try {

            // Vracím Objekty
            return ok(Model_CompilationServer.find.all());

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "get Compilation_Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result compilation_server_get(UUID compilation_server_id) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(compilation_server_id);
      
            // Vracím odpověď
            return ok(server);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Compilation_Servers",
            tags = {"Admin-External-Server"},
            notes = "remove Compilation_Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result compilation_server_delete(UUID compilation_server_id) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(compilation_server_id);

            // Smažu objekt
            server.delete();

            // Vracím odpověď
            return ok();

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

// PRIVATE FILE STORAGE FOR HOMER SERVERS ###########################################################################*/

    @ApiOperation(value = "get B_Program Instance Snapshost File",
            tags = {"Homer-Server-API"},
            notes = "Required secure Token changed throw websocket",
            produces = "multipart/form-data",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(AuthenticationHomer.class)
    public Result cloud_file_get_b_program_version(UUID snapshot_id) {
        try {

            System.out.println("cloud_file_get_b_program_version");

            // Získám soubor
            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);

            System.out.println("OK 1");

            // Separace na Container a Blob
            int slash = snapshot.program.path.indexOf("/");
            String container_name = snapshot.program.path.substring(0,slash);
            String real_file_path = snapshot.program.path.substring(slash+1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.SECOND, 30);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            logger.warn("cloud_file_get_b_program_version - download link: {}", total_link);

            // Přesměruji na link
            return redirect(total_link);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program_Version Binary DownloadLink",
            tags = {"C_Program"},
            notes = "Required secure Token changed throw websocket",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 404, message = "File by ID not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result cloud_file_bin_get_c_program_version(UUID version_id) {
        try {

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            // Získám soubor
            Model_Compilation compilation = version.compilation;

            if (compilation == null) {
                throw new Result_Error_NotFound(Model_Compilation.class);
            }

            if (compilation.status != CompilationStatus.SUCCESS) {
                throw new Result_Error_NotFound(Model_Blob.class);
            }

            byte[] bytes = Model_Blob.get_decoded_binary_string_from_Base64(compilation.blob.get_fileRecord_from_Azure_inString());

            // Vrátím soubor
            return file(bytes, "firmware.bin");

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program File",
            tags = {"Homer-Server-API"},
            notes = "Required secure Token changed throw websocket",
            produces = "multipart/form-data",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(AuthenticationHomer.class)
    public Result cloud_file_get_c_program_compilation(UUID compilation_id) {
        try {

            // Získám soubor
            Model_Compilation compilation = Model_Compilation.getById(compilation_id);

            if (compilation.status != CompilationStatus.SUCCESS) {
                throw new Result_Error_NotFound(Model_Blob.class);
            }

            // Separace na Container a Blob
            int slash = compilation.blob.path.indexOf("/");
            String container_name = compilation.blob.path.substring(0,slash);
            String real_file_path = compilation.blob.path.substring(slash+1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.SECOND, 30);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            logger.debug("cloud_file_get_c_program_version - download link: {}", total_link);

            // Přesměruji na link
            return redirect(total_link);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Bootloader File",
            tags = {"Homer-Server-API"},
            notes = "Required secure Token changed throw websocket",
            produces = "multipart/form-data",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(AuthenticationHomer.class)
    public Result cloud_file_get_bootloader(UUID bootloader_id) {
        try {


            logger.trace("cloud_file_get_bootloader - download id: {}", bootloader_id);

            // Získám soubor
            Model_BootLoader bootLoader = Model_BootLoader.getById(bootloader_id);

            logger.trace("cloud_file_get_bootloader - Bootloader: {}", bootLoader.version_identifier);
            logger.trace("cloud_file_get_bootloader - File Path: {}",  bootLoader.file.path);

            int slash = bootLoader.file.path.indexOf("/");
            String container_name = bootLoader.file.path.substring(0,slash);
            String real_file_path = bootLoader.file.path.substring(slash+1);

            logger.trace("cloud_file_get_bootloader - Container Name {} real_file_path {} ", container_name, real_file_path );

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 250);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            logger.trace("cloud_file_get_bootloader_version - download link: {}", total_link);

            // Přesměruji na link
            return redirect(total_link);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }
}
