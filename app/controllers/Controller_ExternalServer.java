package controllers;

import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Homer_Server;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New;
import utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New;
import utilities.swagger.outboundClass.Swagger_CompilerServer_public_Detail;

import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ExternalServer extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ExternalServer.class);
    
///###################################################################################################################*/
    
    @ApiOperation(value = "create Homer_Server",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Swagger_Cloud_Homer_Server_New.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.personal_server_name = help.personal_server_name;
            server.server_type = Enum_Cloud_HomerServer_type.public_server;

            server.mqtt_port = help.mqtt_port;
            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.server_remote_port = help.server_remote_port;

            server.server_url = help.server_url;

            // Kontrola oprávnění
            if (!server.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu
            server.save();

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Homer_Server Set Main Server ",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_set_main_server(String homer_server_id) {
        try {

            Model_HomerServer server = Model_HomerServer.get_byId(homer_server_id);
            if (server == null) return GlobalResult.result_notFound("HomerServer homer_server_id not found");

            Model_HomerServer main_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();
            if (main_server != null) return GlobalResult.result_badRequest("HomerServer Main server is already set.");

            if (!server.edit_permission()) return GlobalResult.result_forbidden();

            server.server_type = Enum_Cloud_HomerServer_type.main_server;
            server.update();

            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }
    
    @ApiOperation(value = "edit Homer_Server Set Backup Server ",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_set_backup_server(String homer_server_id) {
        try {

            Model_HomerServer server = Model_HomerServer.get_byId(homer_server_id);
            if (server == null) return GlobalResult.result_notFound("HomerServer homer_server_id not found");
            if (server.server_type != Enum_Cloud_HomerServer_type.public_server) return GlobalResult.result_badRequest("Server must be in public group!");

            Model_HomerServer backup_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.backup_server).findUnique();
            if (backup_server != null) return GlobalResult.result_badRequest("HomerServer Main server is already set.");

            if (!server.edit_permission()) return GlobalResult.result_forbidden();

            server.server_type = Enum_Cloud_HomerServer_type.backup_server;
            server.update();

            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Homer_Server",
            tags = {"External-Server"},
            notes = "Edit basic information Compilation_Server",
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
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_edit(String unique_identifier ) {
        try {

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.get_byId(unique_identifier);
            if (server == null) return GlobalResult.result_notFound("Cloud_Blocko_Server server_id not found");

            // Kontrola oprávnění
            if (!server.edit_permission()) return GlobalResult.result_forbidden();

            // Úprava objektu
            server.personal_server_name = help.personal_server_name;

            server.mqtt_port = help.mqtt_port;

            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.server_url = help.server_url;
            server.server_remote_port = help.server_remote_port;

            // Uložení objektu
            server.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Homer_Servers List",
            tags = {"Admin-External-Server"},
            notes = "get all Homer Servers",
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
            @ApiResponse(code = 200, message = "Ok Result",      response = Model_HomerServer.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_get_all() {
        try {

            // Získání seznamu
            List<Model_HomerServer> servers = Model_HomerServer.get_all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Homer_Server",
            tags = {"Admin-External-Server"},
            notes = "get all Homer Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",  response = Model_HomerServer.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_get(String server_id) {
        try {

            // Získání seznamu
            Model_HomerServer serves = Model_HomerServer.get_byId(server_id);
            if (serves == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            if (!serves.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(serves));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Homer_Server",
            tags = {"External-Server"},
            notes = "remove Compilation_Servers",
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
    @Security.Authenticated(Secured_API.class)
    public Result homer_server_delete(String server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.get_byId(server_id);
            if (server == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            // Kontrola oprávnění
            if (!server.delete_permission()) return GlobalResult.result_forbidden();

            // Smzání objektu
            server.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "Create new Gate for Compilation_Server",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result compilation_server_create() {
        try {

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Cloud_Compilation_Server_New help = form.get();

            // Vytvářím objekt
            Model_CompilationServer server = new Model_CompilationServer();
            server.personal_server_name = help.personal_server_name;
            server.server_url = help.server_url;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!server.create_permission())  return GlobalResult.result_forbidden();

            // Ukládám objekt
            server.save();

            // Vracím objekt
            return GlobalResult.result_created(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "Edit basic information Compilation_Server",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update successfuly",        response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",   response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result compilation_server_edit(String server_id ) {
        try {

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Cloud_Compilation_Server_New help = form.get();

            // Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.get_byId(server_id);
            if (server == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            // Zkontroluji oprávnění
            if (!server.edit_permission()) return GlobalResult.result_forbidden();

            // Upravím objekt
            server.personal_server_name = help.personal_server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Swagger_CompilerServer_public_Detail.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result compilation_server_get_all() {
        try {

            // Vyhledám všechny objekty
            List<Model_CompilationServer> servers = Model_CompilationServer.find.all();

            // Vylistování informací
            List<Swagger_CompilerServer_public_Detail> servers_short = new ArrayList<>();
            for (Model_CompilationServer server : servers) servers_short.add(server.get_public_info());

            // Vracím Objekty
            return GlobalResult.result_ok(Json.toJson(servers_short));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Compilation_Server",
            tags = {"Admin-External-Server"},
            notes = "get Compilation_Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result compilation_server_get(String server_id ) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.get_byId(server_id);
            if (server == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (! server.read_permission())  return GlobalResult.result_forbidden();

            // Vracím odpověď
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Compilation_Servers",
            tags = {"Admin-External-Server"},
            notes = "remove Compilation_Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result compilation_server_delete(String server_id ) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.get_byId(server_id);
            if (server == null) return GlobalResult.result_notFound("Cloud_Compilation_Server server_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (! server.delete_permission())  return GlobalResult.result_forbidden();

            // Smažu objekt
            server.delete();

            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

/// PRIVATE FILE STORAGE FOR HOMER SERVERS ###########################################################################*/

    @ApiOperation(value = "get B_Program File",
            tags = {"Homer-Server-API"},
            notes = "Required secure Token changed throw websocket",
            produces = "multipart/form-data",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses(value = {
            @ApiResponse(code = 303, message = "Ok Result"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Homer_Server.class)
    public Result cloud_file_get_b_program_version(String b_program_version_id) {
        try {

            // Získám soubor
            Model_VersionObject version_object = Model_VersionObject.get_byId(b_program_version_id);

            if (version_object== null) {
               return GlobalResult.result_notFound("File not found");
            }

            if (version_object.get_b_program() == null) {
                return GlobalResult.result_forbidden();
            }

            // Separace na Container a Blob
            int slash = version_object.files.get(0).file_path.indexOf("/");
            String container_name = version_object.files.get(0).file_path.substring(0,slash);
            String real_file_path = version_object.files.get(0).file_path.substring(slash+1);

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

            terminal_logger.warn("cloud_file_get_b_program_version - download link: {}", total_link);

            // Přesměruji na link
            return GlobalResult.redirect(total_link);

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 303, message = "Ok Result"),
            @ApiResponse(code = 404, message = "File by ID not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result cloud_file_bin_get_c_program_version(String version_id) {
        try {

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if (version_object.get_c_program() == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

            if (!version_object.get_c_program().read_permission()) return GlobalResult.result_forbidden();

            // Získám soubor
            Model_CCompilation compilation = version_object.c_compilation;

            if (compilation == null) {
                return GlobalResult.result_notFound("File not found");
            }

            if (compilation.status != Enum_Compile_status.successfully_compiled_and_restored) {
                return GlobalResult.result_notFound("File not successfully compiled and restored");
            }

            byte[] bytes = Model_FileRecord.get_decoded_binary_string_from_Base64(compilation.bin_compilation_file.get_fileRecord_from_Azure_inString());

            // Vrátím soubor
            return GlobalResult.result_binFile(bytes, "firmware.bin");

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 303, message = "Ok Result"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Homer_Server.class)
    public Result cloud_file_get_c_program_compilation(String compilation_id) {
        try {

            // Získám soubor
            Model_CCompilation compilation = Model_CCompilation.find.byId(compilation_id);

            if (compilation == null) {
                return GlobalResult.result_notFound("File not found");
            }

            if (compilation.status != Enum_Compile_status.successfully_compiled_and_restored) {
                return GlobalResult.result_notFound("File not successfully compiled and restored");
            }

            // Separace na Container a Blob
            int slash = compilation.bin_compilation_file.file_path.indexOf("/");
            String container_name = compilation.bin_compilation_file.file_path.substring(0,slash);
            String real_file_path = compilation.bin_compilation_file.file_path.substring(slash+1);

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

            terminal_logger.debug("cloud_file_get_c_program_version - download link: {}", total_link);

            // Přesměruji na link
            return GlobalResult.redirect(total_link);

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 303, message = "Ok Result"),
            @ApiResponse(code = 404, message = "File by ID not found",response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Homer_Server.class)
    public Result cloud_file_get_bootloader(String bootloader_id) {
        try {

            // Získám soubor
            Model_BootLoader bootLoader = Model_BootLoader.get_byId(bootloader_id);

            if (bootLoader == null) {
                return GlobalResult.result_notFound("File not found");
            }

            // Separace na Container a Blob
            int slash = bootLoader.file.file_path.indexOf("/");
            String container_name = bootLoader.file.file_path.substring(0,slash);
            String real_file_path = bootLoader.file.file_path.substring(slash+1);

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

            terminal_logger.debug("cloud_file_get_bootloader_version - download link: {}", total_link);

            // Přesměruji na link
            return GlobalResult.redirect(total_link);

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }
}
