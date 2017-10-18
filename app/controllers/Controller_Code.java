package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import utilities.emails.Email;
import utilities.enums.Enum_Approval_state;
import utilities.enums.Enum_Compile_status;
import utilities.enums.Enum_Publishing_type;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Homer_Server;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_List;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_File_Link;

import java.util.*;

@Security.Authenticated(Secured_API.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Code extends Controller{

// LOGGER ##############################################################################################################
    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Board.class);


// C_ Program && Version ###############################################################################################

    @ApiOperation(value = "create C_Program",
            tags = {"C_Program"},
            notes = "If you want create new C_Program in project.id = {project_id}. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.create_permission", value = Model_CProgram.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "C_Program_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_create() {
        try {

            // Zpracování Json
            final Form<Swagger_C_Program_New> form = Form.form(Swagger_C_Program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_New help = form.get();

            // Ověření Typu Desky
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            // Tvorba programu
            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.type_of_board         = typeOfBoard;
            c_program.publish_type          = Enum_Publishing_type.private_program;

            if(help.project_id != null){
                // Ověření projektu
                Model_Project project = Model_Project.get_byId(help.project_id);
                if (project == null) return GlobalResult.result_notFound("Project project_id not found");
                c_program.project = project;
            }

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!c_program.create_permission()) return GlobalResult.result_forbidden();

            // Uložení C++ Programu
            c_program.save();
            c_program.refresh();

            // Přiřadím první verzi!
            if (typeOfBoard.get_main_c_program() != null && typeOfBoard.get_main_c_program().default_main_version != null) {

                System.out.println("Mám zde výchozí program!");

                Model_VersionObject version_object = new Model_VersionObject();
                version_object.version_name = "1.0.1";
                version_object.version_description = typeOfBoard.get_main_c_program().description;
                version_object.author = Controller_Security.get_person();
                version_object.date_of_create = new Date();
                version_object.c_program = c_program;
                version_object.public_version = help.c_program_public_admin_create;

                // Zkontroluji oprávnění
                if (!version_object.c_program.update_permission()) return GlobalResult.result_forbidden();

                version_object.save();

                for (Model_FileRecord file : typeOfBoard.get_main_c_program().default_main_version.files) {

                    JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                    Form<Swagger_C_Program_Version_Update> scheme_form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
                    if (form.hasErrors()) {
                        terminal_logger.internalServerError(new Exception("Error loading first default version of CProgram."));
                        break;
                    }
                    Swagger_C_Program_Version_Update scheme_load_form = scheme_form.get();

                    // Nahraje do Azure a připojí do verze soubor
                    ObjectNode content = Json.newObject();
                    content.put("main", scheme_load_form.main);
                    content.set("files", Json.toJson(scheme_load_form.files));
                    content.set("imported_libraries", Json.toJson(scheme_load_form.imported_libraries));

                    // Content se nahraje na Azure
                    Model_FileRecord.uploadAzure_Version(content.toString(), "code.json", c_program.get_path(), version_object);
                    version_object.update();
                }

                version_object.compile_program_thread();

            }

            c_program.refresh();

            return GlobalResult.result_created(Json.toJson(c_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make_Clone C_Program",
            tags = {"C_Program"},
            notes = "clone C_Program for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_C_Program_Copy> form = Form.form(Swagger_C_Program_Copy.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Copy help = form.get();

            // Vyhledám Objekt
            Model_CProgram c_program_old = Model_CProgram.get_byId(help.c_program_id);
            if(c_program_old == null) return GlobalResult.result_notFound("C_Program c_program not found");

            // Zkontroluji oprávnění
            if(!c_program_old.read_permission())  return GlobalResult.result_forbidden();

            // Vyhledám Objekt
            Model_Project project = Model_Project.get_byId(help.project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if(!project.update_permission())  return GlobalResult.result_forbidden();


            Model_CProgram c_program_new =  new Model_CProgram();
            c_program_new.name = help.name;
            c_program_new.description = help.description;
            c_program_new.type_of_board = c_program_old.get_type_of_board();
            c_program_new.project = project;
            c_program_new.save();

            c_program_new.refresh();


            for(Model_VersionObject version : c_program_old.getVersion_objects()){


                Model_VersionObject copy_object = new Model_VersionObject();
                copy_object.version_name        = version.version_name;
                copy_object.date_of_create      = version.date_of_create;
                copy_object.version_description = version.version_description;
                copy_object.date_of_create      = new Date();
                copy_object.c_program           = c_program_new;
                copy_object.public_version      = false;
                copy_object.author              = version.author;

                // Zkontroluji oprávnění
                copy_object.save();

                // Překopíruji veškerý obsah
                Model_FileRecord fileRecord = version.files.get(0);


                Model_FileRecord.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program_new.get_path() ,  copy_object);
                copy_object.update();

                copy_object.compile_program_thread();

            }

            c_program_new.refresh();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(c_program_new));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
    
    @ApiOperation(value = "get C_Program",
            tags = {"C_Program"},
            notes = "get C_Program by query = c_program_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.read_permission", value = Model_CProgram.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_get(String c_program_id) {
        try {

            // Vyhledám Objekt
            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);
            if(c_program == null) return GlobalResult.result_notFound("C_Program c_program not found");

            // Zkontroluji oprávnění
            if(! c_program.read_permission())  return GlobalResult.result_forbidden();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(c_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_Program List by Filter",
            tags = {"C_Program"},
            notes = "get all C_Programs that belong to logged person",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check person_permissions"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_Program_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number){

        try {

            // Získání JSON
            final Form<Swagger_C_Program_Filter> form = Form.form(Swagger_C_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);

            query.orderBy("UPPER(name) ASC");


            // Pokud JSON obsahuje project_id filtruji podle projektu
            if((help.project_id != null) && !(help.project_id.equals(""))){

                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null )return GlobalResult.result_notFound("Project not found");
                if(!project.read_permission())return GlobalResult.result_forbidden();

                query.where().eq("project.id", help.project_id).eq("removed_by_user", false);
            }

            if(!help.type_of_board_ids.isEmpty()){
               query.where().in("type_of_board.id", help.type_of_board_ids);
            }


            if(help.public_programs){
                query.where().isNull("project").eq("removed_by_user", false).eq("publish_type", Enum_Publishing_type.public_program.name());
            }

            if(help.pending_programs){
                if(!Controller_Security.get_person().has_permission(Model_CProgram.permissions.C_Program_community_publishing_permission.name())) return GlobalResult.result_forbidden();
                query.where().eq("version_objects.approval_state", Enum_Approval_state.pending.name());
            }

            // Vyvoření odchozího JSON
            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit C_Program",
            tags = {"C_Program"},
            notes = "If you want edit base information about C_Program by  query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_edit( String c_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_C_Program_Edit> form = Form.form(Swagger_C_Program_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Edit help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);
            if(c_program == null ) return GlobalResult.result_notFound("C_Program c_program_id not found");

            // Úprava objektu
            c_program.name = help.name;
            c_program.description = help.description;

            // Zkontroluji oprávnění
            if(!c_program.edit_permission())  return GlobalResult.result_forbidden();

            // Uložení změn
            c_program.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(c_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete C_Program",
            tags = {"C_Program"},
            notes = "delete C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_delete( String c_program_id){
        try{

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);
            if(c_program == null ) return GlobalResult.result_notFound("C_Program c_program_id not found");

            // Kontrola oprávnění
            if(!c_program.delete_permission()) return GlobalResult.result_forbidden();

            // Vyhledání PRoduct pro získání kontejneru
            //Model_Product product = Model_Product.find.where().eq("projects.c_programs.id", c_program_id).findUnique();


            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create C_Program_Version",
            tags = {"C_Program"},
            notes = "If you want add new code to C_Program by query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_update"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_create(@ApiParam(value = "version_id String query", required = true)  String c_program_id){
        try{

            // Zpracování Json
            Form<Swagger_C_Program_Version_New> form = Form.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);
            if(c_program == null) return GlobalResult.result_notFound("C_Program c_program_id not found");

            // Zkontroluji oprávnění
            if(!c_program.update_permission()) return GlobalResult.result_forbidden();

            // První nová Verze
            Model_VersionObject version_object = new Model_VersionObject();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.author              = Controller_Security.get_person();
            version_object.date_of_create      = new Date();
            version_object.c_program           = c_program;
            version_object.public_version      = false;

            // Zkontroluji oprávnění
            if(!version_object.c_program.update_permission()) return GlobalResult.result_forbidden();

            version_object.save();

            // Content se nahraje na Azure
            Model_FileRecord.uploadAzure_Version(Json.toJson(help).toString(), "code.json" , c_program.get_path() ,  version_object);
            version_object.update();

            version_object.compile_program_thread();

            // Vracím vytvořený objekt
            return GlobalResult.result_created(Json.toJson(c_program.program_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_Program_Version",
            tags = {"C_Program"},
            notes = "get Version of C_Program by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if(version_object == null) return GlobalResult.result_notFound("Version_Object version_object not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

            // Zkontroluji oprávnění
            if (!version_object.c_program.read_permission())  return GlobalResult.result_forbidden();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(version_object.c_program.program_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit C_Program_Version information",
            tags = {"C_Program"},
            notes = "For update basic (name and description) information in Version of C_Program. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_VersionObject.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_edit(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();

            // Ověření objektu
            Model_VersionObject version_object= Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Kontrola oprávnění
            if(!version_object.c_program.edit_permission()) return GlobalResult.result_forbidden();

            //Uprava objektu
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            // Uložení změn
            version_object.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete C_Program_Version",
            tags = {"C_Program"},
            notes = "delete Version.id = version_id in C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id){
        try{

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.delete_permission()) return GlobalResult.result_forbidden();

            // Smažu zástupný objekt
            version_object.delete();

            // Vracím potvrzení o smazání
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_Program_Version Binary DownloadLink",
            tags = {"C_Program"},
            notes = "Required secure Token changed throw websocket",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_File_Link.class),
            @ApiResponse(code = 404, message = "File by ID not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission or File is not probably right type",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result cloud_file_get_c_program_version(String version_id){
        try{

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

           if(!version_object.c_program.read_permission()) return GlobalResult.result_forbidden();

            // Získám soubor
            Model_CCompilation compilation = version_object.c_compilation;

            if(compilation == null){
                return GlobalResult.result_notFound("File not found");
            }


            if(compilation.status != Enum_Compile_status.successfully_compiled_and_restored){
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
            cal.add(Calendar.MINUTE, 30);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());


            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            terminal_logger.debug("cloud_file_get_c_program_version:: Total Link:: " + total_link);

            Swagger_File_Link link = new Swagger_File_Link();
            link.file_link = total_link;

            // Přesměruji na link
            return GlobalResult.result_ok(Json.toJson(link));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make C_Program_Version public",
            tags = {"C_Program"},
            notes = "Make C_Program public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_edit"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Bad Request",               response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_make_public(@ApiParam(value = "version_id String query", required = true)  String version_id){
        try {

            // Kontrola objektu
            Model_VersionObject version = Model_VersionObject.get_byId(version_id);
            if(version == null) return GlobalResult.result_notFound("Version not found");

            if(version.c_program == null )return GlobalResult.result_notFound("Version not found");


            if(Model_VersionObject.find.where().eq("approval_state", Enum_Approval_state.pending.name())
                    .eq("c_program.project.participants.person.id", Controller_Security.get_person_id())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return GlobalResult.result_badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if(version.approval_state != null)  return GlobalResult.result_badRequest("You cannot publish same program twice!");

            // Úprava objektu
            version.approval_state = Enum_Approval_state.pending;

            // Kontrola oprávnění
            if(!(version.c_program.edit_permission())) return GlobalResult.result_forbidden();

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit C_Program_Version Response publication",
            tags = {"Admin-C_Program"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Community_Version_Publish_Response",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_public_response(){
        try {

            // Získání Json
            final Form<Swagger_Community_Version_Publish_Response> form = Form.form(Swagger_Community_Version_Publish_Response.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Community_Version_Publish_Response help = form.get();

            // Kontrola objektu
            Model_VersionObject version_old = Model_VersionObject.get_byId(help.version_id);
            if(version_old == null) return GlobalResult.result_notFound("Version not found");

            if(version_old.c_program == null){
                return GlobalResult.result_notFound("C_Program c_program_id not found");
            }

            // Ověření objektu
            Model_CProgram c_program_old = Model_CProgram.get_byId(version_old.c_program.id);


            // Zkontroluji oprávnění
            if(!c_program_old.community_publishing_permission()){
                return GlobalResult.result_forbidden();
            }

            if(help.decision){

                System.out.println("help.decision je true!!!");

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Enum_Approval_state.approved;
                version_old.update();


                Model_CProgram c_program = Model_CProgram.find.byId(c_program_old.id + "_public_copy");

                if(c_program == null) {
                    c_program = new Model_CProgram();
                    c_program.id = c_program_old.id + "_public_copy";
                    c_program.name = help.program_name;
                    c_program.description = help.program_description;
                    c_program.date_of_create = new Date();
                    c_program.type_of_board = c_program_old.type_of_board;
                    c_program.publish_type  = Enum_Publishing_type.public_program;
                    c_program.save();
                }



                Model_VersionObject version_object = new Model_VersionObject();
                version_object.version_name        = help.version_name;
                version_object.version_description = help.version_description;
                version_object.date_of_create      = new Date();
                version_object.c_program           = c_program;
                version_object.public_version      = true;
                version_object.author              = version_old.author;

                // Zkontroluji oprávnění
                version_object.save();

                c_program.refresh();

                // Překopíruji veškerý obsah
                Model_FileRecord fileRecord = version_old.files.get(0);


                Model_FileRecord.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program.get_path() ,  version_object);
                version_object.update();

                version_object.compile_program_thread();

                // Admin to schválil bez dalších keců
                if((help.reason == null || help.reason.length() < 4) ){
                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("C Program Name: ") +        c_program_old.name + Email.newLine() +
                                        Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +          c_program_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") +   c_program_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible.")
                                .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.get_person().full_name)
                                .send(version_old.c_program.get_project().get_product().customer, "Publishing your program" );

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }

                // Admin to schválil ale měl nějaký keci k tomu
                }else {

                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("C Program Name: ") +        c_program_old.name + Email.newLine() +
                                        Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +          c_program_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") +   c_program_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible. We also had to make some changes to your program or rename something.")
                                .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                                .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.get_person().full_name)
                                .send(version_old.c_program.get_project().get_product().customer, "Publishing your program" );

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

            }else {

                version_old.approval_state = Enum_Approval_state.disapproved;
                version_old.update();

                try {

                    new Email()
                            .text("First! Thank you for publishing your program!")
                            .text(Email.bold("C Program Name: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("Version Name: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("Version Description: ") + c_program_old.name + Email.newLine())
                            .divider()
                            .text("We are sorry, but we found some problems in your program, so we did not publish it. But do not worry and do not give up! " +
                                    "We are glad that you want to contribute to our public libraries. Here are some tips what to improve, so you can try it again.")
                            .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                            .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.get_person().full_name)
                            .send(version_old.c_program.get_project().get_product().customer, "Publishing your program");

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }
            }

            // Potvrzení
            return  GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "set_c_program_version_as_main Type_of_board",
            tags = {"Admin-C_Program, Type-Of-Board"},
            notes = "set C_Program version as Main for This Type of Device. Version must be from Main or Test C Program of this version",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result c_program_markScheme(@ApiParam(value = "version_id", required = true) String version_id) {
        try {

            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version_Object version_object_id not found");

            if (version_object.c_program == null || (version_object.c_program.type_of_board_default == null && version_object.c_program.type_of_board_test == null)) return GlobalResult.result_badRequest("Version_object is not version of c_program or is not default firmware");

            // Kontrola oprávnění
            if(!version_object.c_program.edit_permission()) return GlobalResult.result_forbidden();

            Model_VersionObject previous_main_version = Model_VersionObject.find.where().eq("c_program.id", version_object.c_program.id).isNotNull("default_program").findUnique();

            if (previous_main_version != null){

                previous_main_version.default_program = null;
                version_object.c_program.default_main_version  = null;
                previous_main_version.update();
                version_object.c_program.update();
            }

            version_object.default_program = version_object.c_program;
            version_object.update();

            version_object.c_program.refresh();

            // Vracím Json
            return GlobalResult.result_ok(Json.toJson(version_object.c_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
