package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.compiler.*;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.c_program.Model_CProgram;
import models.project.c_program.actualization.Model_ActualizationProcedure;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import play.data.Form;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.*;
import utilities.emails.Email;
import utilities.enums.*;
import utilities.hardware_updater.Master_Updater;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Board_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_Version_Public_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_ImportLibrary_List;
import utilities.swagger.outboundClass.*;
import utilities.web_socket.message_objects.compilator_tyrion.WS_Make_compilation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Controller se zabívá správou knihoven, procesorů, desek (hardware), typů desek a jejich výrobcem.
 * Dále obsluhuje kompilaci C++ kodu (propojení s kontrolerem Websocket)
 *
 */


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Secured_API.class)
public class Controller_CompilationLibraries extends Controller {

    // Rest Api call client
    @Inject WSClient ws;

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


// C_ Program && Version ###############################################################################################*/

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
                        logger.error("CompilationLibraries:: c_program_create:: Error loading first default version of CProgram");
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
            Model_Product product = Model_Product.find.where().eq("projects.c_programs.id", c_program_id).findUnique();


            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
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


///###################################################################################################################*/

    @ApiOperation(value = "compile C_program Version",
            tags = {"C_Program"},
            notes = "Compile specific version of C_program - before compilation - you have to update (save) version code",
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
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Compilation_Ok.class),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_BadRequest.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_BadRequest.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_version( @ApiParam(value = "version_id String query", required = true) String version_id ){
        try{

            logger.debug("CompilationController:: Starting compilation on version_id = " + version_id);

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Smažu předchozí kompilaci
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version is not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.read_permission()) return GlobalResult.forbidden_Permission();

            // Smažu předchozí kompilaci
            if(version_object.c_compilation != null) return GlobalResult.result_ok(Json.toJson( new Swagger_Compilation_Ok()));


            JsonNode result = version_object.compile_program_procedure();

            if(result.has("status") && result.get("status").asText().equals("success")) return  GlobalResult.result_ok(result);

            if(result.has("error_code") && result.get("error_code").asInt() == 400) return GlobalResult.badRequest(result);
            if(result.has("error_code") && result.get("error_code").asInt() == 477) return GlobalResult.external_server_is_offline();

            // Neznámá chyba se kterou nebylo počítání
           return GlobalResult.result_BadRequest("unknown_error");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "compile C_program with Code",
            tags = {"C_Program"},
            notes = "Compile code",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compilation successful",    response = Result_ok.class),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_BadRequest.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_code() {
        try {

            // Zpracování Json
            Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.formExcepting(form.errorsAsJson());
            Swagger_C_Program_Version_Update help = form.get();

            // Ověření objektu
            if (help.type_of_board_id.isEmpty()) return GlobalResult.result_BadRequest("type_of_board_id is missing!");

            // Ověření objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            if(!Model_CompilationServer.is_online()) return GlobalResult.result_external_server_is_offline("Compilation Server offilne");


            List<Swagger_C_Program_Version_New.Library_File> library_files = new ArrayList<>();

            for (String lib_id : help.library_files) {

                Model_VersionObject lib_version = Model_VersionObject.find.byId(lib_id);
                if (lib_version == null || lib_version.library == null){

                    ObjectNode error = Json.newObject();
                    error.put("status", "error");
                    error.put("error", "Error getting libraries");
                    error.put("error_code", 400);
                    return GlobalResult.result_BadRequest(error);
                }

                if (!lib_version.files.isEmpty()){
                    for (Model_FileRecord f : lib_version.files) {

                        JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                        Form<Swagger_C_Program_Version_New.Library_File> lib_form = Form.form(Swagger_C_Program_Version_New.Library_File.class).bind(j);
                        if (lib_form.hasErrors()){

                            ObjectNode error = Json.newObject();
                            error.put("status", "error");
                            error.put("error", "Error importing libraries");
                            error.put("error_code", 400);
                            return GlobalResult.result_BadRequest(error);
                        }

                        Swagger_C_Program_Version_New.Library_File lib_file = lib_form.get();

                        for (Swagger_C_Program_Version_Update.User_File user_file : help.user_files){

                            if (lib_file.file_name.equals(user_file.file_name))break;
                            if (!library_files.contains(lib_file)) library_files.add(lib_file);

                        }
                    }
                }
            }

            ObjectNode includes = Json.newObject();

            for(Swagger_C_Program_Version_New.Library_File file_lib : library_files){
                includes.put(file_lib.file_name , file_lib.content);
            }

            if(help.user_files != null)
                for(Swagger_C_Program_Version_Update.User_File user_file : help.user_files){
                    includes.put(user_file.file_name , user_file.code);
                }


            if (Controller_WebSocket.compiler_cloud_servers.isEmpty()) {
                return GlobalResult.result_external_server_is_offline("Compilation cloud_compilation_server is offline!");
            }


            // Odesílám na compilační cloud_compilation_server
            WS_Make_compilation compilation_result = Model_CompilationServer.make_Compilation(new WS_Make_compilation().make_request( typeOfBoard ,"", help.main, includes ));


            // V případě úspěšného buildu obsahuje příchozí JsonNode buildUrl
            if (compilation_result.buildUrl != null && compilation_result.status.equals("success")) {
                return GlobalResult.result_ok();
            }

            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
            if (!compilation_result.buildErrors.isEmpty()) {

                return GlobalResult.result_buildErrors(Json.toJson(compilation_result.buildErrors));
            }

            // Nebylo úspěšné ani odeslání requestu - Chyba v konfiguraci a tak vracím defaulní chybz
            if (compilation_result.error != null) {

                ObjectNode result_json = Json.newObject();
                result_json.put("error", compilation_result.error);

                return GlobalResult.result_external_server_error(result_json);
            }

            // Neznámá chyba se kterou nebylo počítání
            return GlobalResult.result_BadRequest("Unknown error");
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    /**
    @ApiOperation(value = "update Embedded Hardware with  binary file",
            tags = {"C_Program", "Actualization"},
            notes = "Upload Binary file and choose hardware_id for update. Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https",
            consumes = "application/octet-stream",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_ok.class),
            @ApiResponse(code = 477, message = "External Cloud_Homer_server where is hardware is offline", response = Result_serverIsOffline.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result uploadBinaryFileToBoard(@ApiParam(value = "version_id ", required = true) String board_id, @ApiParam(value = "version_id ", required = true) String firmware_type_string) {
        try {

            System.out.println("Body " + request().body().asText());

            // Vyhledání objektů
            Board board = Board.find.byId(board_id);
            if (board == null) return GlobalResult.notFoundObject("Board board_id object not found");

            if (!board.update_permission()) return GlobalResult.forbidden_Permission();

            Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
            if (firmware_type == null) return GlobalResult.notFoundObject("FirmwareType not found!");

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.notFoundObject("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_BadRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_BadRequest("File is bigger than 500K b");

            // Existuje Homer?

             String binary_file = FileRecord.get_encoded_binary_string_from_File(file);
             FileRecord fileRecord = FileRecord.create_Binary_file("byzance-private/binaryfiles", binary_file, file_name);
             Controller_Actualization.add_new_actualization_request_with_user_file(board.project, firmware_type, board, fileRecord);

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    */

    @ApiOperation(value = "only for Tyrion Front End", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result uploadBinaryFileToBoard_fake_board(String instance_id, String board_id, String build_id,  String firmware_type_string){
        try {

            // Slouží k nahrávání firmwaru do deviců, které jsou ve fakce instnaci pro testování
            // nejsou databázovaný a tedy nejde spustit regulérní update procedura na kterou jsme zvyklé - viz metoda nad tímto
            // Slouží jen pro Admin rozhraní Tyriona

            Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
            if (firmware_type == null) return GlobalResult.notFoundObject("FirmwareType not found!");

            List<String> list = new ArrayList<>();
            list.add(board_id);

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.notFoundObject("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_BadRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_BadRequest("File is bigger than 500K b");


            ObjectNode request = Json.newObject();
            request.put("messageChannel", "tyrion");
            request.put("instanceId", instance_id);
            request.put("messageType", "updateDevice");
            request.put("firmware_type", firmware_type.get_firmwareType());
            request.set("targetIds",  Json.toJson(list));
            request.put("build_id", build_id);
            request.put("program", Model_FileRecord.get_encoded_binary_string_from_File(file));

            // TODO - tohle nejde nějak domylset
            // ObjectNode result =  Controller_WebSocket.incomingConnections_homers.get(instance_id).write_with_confirmation(request, 1000*30, 0, 3);

            if(request.get("status").asText().equals("success")) {
                return GlobalResult.result_ok();
            }
            else {
                return GlobalResult.result_BadRequest(request);
            }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Embedded Hardware with C_program compilation",
            tags = {"C_Program", "Actualization"},
            notes = "Upload compilation to list of hardware. Compilation is on Version oc C_program. And before uplouding compilation, you must succesfuly compile required version before! " +
                    "Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.update_permission", value = "true"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    })
            }

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_UploadBinaryFileToBoard",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result uploadCompilationToBoard() {
        try {

            // Zpracování Json
            Form<Swagger_UploadBinaryFileToBoard> form = Form.form(Swagger_UploadBinaryFileToBoard.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_UploadBinaryFileToBoard help = form.get();


            // Ověření objektu
            Model_VersionObject c_program_version = Model_VersionObject.find.byId(help.version_id);
            if(c_program_version == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Zkontroluji oprávnění
            if(! c_program_version.c_program.read_permission())  return GlobalResult.forbidden_Permission();

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(c_program_version.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(c_program_version.c_compilation == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program - Missing compilation File");

            // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
            if(c_program_version.c_compilation.status != Compile_Status.successfully_compiled_and_restored) return GlobalResult.result_BadRequest("You cannot upload code in state:: " + c_program_version.c_compilation.status.name());

            //Zkontroluji zda byla verze už zkompilována
            if(!c_program_version.c_compilation.status.name().equals(Compile_Status.successfully_compiled_and_restored.name())) return GlobalResult.result_BadRequest("The program is not yet compiled & Restored");

            String typeOfBoard_id = c_program_version.c_program.type_of_board_id();

            // Vyhledání objektů
            List<Model_Board> board_from_request = Model_Board.find.where().idIn(help.board_id).findList();
            if (board_from_request.size() == 0) return GlobalResult.result_BadRequest("0 device is available. Does not exist or is decommissioned.");

            // Vyseparované desky nad který lze provádět nějaké operace
            List<Model_Board> board_for_update = Model_Board.find.where().idIn(help.board_id).findList();
            // Kontrola oprávnění
            for (Model_Board board : board_from_request) {
                // Kontrola oprávnění
                if (board.update_permission() && board.type_of_board_id().equals(typeOfBoard_id))
                    board_for_update.add(board);
            }

            Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
            procedure.save();

            for(Model_Board board : board_for_update)
            {
                Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                plan.board = board;
                plan.firmware_type = Firmware_type.FIRMWARE;
                plan.actualization_procedure = procedure;
                plan.c_program_version_for_update = c_program_version;
                plan.save();
            }

            procedure.refresh();

            Master_Updater.add_new_Procedure(procedure);

            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


///###################################################################################################################*/

    @ApiOperation(value = "Create new Compilation Server",
            hidden = true,
            tags = {"External Server"},
            notes = "Create new Gate for Compilation Server",
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
            @ApiResponse(code = 201, message = "Successful created",      response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Compilation_Server(){
        try{

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Compilation_Server_New help = form.get();


            // Vytvářím objekt
            Model_CompilationServer server = new Model_CompilationServer();
            server.personal_server_name = help.personal_server_name;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! server.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            server.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Compilation Server",
            hidden = true,
            tags = {"External Server"},
            notes = "Edit basic information Compilation Server",
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
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Compilation_Server( @ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Compilation_Server_New help = form.get();

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Zkontroluji oprávnění
            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            // Upravím objekt
            server.personal_server_name = help.personal_server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Compilation Servers",
            hidden = true,
            tags = {"External Server"},
            notes = "get Compilation Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Model_CompilationServer.class, responseContainer = "List "),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Compilation_Server(){
        try{

            // Vyhledám všechny objekty
            List<Model_CompilationServer> servers = Model_CompilationServer.find.all();

            // Vracím Objekty
            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Compilation Servers",
            hidden = true,
            tags = {"External Server"},
            notes = "remove Compilation Servers",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Compilation_Server( @ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! server.delete_permission())  return GlobalResult.forbidden_Permission();

            // Smažu objekt
            server.delete();

            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create new Processor",
            hidden = true,
            tags = {"Processor"},
            notes = "If you want create new Processor. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Vytvářím objekt
            Model_Processor processor = new Model_Processor();
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            processor.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Processor",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Objects not found",     response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_get(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Vracím objekt
            return GlobalResult.result_ok(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Processors",
            tags = {"Processor"},
            notes = "If you want get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Processor.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_getAll() {
        try {

            //Vyhledám objekty
           List<Model_Processor> processors = Model_Processor.find.all();

            // Vracím seznam objektů
           return GlobalResult.result_ok(Json.toJson(processors));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Processor",
            tags = {"Processor"},
            notes = "If you want update Processor.id by query = processor_id . Send required json values and server respond with update object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_update(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Zpracování Json
            Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.edit_permission())  return GlobalResult.forbidden_Permission();

            // Upravuji objekt
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            // Ukládám do databáze
            processor.update();

            // Vracím upravený objekt
            return GlobalResult.result_ok(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Processor",
            hidden = true,
            tags = {"Processor"},
            notes = "If you want delete Processor by query processor_id.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_delete(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.delete_permission())  return GlobalResult.forbidden_Permission();

            // Mažu z databáze
            processor.delete();

            // Vracím objekt
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "get FileRecord",
            tags = {"File"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            hidden = true
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_File_Content.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result fileRecord(@ApiParam(value = "file_record_id String query", required = true)  String file_record_id){
        try {

            // Kontrola validity objektu
            Model_FileRecord fileRecord = Model_FileRecord.find.fetch("version_object").where().eq("id", file_record_id).findUnique();
            if (fileRecord == null) return GlobalResult.notFoundObject("FileRecord file_record_id not found");

            // Swagger_File_Content - Zástupný dokumentační objekt

            // Vracím content
            return GlobalResult.result_ok(Json.toJson( fileRecord.get_fileRecord_from_Azure_inString()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

///###################################################################################################################*/

    @ApiOperation(value = "create new Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want create new Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Producer() {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            //Vytvářím objekt
            Model_Producer producer = new Model_Producer();
            producer.name = help.name;
            producer.description = help.description;

            // Kontorluji oprávnění těsně před uložením
            if(! producer.create_permission()) return GlobalResult.forbidden_Permission();

            //Ukládám objekt
            producer.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want edit information about Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.edit_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Producer(@ApiParam(required = true) String producer_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontorluji oprávnění těsně před uložením
            if(! producer.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            producer.name = help.name;
            producer.description = help.description;

            // Uložení změn objektu
            producer.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Producers",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producers() {
        try {

            // Získání seznamu
            List<Model_Producer> producers = Model_Producer.find.all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(producers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Producer.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producer(@ApiParam(required = true)  String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want delete Producer",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.delete_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Producer(@ApiParam(required = true) String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontorluji oprávnění
            if(! producer.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            producer.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "The TypeOfBoard is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filtres and more..",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_create"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_create() {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Tvorba objektu
            Model_TypeOfBoard typeOfBoard = new Model_TypeOfBoard();
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Kontorluji oprávnění
            if(!typeOfBoard.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu do DB
            typeOfBoard.save();

            return GlobalResult.created(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want edit base TypeOfBoard information",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_update(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.edit_permission()) return GlobalResult.forbidden_Permission();

            // Uprava objektu
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.compiler_target_name = help.compiler_target_name;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Uložení do DB
            typeOfBoard.update();

            // Vrácení změny
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "delete TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_delete(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found") ;

            // Kontorluji oprávnění
            if(! typeOfBoard.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            typeOfBoard.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get list of all TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get all TypeOfBoard objects",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_getAll() {
        try {

            // Získání seznamu
            List<Model_TypeOfBoard> typeOfBoards = Model_TypeOfBoard.find.all();

            // Vrácení seznamu
            return  GlobalResult.result_ok(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_get(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola validity objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Upload TypeOfBoard picture", hidden = true)
    public Result typeOfBoard_uploadPicture(@ApiParam(required = true) String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (type_of_board == null) return GlobalResult.notFoundObject("Type of board does not exist");

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            if (body == null) return GlobalResult.notFoundObject("Missing picture!");

            Http.MultipartFormData.FilePart file_from_request = body.getFile("file");

            if (file_from_request == null) return GlobalResult.notFoundObject("Missing picture!");

            File file = file_from_request.getFile();

            int dot = file_from_request.getFilename().lastIndexOf(".");
            String file_type = file_from_request.getFilename().substring(dot);

            // Zkontroluji soubor - formát, velikost, rozměry
            if((!file_type.equals(".jpg"))&&(!file_type.equals(".png"))) return GlobalResult.result_BadRequest("Wrong type of File - '.jpg' or '.png' required! ");
            if( (file.length() / 1024) > 500) return GlobalResult.result_BadRequest("Picture is bigger than 500 KB");
            BufferedImage bimg = ImageIO.read(file);
            if((bimg.getWidth() < 50)||(bimg.getWidth() > 400)||(bimg.getHeight() < 50)||(bimg.getHeight() > 400)) return GlobalResult.result_BadRequest("Picture height or width is not between 50 and 400 pixels.");

            // Odebrání předchozího obrázku
            if(!(type_of_board.picture == null)){
                Model_FileRecord fileRecord = type_of_board.picture;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }

            // Pokud link není, vygeneruje se nový, unikátní
            if(type_of_board.azure_picture_link == null){
                while(true){ // I need Unique Value
                    String azure_picture_link = type_of_board.get_Container().getName() + "/" + UUID.randomUUID().toString() + file_type;
                    if (Model_TypeOfBoard.find.where().eq("azure_picture_link", azure_picture_link ).findUnique() == null) {
                        type_of_board.azure_picture_link = azure_picture_link;
                        type_of_board.update();
                        break;
                    }
                }
            }

            String file_path = type_of_board.azure_picture_link;

            int slash = file_path.indexOf("/");
            String file_name = file_path.substring(slash+1);

            type_of_board.picture = Model_FileRecord.uploadAzure_File(file, file_name, file_path);
            type_of_board.update();


            return GlobalResult.result_ok(Json.toJson(type_of_board));
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Remove TypeOfBoard picture", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result typeOfBoard_removePicture(@ApiParam(required = true) String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (type_of_board == null) return GlobalResult.notFoundObject("Type of Board does not exist");

            if(!(type_of_board.picture == null)) {
                Model_FileRecord fileRecord = type_of_board.picture;
                type_of_board.azure_picture_link = null;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }else{
                return GlobalResult.result_BadRequest("There is no picture to remove.");
            }

            return GlobalResult.result_ok("Picture successfully removed");
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    // BootLoader ---------------------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "new_boot_loader", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result boot_loader_create(@ApiParam(value = "type_of_board_id", required = true) String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = Form.form(Swagger_BootLoader_New.class).bindFromRequest();
            if(form.hasErrors()){return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BootLoader_New help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(type_of_board == null) return GlobalResult.notFoundObject("Type_of_board_not_found");

            if(!help.control_identificator()) return GlobalResult.result_BadRequest("Version format is not correct (255.255.255)");

            if(Model_BootLoader.find.where().eq("version_identificator", help.version_identificator ).eq("type_of_board.id", type_of_board.id).findUnique() != null) return GlobalResult.result_BadRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.date_of_create = new Date();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_notes;
            boot_loader.description = help.description;
            boot_loader.version_identificator = help.version_identificator;
            boot_loader.type_of_board = type_of_board;

            if(!boot_loader.create_permission()) return GlobalResult.forbidden_Permission();
            boot_loader.save();

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Uploud bootloader file", hidden = true)
    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result boot_loader_upload_file(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.notFoundObject("BootLoader boot_loader_id not found");

            if(!boot_loader.edit_permission()) return GlobalResult.forbidden_Permission();

            if(boot_loader.file != null) return GlobalResult.result_BadRequest("You cannot upload file twice!");

            Http.MultipartFormData body = request().body().asMultipartFormData();
            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            //Bin FILE
            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin")) return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500) return GlobalResult.result_BadRequest("File is bigger than 500Kb");

            String binary_file = Model_FileRecord.get_encoded_binary_string_from_File(file);
            Model_FileRecord filerecord  = Model_FileRecord.create_Binary_file( boot_loader.get_path(), binary_file, "bootloader.bin");

            boot_loader.file = filerecord;
            filerecord.boot_loader = boot_loader;
            filerecord.update();
            boot_loader.update();


            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Mark as main", hidden = true)
    @BodyParser.Of(BodyParser.Empty.class)
    public Result boot_loader_mark_as_main(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.notFoundObject("BootLoader boot_loader_id not found");

            if(!boot_loader.edit_permission()) return GlobalResult.forbidden_Permission();
            if(boot_loader.file == null) return GlobalResult.result_BadRequest("Required bootloader object with file");

            if(boot_loader.main_type_of_board != null) return GlobalResult.result_BadRequest("Bootloader is Already Main");


            Model_BootLoader old_main = Model_BootLoader.find.where().eq("main_type_of_board.id", boot_loader.type_of_board.id).findUnique();
            if(old_main != null){

                old_main.main_type_of_board = null;
                old_main.update();

            }

            boot_loader.main_type_of_board = boot_loader.type_of_board;
            boot_loader.update();

            // Vracím Json
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Update bootloader on device list", hidden = true)
    @BodyParser.Of(BodyParser.Json.class)
    public Result boot_loader_update_instance(String instance_id){
        try {

            Model_HomerInstance instance = Model_HomerInstance.find.byId(instance_id);
            if(instance == null) return GlobalResult.notFoundObject("Instance not found");

            if(instance.actual_instance == null) return GlobalResult.notFoundObject("Instance not found");

             instance.actual_instance.add_new_actualization_request_bootloader();


            // Vracím Json
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board",
            hidden =  true,
            tags = { "Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "boards themselves with \"registration procedure\". Its not allowed to delete that! Only deactivate. Classic User can registed that to own " +
                    "project or own account",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                 @Extension( name = "permission_required", properties = {
                         @ExtensionProperty(name = "TypeOfBoard.register_new_device_permission", value = "true"),
                         @ExtensionProperty(name = "Static Permission key", value = "Board_create"),
                 }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Model_Board.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New> form = Form.form(Swagger_Board_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_New help = form.get();

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId( help.type_of_board_id  );
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.register_new_device_permission()) return GlobalResult.forbidden_Permission();


                Model_Board board = new Model_Board();
                board.id = help.hardware_unique_id;
                board.is_active = false;
                board.date_of_create = new Date();
                board.type_of_board = typeOfBoard;

                // Uložení desky do DB
                board.save();

            // Vracím seznam zařízení k registraci
            return GlobalResult.created(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Board - update personal description",
            tags = { "Board"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Projec\" ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_edit"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Personal",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update(@ApiParam(required = true)  String board_id){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Personal> form = Form.form(Swagger_Board_Personal.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Personal help = form.get();

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.edit_permission()) return GlobalResult.forbidden_Permission();

            // Uprava desky
            board.personal_description = help.personal_description;

            // Uprava objektu v databázi
            board.update();

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Boards with filter parameters",
            tags = { "Board"},
            notes = "Get List of boards. Acording by permission - system return only hardware from project, where is user owner or" +
                    " all boards if user have static Permission key",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Board_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_getByFilter() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = Form.form(Swagger_Board_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            System.out.println("Filter board");
            System.out.println(Json.toJson(help));

            // Tvorba parametru dotazu
            Query<Model_Board> query = Ebean.find(Model_Board.class);


            // If Json contains TypeOfBoards list of id's
            if(help.type_of_board_ids != null ){
                query.where().in("type_of_board.id", help.type_of_board_ids);
            }

            // If contains confirms
            if(help.active != null){
                Boolean isActive = help.active.equals("true");
                query.where().eq("isActive", isActive);
            }

            // From date
            if(help.projects != null){
                query.where().in("projects.id", help.projects);
            }

            if(help.producers != null){
                query.where().in("type_of_board.producer.id", help.producers);
            }

            if(help.processors != null){
                query.where().in("type_of_board.processor.id", help.processors);
            }

            // Vytvářím seznam podle stránky
            Swagger_Board_List result = new Swagger_Board_List(query, help.page_number);

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Board"},
            notes = "Permanent exclusion from the system - for some reason it is not allowed to remove the Board from database",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_deactivate(@ApiParam(required = true)  String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(board.update_permission()) return GlobalResult.forbidden_Permission();

            // Úprava stavu
            board.is_active = false;

            // Uložení do databáze
            board.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get Board",
            tags = { "Board"},
            notes = "if you want get Board object by query = board_id. User can get only boards from project, whitch " +
                    "user owning or user need Permission key \"Board_rea\".",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_get(@ApiParam(required = true) String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.read_permission()) return GlobalResult.forbidden_Permission();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            e.printStackTrace();
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "check Board during registration",
            tags = {"Board"},
            notes = "Check Board state for new Registration. Types of responses in JSON state value" +
                    "[CAN_REGISTER, NOT_EXIST, ALREADY_REGISTERED_IN_YOUR_ACCOUNT, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE]... \n " +
                    "PERMANENTLY_DISABLED - device was removed by Byzance. \n" +
                    "BROKEN_DEVICE - device exist - but its not possible to registered that. Damaged during manufacturing. ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Board_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_check(@ApiParam(required = true) String hash_for_adding) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique();


            Swagger_Board_Registration_Status status = new Swagger_Board_Registration_Status();

            if(board == null ){
                status.status = Registration_Board_status.NOT_EXIST;
            }else if(board.project_id() == null){
                status.status = Registration_Board_status.CAN_REGISTER;
            }else if(board.project_id() != null && board.read_permission()){
                status.status = Registration_Board_status.ALREADY_REGISTERED_IN_YOUR_ACCOUNT;
            }else{
                status.status = Registration_Board_status.ALREADY_REGISTERED;
            }


            return GlobalResult.result_ok(Json.toJson(status));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    @ApiOperation(value = "connect Board with Project",
            tags = { "Board"},
            notes = "This Api is used by Users for connection of Board with their Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Board_Connection", value = Model_Board.connection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.first_connect_permission", value = "true"),
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_connectProject(@ApiParam(required = true) String hash_for_adding, @ApiParam(required = true) String project_id){
        try {

            logger.debug("CompilationControler:: Registrace nového zařízení ");
            // Kotrola objektu
            Model_Board board = Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique();
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kotrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if(!board.first_connect_permission()) return GlobalResult.result_BadRequest("Board is already registered");

            // Kontrola oprávnění
            if(!project.update_permission()) return GlobalResult.forbidden_Permission();

            // uprava desky
            board.project = project;
            project.boards.add(board);
            board.update();
            project.update();


            if(board.type_of_board.connectible_to_internet){

                logger.debug("CompilationController:: board_connectProject:: Deska je připojitelná k internetu");

                Model_HomerInstance instance = project.private_instance;
                instance.boards_in_virtual_instance.add(board);
                board.virtual_instance_under_project = instance;
                instance.update();
                board.update();
                instance.add_Yoda_to_instance(board.id);
            }


             // vrácení objektu
             return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "disconnect Board from Project",
            tags = { "Board"},
            notes = "This Api is used by Users for disconnection of Board from their Project, its not meaning that Board is removed from system, only disconnect " +
                    "and another user can registred that (connect that with different account/project etc..)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Board_Disconnection", value = Model_Board.disconnection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_disconnectProject(@ApiParam(required = true)   String board_id){
        try {

            // Kontrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.update_permission()) return GlobalResult.forbidden_Permission();

            // Odstraním vazbu
            board.project = null;

            // uložím do databáze
            board.update();

            // vracím upravenou hodnotu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Boards details for integration to Blocko program",
            tags = {"Blocko", "B_Program"},
            notes = "get all boards that user can integrate to Blocko program",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Project_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Swagger_Boards_For_Blocko.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_allDetailsForBlocko(@ApiParam(required = true)   String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return GlobalResult.forbidden_Permission();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.add_M_Projects(project.m_projects);
            boards_for_blocko.add_C_Programs(project.c_programs);

            for (Model_Board board : project.boards)              boards_for_blocko.boards.add(board.get_short_board());


            boards_for_blocko.type_of_boards = Model_TypeOfBoard.find.where().eq("boards.project.id", project.id).findList();


            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // LIBRARIES #######################################################################################################

    @ApiOperation(value = "create new ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "TODO",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "ImportLibrary_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ImportLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_ImportLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibrary_create() {
        try {

            // Zpracování Json
            final Form<Swagger_ImportLibrary_New> form = Form.form(Swagger_ImportLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ImportLibrary_New help = form.get();

            // Vytvářím objekt
            Model_ImportLibrary library = new Model_ImportLibrary();
            library.name = help.name;
            library.description = help.description;
            library.long_description = help.long_description;
            library.tag = help.tag;
            library.state = Library_state.NEW;

            // Kontorluji oprávnění těsně před uložením
            if(!library.create_permission()) return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            library.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(library));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ImportLibrary",
            tags = {"ImportLibrary"},
            notes = "if you want to get ImportLibrary.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ImportLibrary.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result importLibrary_get(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Kontrola objektu
            Model_ImportLibrary library = Model_ImportLibrary.find.byId(library_id);
            if(library == null ) return GlobalResult.notFoundObject("ImportLibrary not found");

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(library));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all ImportLibraries",
            tags = {"ImportLibrary"},
            notes = "if you want to get all ImportLibraries.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ImportLibrary.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result importLibrary_getAll() {
        try {

            // Získání objektů
            List<Swagger_ImportLibrary_Short_Detail> libraries = new ArrayList<>();

            for (Model_ImportLibrary library : Model_ImportLibrary.find.where().eq("removed", false).findList()){
                libraries.add(library.get_short_import_library());
            }

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(libraries));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ImportLibraries by filter",
            tags = {"ImportLibrary"},
            notes = "if you want to get ImportLibraries filtered by specific tag.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ImportLibrary.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result importLibrary_getByFilter(@ApiParam(value = "page_number is Integer. Contain  1,2...n. For first call, use 1", required = false)  int page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_ImportLibrary_Filter> form = Form.form(Swagger_ImportLibrary_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ImportLibrary_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_ImportLibrary> query = Ebean.find(Model_ImportLibrary.class);
            query.where().eq("removed", false);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.tag != null){

                query.where().eq("tag", help.tag);
            }

            // Vyvoření odchozího JSON
            Swagger_ImportLibrary_List result = new Swagger_ImportLibrary_List(query,page_number);

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "TODO",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "ImportLibrary_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ImportLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response = Model_ImportLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibrary_update(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Zpracování Json
            final Form<Swagger_ImportLibrary_New> form = Form.form(Swagger_ImportLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ImportLibrary_New help = form.get();

            // Vyhledání objektu
            Model_ImportLibrary library = Model_ImportLibrary.find.byId(library_id);
            if (library == null) return GlobalResult.notFoundObject("Library not found");

            // Kontrola oprávnění
            if(!library.edit_permission()) return GlobalResult.forbidden_Permission();

            library.name = help.name;
            library.description = help.description;
            library.long_description = help.long_description;
            library.tag = help.tag;
            library.state = help.state;

            // Uložení změn
            library.update();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(library));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "if you want delete ImportLibrary.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "ImportLibrary_delete"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibrary_delete(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Kontrola objektu
            Model_ImportLibrary library = Model_ImportLibrary.find.byId(library_id);
            if(library == null ) return GlobalResult.notFoundObject("ImportLibrary not found");

            // Kontrola oprávnění
            if (!library.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            library.removed = true;
            library.update();

            // Vrácneí potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "new Version of ImportLibrary",
            tags = {"ImportLibrary"},
            hidden = true,
            notes = "If you want add new code to ImportLibrary",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_update"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ImportLibrary_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_create(@ApiParam(value = "library_id String query", required = true)  String library_id){
        try{

            // Zpracování Json
            Form<Swagger_ImportLibrary_Version_New> form = Form.form(Swagger_ImportLibrary_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ImportLibrary_Version_New help = form.get();

            // Ověření objektu
            Model_ImportLibrary library = Model_ImportLibrary.find.byId(library_id);
            if(library == null) return GlobalResult.notFoundObject("ImportLibrary library_id not found");

            // Zkontroluji oprávnění
            if(!library.update_permission()) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Model_VersionObject version_object = new Model_VersionObject();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.author              = Controller_Security.getPerson();
            version_object.date_of_create      = new Date();
            version_object.library             = library;
            version_object.public_version      = true;

            version_object.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode  content = Json.newObject();
            content.set("library_files", Json.toJson(help.library_files) );

            // Content se nahraje na Azure

            Model_FileRecord.uploadAzure_Version(content.toString(), "library.json" , library.get_path() ,  version_object);
            version_object.update();

            // Vracím vytvořený objekt
            return GlobalResult.created(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ImportLibrary Version",
            tags = {"ImportLibrary"},
            notes = "get Version of ImportLibrary by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "ImportLibrary.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result importLibraryVersion_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_object not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Zkontroluji oprávnění
            if(! version_object.library.read_permission())  return GlobalResult.forbidden_Permission();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update basic information in Version of ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "For update basic (name and description) information in Version of ImportLibrary. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_edit"),
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_update(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();

            // Ověření objektu
            Model_VersionObject version_object= Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.forbidden_Permission();

            //Uprava objektu
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            // Uložení změn
            version_object.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Version in ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "delete ImportLibrary by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id){
        try{

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Kontrola oprávnění
            if(!version_object.library.delete_permission()) return GlobalResult.forbidden_Permission();

            version_object.removed_by_user = true;

            // Smažu zástupný objekt
            version_object.update();

            // Vracím potvrzení o smazání
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload file to Version of ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "For update Library files in Version of ImportLibrary.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_File_Load",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_uploadFile(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_Library_File_Load> form = Form.form(Swagger_Library_File_Load.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Library_File_Load help = form.get();

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.forbidden_Permission();

            if(version_object.files.size() > 0) {

                Model_FileRecord file = version_object.files.get(0);
                //Uprava objektu
                JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                Form<Swagger_Library_File_Load> load_form = Form.form(Swagger_Library_File_Load.class).bind(json);
                if (load_form.hasErrors()) {return GlobalResult.formExcepting(load_form.errorsAsJson());}
                Swagger_Library_File_Load lib_help = load_form.get();

                Swagger_ImportLibrary_Version_New.Library_File to_remove;

                for (Swagger_ImportLibrary_Version_New.Library_File new_lib_file : help.library_files) {

                    to_remove = null;

                    for (Swagger_ImportLibrary_Version_New.Library_File old_lib_file : lib_help.library_files) {

                        if(old_lib_file.file_name.equals(new_lib_file.file_name))
                            to_remove = old_lib_file;
                    }

                    if (to_remove != null) lib_help.library_files.remove(to_remove);

                    lib_help.library_files.add(new_lib_file);
                }

                file.delete();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode  content = Json.newObject();
                content.set("library_files", Json.toJson(lib_help.library_files) );

                // Content se nahraje na Azure
                Model_FileRecord.uploadAzure_Version(content.toString(), "library.json" , version_object.library.get_path() ,  version_object);
            }else if(version_object.files.size() == 0){

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode  content = Json.newObject();
                content.set("library_files", Json.toJson(help.library_files) );

                // Content se nahraje na Azure
                Model_FileRecord.uploadAzure_Version(content.toString(), "library.json" , version_object.library.get_path() ,  version_object);
            }

            // Uložení změn
            version_object.update();
            version_object.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove file to Version of ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "For removing Library files in Version of ImportLibrary.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_File_Load",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_removeFile(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_Library_File_Load> form = Form.form(Swagger_Library_File_Load.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Library_File_Load help = form.get();

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.forbidden_Permission();

            if(version_object.files.size() > 0) {

                Model_FileRecord file = version_object.files.get(0);
                //Uprava objektu
                JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                Form<Swagger_Library_File_Load> load_form = Form.form(Swagger_Library_File_Load.class).bind(json);
                if (form.hasErrors()) {return GlobalResult.formExcepting(load_form.errorsAsJson());}
                Swagger_Library_File_Load lib_help = load_form.get();

                List<Swagger_ImportLibrary_Version_New.Library_File> to_remove = new ArrayList<>();

                for (Swagger_ImportLibrary_Version_New.Library_File removed_file : help.library_files) {

                    for (Swagger_ImportLibrary_Version_New.Library_File lib_file : lib_help.library_files) {

                        if(lib_file.file_name.equals(removed_file.file_name))
                            to_remove.add(lib_file);
                    }
                }
                lib_help.library_files.removeAll(to_remove);

                file.delete();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode  content = Json.newObject();
                content.set("library_files", Json.toJson(lib_help.library_files) );

                // Content se nahraje na Azure
                Model_FileRecord.uploadAzure_Version(content.toString(), "library.json" , version_object.library.get_path() ,  version_object);
            }else if(version_object.files.size() == 0){
                return GlobalResult.result_BadRequest("Library has no files!");
            }

            // Uložení změn
            version_object.update();
            version_object.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload example to Version of ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "For linking examples to Version of ImportLibrary.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_edit"),
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_ImportLibrary_Version_Short_Detail.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_uploadExample(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_New> form = Form.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("ImportLibrary Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(version_object.library == null) return GlobalResult.result_BadRequest("Version_Object is not version of ImportLibrary");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.forbidden_Permission();

            Model_CProgram cProgram = Model_CProgram.find.where().eq("example_library.id", version_object.id).eq("name", help.version_name).findUnique();
            if (cProgram != null){
                if (cProgram.version_objects.size() > 0)
                    cProgram.version_objects.get(0).delete();
            }else {
                cProgram = new Model_CProgram();
                cProgram.name = help.version_name;
                cProgram.description = help.version_description;
                cProgram.example_library = version_object;

                cProgram.save();
            }

            cProgram.refresh();

            Model_VersionObject example = new Model_VersionObject();
            example.version_name = help.version_name;
            example.version_description = help.version_description;
            example.c_program = cProgram;
            example.public_version = true;
            example.date_of_create = new Date();

            example.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode  content = Json.newObject();
            content.put("main", help.main );
            //content.set("user_files", Json.toJson( help.user_files) );
            //content.set("library_files", Json.toJson(help.library_files) );

            // Content se nahraje na Azure

            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , cProgram.get_path() ,  example);
            example.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove example from Version of ImportLibrary",
            hidden = true,
            tags = {"ImportLibrary"},
            notes = "For deleting examples from Version of ImportLibrary.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImportLibrary_edit"),
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
    @Security.Authenticated(Secured_Admin.class)
    public Result importLibraryVersion_removeExample(@ApiParam(value = "example_id String query",   required = true)  String example_id){
        try{

            // Ověření objektu
            Model_CProgram cProgram = Model_CProgram.find.byId(example_id);
            if (cProgram == null) return GlobalResult.notFoundObject("Example example_id not found");

            // Zkontroluji validitu Verze zda sedí k ImportLibrary
            if(cProgram.example_library == null) return GlobalResult.result_BadRequest("Program is not example of ImportLibrary");

            Model_VersionObject returnObject = cProgram.example_library;

            // Kontrola oprávnění
            if(!cProgram.example_library.library.edit_permission()) return GlobalResult.forbidden_Permission();

            if (cProgram.version_objects.size() > 0)
                cProgram.version_objects.get(0).delete();

            cProgram.delete();

            returnObject.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(returnObject.get_short_import_library_version()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
