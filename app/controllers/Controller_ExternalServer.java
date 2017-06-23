package controllers;

import io.swagger.annotations.*;
import models.Model_HomerServer;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_InvalidBody;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Ok;
import utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New;

import java.util.List;

@Security.Authenticated(Secured_API.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ExternalServer extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ExternalServer.class);
    
///###################################################################################################################*/
    
    @ApiOperation(value = "Create new Blocko Server",
            tags = {"External Server"},
            notes = "Create new Gate for Blocko Server",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.create_permission", value = Model_HomerServer.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_creat" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Homer_Server(){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.personal_server_name = help.personal_server_name;
            server.server_type = Enum_Cloud_HomerServer_type.public_server;

            // Kontrola oprávnění
            if(!server.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu
            server.save();

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(server));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Homer server - set main server", hidden = true)
    public Result set_main_server(String homer_server_id){
        try{

            Model_HomerServer server = Model_HomerServer.find.byId(homer_server_id);
            if(server == null) return GlobalResult.result_notFound("HomerServer homer_server_id not found");

            Model_HomerServer main_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();
            if(main_server != null) return GlobalResult.result_badRequest("HomerServer Main server is already set.");

            if(!server.edit_permission()) return GlobalResult.result_forbidden();

            server.server_type = Enum_Cloud_HomerServer_type.main_server;
            server.update();

            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Homer server - set main server", hidden = true)
    public Result set_backup_server(String homer_server_id){
        try{

            Model_HomerServer server = Model_HomerServer.find.byId(homer_server_id);
            if(server == null) return GlobalResult.result_notFound("HomerServer homer_server_id not found");
            if(server.server_type != Enum_Cloud_HomerServer_type.public_server) return GlobalResult.result_badRequest("Server must be in public group!");


            Model_HomerServer backup_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.backup_server).findUnique();
            if(backup_server != null) return GlobalResult.result_badRequest("HomerServer Main server is already set.");

            if(!server.edit_permission()) return GlobalResult.result_forbidden();

            server.server_type = Enum_Cloud_HomerServer_type.backup_server;
            server.update();

            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Compilation Server",
            tags = {"External Server"},
            notes = "Edit basic information Compilation Server",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_edit" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Homer_Server(@ApiParam(value = "unique_identifier ", required = true) String unique_identifier ){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.get_byId(unique_identifier);
            if (server == null) return GlobalResult.result_notFound("Cloud_Blocko_Server server_id not found");

            // Kontrola oprávnění
            if(!server.edit_permission()) return GlobalResult.result_forbidden();

            // Úprava objektu
            server.personal_server_name = help.personal_server_name;

            server.mqtt_port = help.mqtt_port;
            server.mqtt_password = help.mqtt_password;
            server.mqtt_username = help.mqtt_username;

            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.server_url = help.server_url;

            // Uložení objektu
            server.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Blocko Servers",
            tags = {"External Server"},
            notes = "get all Blocko Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = Model_HomerServer.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_read")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Model_HomerServer.class, responseContainer = "List "),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Homer_Server(){
        try{

            // Získání seznamu
            List<Model_HomerServer> servers = Model_HomerServer.get_all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Compilation Servers",
            tags = {"External Server"},
            notes = "remove Compilation Servers",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_delete")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Homer_Server(@ApiParam(value = "unique_identificator ", required = true)  String unique_identificator ){
        try{

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.get_byId(unique_identificator);
            if (server == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            // Kontrola oprávnění
            if(!server.delete_permission()) return GlobalResult.result_forbidden();

            // Smzání objektu
            server.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

}
