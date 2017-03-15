package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.Model_FileRecord;
import models.Model_TypeOfBoard;
import models.Model_VersionObject;
import models.Model_CProgram;
import models.Model_Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.emails.Email;
import utilities.enums.Approval_state;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_Version_Public_List;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_For_Public_Decision;

import java.util.Date;
import java.util.List;

@Security.Authenticated(Secured_API.class)
public class Controller_Code extends Controller{

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// C_ Program && Version ###############################################################################################

    @ApiOperation(value = "Create new C_Program",
            tags = {"C_Program"},
            notes = "If you want create new C_program in project.id = {project_id}. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.create_permission", value = Model_CProgram.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "C_program_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created", response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_create() {
        try {

            // Zpracování Json
            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Ověření Typu Desky
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Tvorba programu
            Model_CProgram c_program             = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.date_of_create        = new Date();
            c_program.type_of_board         = typeOfBoard;

            if(help.project_id != null){
                // Ověření projektu
                Model_Project project = Model_Project.find.byId(help.project_id);
                if (project == null) return GlobalResult.notFoundObject("Project project_id not found");
                c_program.project = project;
            }

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!c_program.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení C++ Programu
            c_program.save();
            c_program.refresh();

            // Přiřadím první verzi!
            if (typeOfBoard.version_scheme != null && typeOfBoard.version_scheme.default_main_version != null) {

                Model_VersionObject version_object = new Model_VersionObject();
                version_object.version_name = "First default version of C_Program.";
                version_object.version_description = "This is default description.";
                version_object.author = Controller_Security.getPerson();
                version_object.date_of_create = new Date();
                version_object.c_program = c_program;
                version_object.public_version = (help.c_program_public_admin_create && Controller_Security.getPerson().admin_permission());

                // Zkontroluji oprávnění
                if (!version_object.c_program.update_permission()) return GlobalResult.forbidden_Permission();

                version_object.save();

                for (Model_FileRecord file : typeOfBoard.version_scheme.default_main_version.files) {

                    JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                    Form<Swagger_C_Program_Version_Update> scheme_form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
                    if (form.hasErrors()) {
                        logger.error("Code:: c_program_create:: Error loading first default version of CProgram");
                        break;
                    }
                    Swagger_C_Program_Version_Update scheme_load_form = scheme_form.get();


                    // Nahraje do Azure a připojí do verze soubor
                    ObjectNode content = Json.newObject();
                    content.put("main", scheme_load_form.main);
                    content.set("user_files", Json.toJson(scheme_load_form.user_files));
                    content.set("library_files", Json.toJson(scheme_load_form.library_files));

                    // Content se nahraje na Azure
                    Model_FileRecord.uploadAzure_Version(content.toString(), "code.json", c_program.get_path(), version_object);
                    version_object.update();
                }

                version_object.compile_program_thread();
            }

            c_program.refresh();

            return GlobalResult.created(Json.toJson(c_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program",
            tags = {"C_Program"},
            notes = "get C_program by query = c_program_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.read_permission", value = Model_CProgram.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_program_get(@ApiParam(value = "c_program_id String query", required = true) String c_program_id) {
        try {

            // Vyhledám Objekt
            Model_CProgram c_program = Model_CProgram.find.byId(c_program_id);
            if(c_program == null) return GlobalResult.notFoundObject("C_Program c_program not found");

            // Zkontroluji oprávnění
            if(! c_program.read_permission())  return GlobalResult.forbidden_Permission();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(c_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program List",
            tags = {"C_Program"},
            notes = "get all C_Programs that belong to logged person",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check person_permissions"),
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
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number){

        try {

            // Získání JSON
            final Form<Swagger_C_Program_Filter> form = Form.form(Swagger_C_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);
            query.where().eq("project.participants.person.id", Controller_Security.getPerson().id);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if((help.project_id != null)&&!(help.project_id.equals(""))){

                query.where().eq("project.id", help.project_id);
            }

            // Vyvoření odchozího JSON
            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program_Version public",
            tags = {"C_Program"},
            notes = "get approved or edited C_program public Versions ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_C_Program_Version_Public_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_program_getPublicList(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number){
        try {

            // Vytřídění objektů
            Query<Model_VersionObject> query = Ebean.find(Model_VersionObject.class);
            query.where().isNotNull("c_program").eq("public_version", true).ne("approval_state", Approval_state.pending).ne("approval_state", Approval_state.disapproved);

            // Vytvoření výsledku a stránkování
            Swagger_C_Program_Version_Public_List result = new Swagger_C_Program_Version_Public_List(query,page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program_Version public",
            tags = {"C_Program"},
            hidden = true,
            notes = "get C_program public Versions by Type of Board",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @Security.Authenticated(Secured_Admin.class)
    public Result c_program_getPublicByType(String type_of_board_id){
        try {

            // Vytřídění objektů
            Query<Model_VersionObject> query = Ebean.find(Model_VersionObject.class);
            List<Model_CProgram> programs = Model_CProgram.find.where().isNull("project").eq("type_of_board.id", type_of_board_id).findList();

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(programs));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Edit C_Program",
            tags = {"C_Program"},
            notes = "If you want edit base information about C_program by  query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_edit"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",    response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_update(@ApiParam(value = "c_program_id String query", required = true)  String c_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.find.byId(c_program_id);
            if(c_program == null ) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Ověření objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(help.type_of_board_id);
            if(typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Úprava objektu
            c_program.name = help.name;
            c_program.description = help.description;
            c_program.type_of_board = typeOfBoard;

            // Zkontroluji oprávnění
            if(!c_program.edit_permission())  return GlobalResult.forbidden_Permission();

            // Uložení změn
            c_program.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(c_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete C_program",
            tags = {"C_Program"},
            notes = "delete C_program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_program_delete(@ApiParam(value = "c_program_id String query", required = true)  String c_program_id){
        try{

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.find.byId(c_program_id);
            if(c_program == null ) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Kontrola oprávnění
            if(!c_program.delete_permission()) return GlobalResult.forbidden_Permission();

            // Vyhledání PRoduct pro získání kontejneru
            //Model_Product product = Model_Product.find.where().eq("projects.c_programs.id", c_program_id).findUnique();


            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program List by Project",
            tags = {"C_Program"},
            notes = "get all C_Programs that belong to logged person and given project",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check person_permissions"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_Program_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_C_Program_List_by_Project(@ApiParam(value = "project_id String query", required = true) String project_id, @ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) Integer page_number){

        try {

            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);
            query.where().eq("project.participants.person.id", Controller_Security.getPerson().id).eq("project.id",project_id);

            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "new Version of C_Program",
            tags = {"C_Program"},
            notes = "If you want add new code to C_program by query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_update"),
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
            @ApiResponse(code = 201, message = "Successful created",        response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_programVersion_create(@ApiParam(value = "version_id String query", required = true)  String c_program_id){
        try{

            // Zpracování Json
            Form<Swagger_C_Program_Version_New> form = Form.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.find.byId(c_program_id);
            if(c_program == null) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Zkontroluji oprávnění
            if(!c_program.update_permission()) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Model_VersionObject version_object = new Model_VersionObject();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.author              = Controller_Security.getPerson();
            version_object.date_of_create      = new Date();
            version_object.c_program           = c_program;
            version_object.public_version      = false;

            // Zkontroluji oprávnění
            if(!version_object.c_program.update_permission()) return GlobalResult.forbidden_Permission();

            version_object.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode  content = Json.newObject();
            content.put("main", help.main );
            content.set("user_files", Json.toJson( help.user_files) );
            content.set("library_files", Json.toJson(help.library_files) );

            // Content se nahraje na Azure

            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program.get_path() ,  version_object);
            version_object.update();

            version_object.compile_program_thread();

            // Vracím vytvořený objekt
            return GlobalResult.created(Json.toJson(c_program.program_version(version_object)));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get C_program Version",
            tags = {"C_Program"},
            notes = "get Version of C_program by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_programVersion_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_object not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

            // Zkontroluji oprávnění
            if(! version_object.c_program.read_permission())  return GlobalResult.forbidden_Permission();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(version_object.c_program.program_version(version_object)));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update basic information in Version of C_program",
            tags = {"C_Program"},
            notes = "For update basic (name and description) information in Version of C_program. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_edit"),
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
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_programVersion_update(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();

            // Ověření objektu
            Model_VersionObject version_object= Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Kontrola oprávnění
            if(!version_object.c_program.edit_permission()) return GlobalResult.forbidden_Permission();

            //Uprava objektu
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            // Uložení změn
            version_object.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Version in C_program",
            tags = {"C_Program"},
            notes = "delete Version.id = version_id in C_program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_programVersion_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id){
        try{

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.delete_permission()) return GlobalResult.forbidden_Permission();

            version_object.removed_by_user = true;

            // Smažu zástupný objekt
            version_object.update();

            // Vracím potvrzení o smazání
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make C_program_Version public",
            tags = {"C_Program"},
            notes = "Make C_program public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_edit"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 400, message = "The user has entered more than three channels. Or other problem :(", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result c_programVersion_makePublic(@ApiParam(value = "version_id String query", required = true)  String version_id){
        try {

            // Kontrola objektu
            Model_VersionObject version = Model_VersionObject.find.byId(version_id);
            if(version == null) return GlobalResult.notFoundObject("Version not found");

            if(version.c_program == null )return GlobalResult.notFoundObject("Version not found");


            if(Model_VersionObject.find.where().eq("approval_state", Approval_state.pending.name())
                    .eq("c_program.project.participants.person.id", Controller_Security.getPerson().id)
                    .findList().size() > 3) return GlobalResult.result_BadRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");

            if(version.approval_state != null)  return GlobalResult.result_BadRequest("You cannot publish same program twice!");

            // Úprava objektu
            version.approval_state = Approval_state.pending;

            // Kontrola oprávnění
            if(!(version.c_program.edit_permission())) return GlobalResult.forbidden_Permission();

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "only for Tyrion Front End", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result get_version_for_decision(String version_id){
        try {

            // Vyhledám Objekt
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_object not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

            // Zkontroluji oprávnění
            if(! version_object.c_program.read_permission())  return GlobalResult.forbidden_Permission();


            Swagger_C_Program_Version_For_Public_Decision version = new Swagger_C_Program_Version_For_Public_Decision();
            version.c_program_version = version_object.c_program.program_version(version_object);
            version.c_program_id   = version_object.c_program.id;
            version.c_program_name = version_object.c_program.name;
            version.c_program_description = version_object.c_program.description;


            return  GlobalResult.result_ok(Json.toJson(version));


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "only for Tyrion Front End", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result approve_decision(){
        try {

            // Získání Json
            final Form<Swagger_C_Program_Version_Approve_WithChanges> form = Form.form(Swagger_C_Program_Version_Approve_WithChanges.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Approve_WithChanges help = form.get();

            // Kontrola objektu
            Model_VersionObject version_old = Model_VersionObject.find.byId(help.version_id);
            if(version_old == null) return GlobalResult.notFoundObject("Version not found");

            // Ověření objektu
            Model_CProgram c_program_old = Model_CProgram.find.byId(version_old.c_program.id);
            if(c_program_old == null) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Zkontroluji oprávnění
            if(!c_program_old.update_permission()) return GlobalResult.forbidden_Permission();


            if(help.decision){

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval_state.approved;
                version_old.update();

                Model_CProgram c_program = new Model_CProgram();
                c_program.name = help.c_program_name;
                c_program.description = help.c_program_description;
                c_program.date_of_create = new Date();
                c_program.type_of_board = c_program_old.type_of_board;

                // Zkontroluji oprávnění
                if(!c_program.create_permission()) return GlobalResult.forbidden_Permission();
                c_program.save();

                Model_VersionObject version_object = new Model_VersionObject();
                version_object.version_name        = help.version_name;
                version_object.version_description = help.version_description;
                version_object.date_of_create      = new Date();
                version_object.c_program           = c_program;
                version_object.public_version      = true;
                version_object.author              = version_old.author;

                // Zkontroluji oprávnění
                if(!version_object.c_program.update_permission()) return GlobalResult.forbidden_Permission();
                version_object.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode  content = Json.newObject();
                content.put("main", help.main );
                content.set("user_files", null);
                content.set("library_files", null );


                Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program.get_path() ,  version_object);
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
                                .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.getPerson().full_name)
                                .send(version_old.c_program.project.product.payment_details.person.mail, "Publishing your program" );

                    } catch (Exception e) {
                        logger.error ("Sending mail -> critical error", e);
                        e.printStackTrace();
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
                                .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.getPerson().full_name)
                                .send(version_old.c_program.project.product.payment_details.person.mail, "Publishing your program" );

                    } catch (Exception e) {
                        logger.error ("Sending mail -> critical error", e);
                        e.printStackTrace();
                    }
                }

            }else {

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval_state.disapproved;
                version_old.update();

                try {

                    new Email()
                            .text("First! Thank you for publishing your program!")
                            .text(  Email.bold("C Program Name: ") +        c_program_old.name + Email.newLine() +
                                    Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("Version Name: ") +          c_program_old.name + Email.newLine() +
                                    Email.bold("Version Description: ") +   c_program_old.name + Email.newLine() )
                            .divider()
                            .text("We are sorry, but we found some problems in your program, so we did not publish it. But do not worry and do not give up! " +
                                    "We are glad that you want to contribute to our public libraries. Here are some tips what to improve, so you can try it again.")
                            .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                            .text(Email.bold("Thanks!") + Email.newLine() + Controller_Security.getPerson().full_name)
                            .send(version_old.c_program.project.product.payment_details.person.mail, "Publishing your program" );

                } catch (Exception e) {
                    logger.error ("Sending mail -> critical error", e);
                    e.printStackTrace();
                }

            }


            // Potvrzení
            return  GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Create new default C_Program for Type of Board",
            tags = {"C_Program"},
            hidden = true,
            notes = "If you want create new C_program in project.id = {project_id}.",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.create_permission", value = Model_CProgram.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "C_program_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created", response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result c_program_createDefault() {
        try {

            // Zpracování Json
            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Ověření Typu Desky
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Tvorba programu
            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.date_of_create        = new Date();
            c_program.type_of_board         = typeOfBoard;
            c_program.type_of_board_default = typeOfBoard;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!c_program.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení C++ Programu
            c_program.save();

            return GlobalResult.created(Json.toJson(c_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Mark as main", hidden = true)
    @BodyParser.Of(BodyParser.Empty.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result c_program_markScheme(@ApiParam(value = "c_program_id", required = true) String c_program_id, @ApiParam(value = "version_id", required = true) String version_id) {
        try {

            // Kontrola objektu
            Model_CProgram cProgram = Model_CProgram.find.byId(c_program_id);
            if (cProgram == null) return GlobalResult.notFoundObject("CProgram c_program_id not found");

            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_object_id not found");

            if (version_object.c_program == null || version_object.c_program.type_of_board_default == null) return GlobalResult.result_BadRequest("Version_object is not version of c_program or is not default firmware");

            // Kontrola oprávnění
            if(!cProgram.edit_permission()) return GlobalResult.forbidden_Permission();

            if (cProgram.default_main_version != null){
                cProgram.default_main_version.default_program = null;
                cProgram.default_main_version.update();
            }

            version_object.default_program = cProgram;
            version_object.update();

            cProgram.refresh();

            // Vracím Json
            return GlobalResult.result_ok(Json.toJson(cProgram));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

}
