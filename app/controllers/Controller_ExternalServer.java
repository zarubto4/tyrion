package controllers;

import com.google.inject.Inject;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.authentication.AuthenticationHomer;
import utilities.enums.CompilationStatus;
import utilities.enums.HomerType;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_C_Program_Version_Update;
import utilities.swagger.input.Swagger_CompilationServer_New;
import utilities.swagger.input.Swagger_HomerServer_Filter;
import utilities.swagger.input.Swagger_HomerServer_New;
import utilities.swagger.output.filter_results.Swagger_HomerServer_List;

import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ExternalServer extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ExternalServer.class);


// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject public Controller_ExternalServer(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }


// HOMER SERVER ########################################################################################################

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
                            dataType = "utilities.swagger.input.Swagger_HomerServer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Swagger_HomerServer_New.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result homer_server_create() {
        try {

            // Get and Validate Object
            Swagger_HomerServer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_New.class);

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.personal_server_name = help.personal_server_name;
            server.server_type = HomerType.PUBLIC;

            server.mqtt_port = help.mqtt_port;
            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.hardware_logger_port = help.hardware_logger_port;

            server.server_url = help.server_url;

            // Uložení objektu
            server.save();

            // Vrácení objektu
            return created(server.json());

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
    public Result homer_server_set_main_server(String homer_server_id) {
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

            return ok(server.json());

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
    public Result homer_server_set_backup_server(String homer_server_id) {
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

            return ok(server.json());

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
                            dataType = "utilities.swagger.input.Swagger_HomerServer_New",
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
    public Result homer_server_edit(String unique_identifier ) {
        try {

            // Get and Validate Object
            Swagger_HomerServer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_HomerServer_New.class);

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(unique_identifier);

            // Úprava objektu
            server.personal_server_name = help.personal_server_name;
            server.mqtt_port = help.mqtt_port;
            server.grid_port = help.grid_port;
            server.web_view_port = help.web_view_port;
            server.hardware_logger_port = help.hardware_logger_port;
            server.server_url = help.server_url;

            // Uložení objektu
            server.update();

            // Vrácení objektu
            return ok(server.json());

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

            if (!help.server_types.isEmpty()) {
                query.where().in("server_type", help.server_types);
            }
            if (help.project_id != null) {
                throw new Result_Error_NotSupportedException();
            }

            // Vyvoření odchozího JSON
            Swagger_HomerServer_List result = new Swagger_HomerServer_List(query, page_number);

            // Vrácení seznamu
            return ok(result.json());

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
    public Result homer_server_get(String server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(server_id);

            // Vrácení objektu
            return ok(server.json());

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
    public Result homer_server_delete(String server_id) {
        try {

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.getById(server_id);

            // Smzání objektu
            server.delete();

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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
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
            return created(server.json());

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
            @ApiResponse(code = 400, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",   response = Result_InvalidBody.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result compilation_server_edit(String server_id ) {
        try {

            // Get and Validate Object
            Swagger_CompilationServer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_CompilationServer_New.class);

            // Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(server_id);

            // Upravím objekt
            server.personal_server_name = help.personal_server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return ok(server.json());

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
            return ok(Json.toJson(Model_CompilationServer.find.all()));

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
    public Result compilation_server_get(String server_id ) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(server_id);
      
            // Vracím odpověď
            return ok(server.json());

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
    public Result compilation_server_delete(String server_id ) {
        try {

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.getById(server_id);

            // Smažu objekt
            server.delete();

            // Vracím odpověď
            return ok();

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }


// PRIVATE FILE STORAGE FOR HOMER SERVERS ###########################################################################*/

    @ApiOperation(value = "get B_Program File",
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
    public Result cloud_file_get_b_program_version(String b_program_version_id) {
        try {

            // Získám soubor
            Model_BProgramVersion version = Model_BProgramVersion.getById(b_program_version_id);

            // Separace na Container a Blob
            int slash = version.file.path.indexOf("/");
            String container_name = version.file.path.substring(0,slash);
            String real_file_path = version.file.path.substring(slash+1);

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
    public Result cloud_file_bin_get_c_program_version(String version_id) {
        try {

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            // Získám soubor
            Model_Compilation compilation = version.compilation;

            if (compilation == null) {
                return notFound("File not found");
            }

            if (compilation.status != CompilationStatus.SUCCESS) {
                return notFound("File not successfully compiled and restored");
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
    public Result cloud_file_get_c_program_compilation(String compilation_id) {
        try {

            // Získám soubor
            Model_Compilation compilation = Model_Compilation.getById(compilation_id);

            if (compilation.status != CompilationStatus.SUCCESS) {
                return notFound("File not successfully compiled and restored");
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
    public Result cloud_file_get_bootloader(String bootloader_id) {
        try {

            // Získám soubor
            Model_BootLoader bootLoader = Model_BootLoader.getById(bootloader_id);


            // Separace na Container a Blob
            int slash = bootLoader.file.path.indexOf("/");
            String container_name = bootLoader.file.path.substring(0,slash);
            String real_file_path = bootLoader.file.path.substring(slash+1);

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

            logger.debug("cloud_file_get_bootloader_version - download link: {}", total_link);

            // Přesměruji na link
            return redirect(total_link);

        } catch (Exception e) {
           return controllerServerError(e);
        }
    }
}
