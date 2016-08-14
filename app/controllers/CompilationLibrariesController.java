package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.compiler.*;
import models.project.c_program.C_Compilation;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;
import utilities.Server;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Board_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_LibraryGroup_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Single_Library_List;
import utilities.swagger.outboundClass.*;

import java.io.File;
import java.util.*;

/**
 * Controller se zabívá správou knihoven, procesorů, desek (hardware), typů desek a jejich výrobcem.
 * Dále obsluhe kompilaci C++ kodu (propojení s kontrolerem Websocket)
 *
 */


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Secured_API.class)
public class CompilationLibrariesController extends Controller {

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
                                @ExtensionProperty(name = "C_program.create_permission", value = C_Program.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successful created",      response = C_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_C_Program(@ApiParam(value = "project_id String query", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Ověření projektu
            Project project = Project.find.byId(project_id);
            if(project == null ) return GlobalResult.notFoundObject("Project project_id not found");

            // Ověření Typu Desky
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(help.type_of_board_id);
            if(typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Tvorba programu
            C_Program c_program             = new C_Program();
            c_program.program_name          = help.program_name;
            c_program.program_description   = help.program_description;
            c_program.project               = project;
            c_program.dateOfCreate          = new Date();
            c_program.type_of_board         = typeOfBoard;


            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! c_program.create_permission())  return GlobalResult.forbidden_Permission();

            // Uložení C++ Programu
            c_program.save();

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
                            @ExtensionProperty(name = "C_program.read_permission", value = C_Program.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = C_Program.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_C_Program(@ApiParam(value = "c_program_id String query", required = true) String c_program_id) {
        try {

            // Vyhledám Objekt
            C_Program c_program = C_Program.find.byId(c_program_id);
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
                            @ExtensionProperty(name = "C_program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check permissions"),
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
    public Result get_C_Program_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true)  Integer page_number){

        try {

            // Získání JSON
            final Form<Swagger_C_Program_Filter> form = Form.form(Swagger_C_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<C_Program> query = Ebean.find(C_Program.class);
            query.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vyvoření odchozího JSON
            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return GlobalResult.internalServerError();
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
                            @ExtensionProperty(name = "C_program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check permissions"),
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

            Query<C_Program> query = Ebean.find(C_Program.class);
            query.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("project.id",project_id);

            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get C_program Version",
            tags = {"C_Program"},
            notes = "get Version of C_program by query = verison_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.Version.read_permission", value = Version_Object.read_permission_docs),
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result  get_C_Program_Version (@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Version_Object version_object = Version_Object.find.byId(version_id);
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
            @ApiResponse(code = 200, message = "Ok Result",    response = C_Program.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
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
    public Result edit_C_Program_Description(@ApiParam(value = "c_program_id String query", required = true)  String c_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Ověření objektu
            C_Program c_program = C_Program.find.byId(c_program_id);
            if(c_program == null ) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Ověření objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(help.type_of_board_id);
            if(typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // úprava objektu
            c_program.program_name = help.program_name;
            c_program.program_description = help.program_description;
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_C_Program_Version(@ApiParam(value = "version_id String query", required = true)  String c_program_id){
        try{

            // Zpracování Json
            Form<Swagger_C_Program_Version_New> form = Form.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            C_Program c_program = C_Program.find.byId(c_program_id);
            if(c_program == null) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Zkontroluji oprávnění
            if(!c_program.update_permission()) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Version_Object version_object      = new Version_Object();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.date_of_create = new Date();
            version_object.c_program = c_program;

            // Zkontroluji oprávnění
            if(!version_object.c_program.update_permission()) return GlobalResult.forbidden_Permission();

            version_object.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode  content = Json.newObject();
                        content.put("main", help.main );
                        content.set("user_files", Json.toJson( help.user_files) );
                        content.set("external_libraries", Json.toJson( help.external_libraries) );

            // Content se nahraje na Azure

            UtilTools.uploadAzure_Version(content.toString(), "code.json" , c_program.get_path() ,  version_object);


            String token = request().getHeader("X-AUTH-TOKEN"); // Určen pro exetrní vlákno zpracování
            version_object.compilation_in_progress = true;
            version_object.compilable = true;
            version_object.update();

            Thread compile_that = new Thread() {
                @Override
                public void run() {
                    try {

                       F.Promise<WSResponse> responsePromise = ws.url(Server.tyrion_serverAddress + "/compilation/c_program/version/compile/" + version_object.id)
                                .setContentType("undefined")
                                .setMethod("PUT")
                                .setHeader("X-AUTH-TOKEN", token)
                                .setRequestTimeout(50000)
                                .put("");

                        JsonNode result = responsePromise.get(50000).asJson();

                         // At se deje co se deje nakonci stejnak musím změnit stav compilation_in_progress = false.
                         // A objekt hledám znovu protože na druhe strane ho nekdo mohl updatovat o nove iformace a ty bych updatem na puvodnim objeektu přemazal
                         Version_Object update_version = Version_Object.find.byId(version_object.id);
                         update_version.compilation_in_progress = false;
                         update_version.update();
                         return;


                    }catch (Exception e){
                        logger.error("Error while cloud_compilation_server tried compile version of C_program", e);
                    }

                }
            };

            compile_that.start();

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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_C_Program_Version(@ApiParam(value = "version_id String query",   required = true)    String version_id){
        try{

            // Ověření objektu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smažu obsah konkrétní verze
            Product product = Product.find.where().eq("projects.c_programs.version_objects.id", version_id).findUnique();

            UtilTools.azureDelete( product.get_Container(), version_object.blob_version_link);

            version_object.c_program = null;

            // Smažu zástupný objekt
            version_object.delete();

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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Version_Object.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_C_Program_version( @ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();

            // Ověření objektu
            Version_Object version_object= Version_Object.find.byId(version_id);
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_C_Program(@ApiParam(value = "c_program_id String query", required = true)  String c_program_id){
        try{

            // Ověření objektu
            C_Program c_program = C_Program.find.byId(c_program_id);
            if(c_program == null ) return GlobalResult.notFoundObject("C_Program c_program_id not found");

            // Kontrola oprávnění
            if(!c_program.delete_permission()) return GlobalResult.forbidden_Permission();

            // Vyhledání PRoduct pro získání kontejneru
            Product product = Product.find.where().eq("projects.c_programs.id", c_program_id).findUnique();

            // Smazání z Azure
            UtilTools.azureDelete(product.get_Container(), c_program.get_path());

            System.out.println("budu mazat");

            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
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
                            @ExtensionProperty(name = "C_program.Version.read_permission", value = Version_Object.read_permission_docs),
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_BadRequest.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_version( @ApiParam(value = "version_id String query", required = true) String version_id ){
        try{

            logger.debug("Starting compilation on version_id = " + version_id);

            // Ověření objektu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");


            FileRecord file = FileRecord.find.where().eq("file_name", "code.json").eq("version_object.id", version_id).findUnique();
            if(file == null) return GlobalResult.notFoundObject("Server has no contenct from version");

            // Smažu předchozí kompilaci
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version is not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.read_permission()) return GlobalResult.forbidden_Permission();

            // Smažu předchozí kompilaci
            if(version_object.c_compilation != null) return GlobalResult.result_ok(Json.toJson( new Swagger_Compilation_Ok()));

            // Zpracování Json
            JsonNode json = Json.parse( file.get_fileRecord_from_Azure_inString() );
            Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
            Swagger_C_Program_Version_Update help = form.get();


            TypeOfBoard typeOfBoard = TypeOfBoard.find.where().eq("c_programs.id", version_object.c_program.id).findUnique();
            if(typeOfBoard == null) return GlobalResult.result_BadRequest("Version is not version of C_Program");


            logger.debug("Server has all the information it needs ");

            // Vytvářím objekt, jež se zašle přes websocket ke kompilaci
            ObjectNode result = Json.newObject();
                       result.put("messageType", "build");
                       result.put("target", typeOfBoard.name);
                       result.put("libVersion", "v0");
                       result.put("versionId", version_id);
                       result.put("code", help.main);
                       result.set("includes", help.includes() == null ? Json.newObject() : help.includes() );

            logger.debug("Server checks compilation cloud_compilation_server");
            // Kontroluji zda je nějaký kompilační cloud_compilation_server připojený
            if(WebSocketController_Incoming.compiler_cloud_servers.isEmpty()) {
                return GlobalResult.result_external_server_is_offline("Compilation cloud_compilation_server is offline!");
            }

            logger.debug("Server send c++ program for compilation");
            // Odesílám na compilační cloud_compilation_server

            NotificationController.starting_of_compilation(SecurityController.getPerson(), version_object);

            JsonNode compilation_result = WebSocketController_Incoming.compiler_server_make_Compilation(SecurityController.getPerson(), result);


            // V případě úspěšného buildu obsahuje příchozí JsonNode buildUrl
           if( compilation_result.has("buildUrl") ){

               NotificationController.successful_compilation(SecurityController.getPerson(), version_object);

               logger.debug("Build was succesfull");
               // Updatuji verzi - protože vše proběhlo v pořádku
               version_object.compilation_in_progress = false;
               version_object.compilable = true;
               version_object.update();

               // Vytvářím objekt hotové kompilace
               C_Compilation c_compilation        = new C_Compilation();
               c_compilation.c_comp_build_url     = compilation_result.get("buildUrl").asText();
               c_compilation.virtual_input_output = compilation_result.get("interface").toString();
               c_compilation.version_object       = version_object;
               c_compilation.dateOfCreate         = new Date();

               // Ukládám kompilační objekt


                logger.debug("Trying download bin file");
               try{

                   logger.debug("Sending request to Compilatin server for downloading");
                   F.Promise<WSResponse> responsePromise = ws.url(c_compilation.c_comp_build_url)
                           .setContentType("undefined")
                           .setRequestTimeout(2500)
                           .get();


                   byte[] body = responsePromise.get(2500).asByteArray();

                   logger.debug("Compilatin server respond successfuly WITH File");
                   if( body != null) {
                        // Daný soubor potřebuji dostat na Azure a Propojit s verzí

                          String binary_file_in_string = UtilTools.get_encoded_binary_string_from_body(body);
                          c_compilation.bin_compilation_file =  UtilTools.create_Binary_file( binary_file_in_string, "compilation.bin");
                          logger.debug("File succesfuly restored in Azure!");
                   }
               }catch (Exception e){
                   logger.warn("Došlo k chybě při stahování souboru", e);
               }

               c_compilation.save();

               return GlobalResult.result_ok(Json.toJson(new Swagger_Compilation_Ok() ));
           }
            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
           else if(compilation_result.has("buildErrors")){

               NotificationController.unsuccessful_compilation_warn( SecurityController.getPerson(), version_object, compilation_result.get("buildErrors").asText() );

               logger.debug("Build wasn't succesfull - buildErrors");
               version_object.compilable = false;
               version_object.update();

               Form< Swagger_Compilation_Build_Error> form_compilation =  Form.form(Swagger_Compilation_Build_Error.class).bind(compilation_result.get("buildErrors").get(0) );
               Swagger_Compilation_Build_Error swagger_compilation_build_error = form_compilation.get();


               return GlobalResult.result_buildErrors(Json.toJson( swagger_compilation_build_error ));

            // Nebylo úspěšné ani odeslání reqestu - Chyba v konfiguraci a tak vracím defaulní chybz
           }else if(compilation_result.has("error") ){

               NotificationController.unsuccessful_compilation_error( SecurityController.getPerson(), version_object,  compilation_result.get("error").asText());

               logger.debug("Build wasn't successful - error in communication");
               version_object.compilable = false;
               version_object.update();

               return GlobalResult.result_external_server_error(Json.toJson(compilation_result.get("error")));
           }

            logger.error("Build wasn't successful - unknown error!!");
            version_object.compilable = false;
            version_object.update();

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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_code(){
        try{

            // Zpracování Json
            Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Update help = form.get();

            // Ověření objektu
            if(help.type_of_board_id.isEmpty()) return GlobalResult.result_BadRequest("type_of_board_id is missing!");

            // Ověření objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(help.type_of_board_id);
            if(typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Vytvářím objekt, jež se zašle přes websocket ke kompilaci
            ObjectNode result = Json.newObject();
                       result.put("messageType", "build");
                       result.put("target", typeOfBoard.name);
                       result.put("libVersion", "v0");
                       result.put("code", help.main);
                       result.set("includes", help.includes() == null ? Json.newObject() : help.includes() );


            if(WebSocketController_Incoming.compiler_cloud_servers.isEmpty()){
                return GlobalResult.result_external_server_is_offline("Compilation cloud_compilation_server is offline!");
            }


            // Odesílám na compilační cloud_compilation_server
            JsonNode compilation_result = WebSocketController_Incoming.compiler_server_make_Compilation(SecurityController.getPerson(), result);

            // V případě úspěšného buildu obsahuje příchozí JsonNode buildUrl
            if(compilation_result.has("buildUrl")){
                return GlobalResult.result_ok();
            }

            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
            else if(compilation_result.has("buildErrors")){

                return GlobalResult.result_buildErrors(Json.toJson(compilation_result.get("buildErrors")));
            }

            // Nebylo úspěšné ani odeslání reqestu - Chyba v konfiguraci a tak vracím defaulní chybz
            else if(compilation_result.has("error") ){
                return GlobalResult.result_external_server_error(Json.toJson(compilation_result.get("error")));
            }

            // Neznámá chyba se kterou nebylo počítání
            return GlobalResult.result_BadRequest("Unknown error");
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }

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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 477, message = "External Cloud_Homer_server where is hardware is offline", response = Result_serverIsOffline.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result uploadBinaryFileToBoard(@ApiParam(value = "version_id ", required = true) String board_id ) {
        try{

            // Vyhledání objektů
            Board board = Board.find.byId(board_id);
            if(board == null) return GlobalResult.notFoundObject("Board board_id object not found");

            if(!board.update_permission())  return GlobalResult.forbidden_Permission();

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file_from_request = body.getFile("file");

            if (file_from_request == null) return GlobalResult.notFoundObject("Bin File not found!");

            File file = file_from_request.getFile();

            int dot = file_from_request.getFilename().lastIndexOf(".");
            String file_type = file_from_request.getFilename().substring(dot);
            String file_name = file_from_request.getFilename().substring(0,dot);

            // Zkontroluji soubor
            if(!file_type.equals(".bin")) return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if( (file.length() / 1024) > 500) return GlobalResult.result_BadRequest("File is bigger than 500K b");

            // Existuje Homer?
            ActualizationController.add_new_actualization_request(board.project, board, file, file_from_request.getFilename());

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Embedded Hardware with C_program compilation",
            tags = {"C_Program", "Actualization" },
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result uploadCompilationToBoard( @ApiParam(value = "version_id ", required = true) String version_id) {
        try {

            // Zpracování Json
            Form<Swagger_UploadBinaryFileToBoard> form = Form.form(Swagger_UploadBinaryFileToBoard.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_UploadBinaryFileToBoard help = form.get();


            // Ověření objektu
            Version_Object c_program_version = Version_Object.find.byId(version_id);
            if(c_program_version == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Zkontroluji oprávnění
            if(! c_program_version.c_program.read_permission())  return GlobalResult.forbidden_Permission();

            // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
            if(!c_program_version.compilable || c_program_version.compilation_in_progress) return GlobalResult.result_BadRequest("You cannot upload uncompilable version or compilation in progress");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(c_program_version.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");


            //Zkontroluji zda byla verze už zkompilována
            if(c_program_version.c_compilation == null) return GlobalResult.result_BadRequest("The program is not yet compiled");

            // Pokud nemám kompilaci a zároveň je kompilační cloud_compilation_server offline - oznámím nemožnost pokračovat
            if(c_program_version.c_compilation.bin_compilation_file == null && WebSocketController_Incoming.compiler_cloud_servers.isEmpty()){
                return GlobalResult.result_BadRequest("We have not your historic compilation and int the same time compilation cloud_compilation_server for compile your code is offline! So we cannot do anything now :((( ");
            }

            String typeOfBoard_id = c_program_version.c_program.type_of_board_id();

            // Vyhledání objektů
            List<Board> board_from_request = Board.find.where().idIn(help.board_id).findList();
            if(board_from_request.size() == 0) return GlobalResult.result_BadRequest("no device is available. Does not exist or is decommissioned.");

            // Vyseparované desky nad který lze provádět nějaké operace
            List<Board> board_for_update = Board.find.where().idIn(help.board_id).findList();
            // Kontrola oprávnění
            for(Board board : board_from_request){
                // Kontrola oprávnění
                if(board.update_permission() && board.type_of_board_id().equals(typeOfBoard_id)) board_for_update.add(board);
            }


            ActualizationController.add_new_actualization_request(c_program_version.c_program.project , board_for_update, c_program_version );

            // Vracím odpověď
            return GlobalResult.result_ok("Procedura byla spuštěna - uživatel bude informován!");

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


///###################################################################################################################*/

    @ApiOperation(value = "Create new Compilation Server",
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
            @ApiResponse(code = 201, message = "Successful created",      response = Cloud_Compilation_Server.class),
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
            Cloud_Compilation_Server server = new Cloud_Compilation_Server();
            server.server_name = help.server_name;
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + server.server_name;
            server.set_hash_certificate();

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
            @ApiResponse(code = 200, message = "Update successfuly",        response = Cloud_Compilation_Server.class),
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
            Cloud_Compilation_Server server = Cloud_Compilation_Server.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Zkontroluji oprávnění
            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            // Upravím objekt
            server.server_name = help.server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Compilation Servers",
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
            @ApiResponse(code = 200, message = "Ok Result",      response = Cloud_Compilation_Server.class, responseContainer = "List "),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Compilation_Server(){
        try{

            // Vyhledám všechny objekty
            List<Cloud_Compilation_Server> servers = Cloud_Compilation_Server.find.all();

            // Vracím Objekty
            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Compilation Servers",
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
            Cloud_Compilation_Server server = Cloud_Compilation_Server.find.byId(server_id);
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
            @ApiResponse(code = 201, message = "Successful created",      response = Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Processor() {
        try {

            // Zpracování Json
            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Vytvářím objekt
            Processor processor = new Processor();
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
            @ApiResponse(code = 200, message = "Ok Result",             response = Processor.class),
            @ApiResponse(code = 400, message = "Objects not found",     response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Processor(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Processor processor = Processor.find.byId(processor_id);
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Processor.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Processor_All() {
        try {

            //Vyhledám objekty
           List<Processor> processors = Processor.find.all();

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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result update_Processor(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Zpracování Json
            Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Kontroluji validitu
            Processor processor = Processor.find.byId(processor_id);
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Processor(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Kontroluji validitu
            Processor processor = Processor.find.byId(processor_id);
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

    @ApiOperation(value = "Create new LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new LibraryGroup. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "LibraryGroup_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = LibraryGroup.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_LibraryGroup() {
        try {

            // Zpracování Json
            Form<Swagger_LibraryGroup_New> form = Form.form(Swagger_LibraryGroup_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_New help = form.get();

            // Vytvářím objekt
            LibraryGroup library_Group = new LibraryGroup();
            library_Group.description = help.description;
            library_Group.group_name = help.group_name;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! library_Group.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            library_Group.save();

            // Vracím Objekt
            return GlobalResult.created(Json.toJson(library_Group));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Create new Version in LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new versinon in LibraryGroup query = libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "LibraryGroup_update" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Version_Object.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_LibraryGroup_Version(@ApiParam(value = "libraryGroup_id String query", required = true) String libraryGroup_id){
        try {

            // Zpracování Json
            Form<Swagger_LibraryGroup_Version> form = Form.form(Swagger_LibraryGroup_Version.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_Version help = form.get();

            // Kontrola Validity
            LibraryGroup library_group = LibraryGroup.find.byId(libraryGroup_id);
            if(library_group == null) return GlobalResult.notFoundObject("LibraryGroup library_group_id not found");

            // Kontrola oprávnění
            if(! library_group.update_permission())  return GlobalResult.forbidden_Permission();

            // Tvorba Verze
            Version_Object version_object      = new Version_Object();
            version_object.date_of_create      = new Date();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.library_group = library_group;

            // Uložení verze
            version_object.save();

            // Vložení verze do objektu
            library_group.version_objects.add(version_object);

            // Uložení objektu
            library_group.update();

            // Vracím Objekt
            return GlobalResult.created(Json.toJson(version_object));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload files to Version in LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "Its not possible now describe uploud file in Swagger. But file name must be longer than 5 chars." +
                    "in body of html content is \"files\"",
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
            @ApiResponse(code = 200, message = "Ok result"),
            @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result upload_Library_To_LibraryGroup(@ApiParam(required = true)  String version_id) {
        try {

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            List<Http.MultipartFormData.FilePart> files = body.getFiles();

            // Kontrola validity
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola validity
            if (version_object.library_group == null) return GlobalResult.result_BadRequest("Version object is not version of Library Group");
            LibraryGroup library_group = version_object.library_group;

            // Kontrola oprávnění
            if(! library_group.update_permission())  return GlobalResult.forbidden_Permission();

            for( Http.MultipartFormData.FilePart file :  files ) {

                // Control lenght of name
                String fileName = file.getFilename();
                if (fileName.length() < 5) return  GlobalResult.result_BadRequest("Too short file name");

                // Ještě kontrola souboru zda už tam není - > Version_Object a knihovny
                FileRecord fileRecord = FileRecord.find.where().in("version_object.id", version_object.id).ieq("file_name", fileName).findUnique();
                if (fileRecord != null) return GlobalResult.result_BadRequest("File exist in this version -> " + fileName + " please, create new version!");

                // Mám soubor
                File libraryFile = file.getFile();


                UtilTools.uploadAzure_Version(libraryFile, "library.txt", library_group.get_path(), version_object);

            }

            // Vracím Objekt
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want get LibraryGroup by query = libraryGroup_id",
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
            @ApiResponse(code = 200, message = "Ok result",                 response = LibraryGroup.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true)  String libraryGroup_id) {
        try {

            // Vyhledání knihovny
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject("LibraryGroup libraryGroup_id not found");

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(libraryGroup));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want delete LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "LibraryGroup_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) String libraryGroup_id) {
        try {

            // Kontrola validity
            LibraryGroup library_group = LibraryGroup.find.byId(libraryGroup_id);
            if(library_group == null) return GlobalResult.notFoundObject("LibraryGroup libraryGroup_id not found");

            // Kontrola oprávnění
            if(! library_group.delete_permission())  return GlobalResult.forbidden_Permission();

            // Smazání z Azure
            UtilTools.azureDelete(library_group.get_Container(),library_group.get_path());

            // Smazání objektu z DB
            library_group.delete();

            // Vracím Odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want edit LibraryGroup by query libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "LibraryGroup_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful created",      response = LibraryGroup.class),
            @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result editLibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true)  String libraryGroup_id) {
        try {

            // Zpracování Json
            final Form<Swagger_LibraryGroup_New> form = Form.form(Swagger_LibraryGroup_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_New help = form.get();

            // Kontrola validity
            LibraryGroup library_group = LibraryGroup.find.byId(libraryGroup_id);
            if(library_group == null) return GlobalResult.notFoundObject("LibraryGroup libraryGroup_id not found");

            // Kontrola oprávnění
            if(! library_group.edit_permission())  return GlobalResult.forbidden_Permission();

            // Úprava objektu
            library_group.description = help.description;
            library_group.group_name = help.group_name;

            // Uložení objektu
            library_group.save();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(library_group));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Libraries from LibraryGroup Version",
            tags = {"LibraryGroup"},
            notes = "If you want get Libraries from LibraryGroup.Version by query = version_id",
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
            @ApiResponse(code = 200, message = "Ok result",                 response = Version_Object.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_LibraryGroup_Version_Libraries(@ApiParam(required = true)  String version_id){
        try {

            // Kontrola validity
            Version_Object versionObject = Version_Object.find.byId(version_id);
            if(versionObject == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola zda se jendá o Lib
            if(versionObject.library_group == null ) return GlobalResult.notFoundObject(("Version is not version of Library Group"));

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(versionObject));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get LibraryGroup with Filters parameters",
            tags = {"LibraryGroup"},
            notes = "If you want get all or only some LibraryGroups you can use filter parameters in Json. But EveryTime server will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... in Json we help you with pages list \n ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_LibraryGroup_List.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_LibraryGroup_Filter( @ApiParam(value = "page_number is Integer. 1,2,3...n For first call, use 1 (first page of list) ",required = true)Integer page_number)  {
        try {

            // Zpracování Json
            Form<Swagger_LibraryGroup_Filter> form = Form.form(Swagger_LibraryGroup_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_Filter help = form.get();

            //Vytvářím Query parametr (definuji ORM objekt)
            Query<LibraryGroup> query = Ebean.find(LibraryGroup.class);

            // If contains confirms
            if (help.processors_id != null) {
                query.where().in("processors.id", help.processors_id);
            }

            if (help.group_name != null) {
                query.where().ieq("group_name", help.group_name);
            }
            if (help.order != null) {

                String order = help.order;
                String value = help.value;

                OrderBy<LibraryGroup> orderBy = new OrderBy<>();

                if (order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }

            // Vytvářím Objekt filtru
            Swagger_LibraryGroup_List result = new Swagger_LibraryGroup_List(query, page_number);

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get version from LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "get version from LibraryGroup",
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
            @ApiResponse(code = 200, message = "Ok result",      response = Version_Object.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_LibraryGroup_Version(@ApiParam(value = "version_id String query", required = true) String version_id){
        try {

            // Kontroluji validitu
            Version_Object version = Version_Object.find.byId(version_id);
            if(version == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(version));

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
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result fileRecord(@ApiParam(value = "file_record_id String query", required = true)  String file_record_id){
        try {

            // Kontrola validity objektu
            FileRecord fileRecord = FileRecord.find.fetch("version_object").where().eq("id", file_record_id).findUnique();
            if (fileRecord == null) return GlobalResult.notFoundObject("FileRecord file_record_id not found");

            // Swagger_File_Content - Zástupný dokumentační objekt

            // Vracím content
            return GlobalResult.result_ok(Json.toJson( fileRecord.get_fileRecord_from_Azure_inString()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/


    @ApiOperation(value = "create new SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "SingleLibrary_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = SingleLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_SingleLibrary() {
        try {

            // Zpracování Json
            final Form<Swagger_SingleLibrary_New> form = Form.form(Swagger_SingleLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_New help = form.get();

            // Vytvářím objekt
            SingleLibrary singleLibrary = new SingleLibrary();
            singleLibrary.library_name = help.library_name;
            singleLibrary.description = help.description;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! singleLibrary.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            singleLibrary.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(singleLibrary));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create new SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SingleLibrary.update_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "SingleLibrary_update" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {

            @ApiResponse(code = 201, message = "Successfully created",    response = Version_Object.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_SingleLibrary_Version(@ApiParam(value = "library_id String query", required = true)  String library_id){
        try {

            // Zpracování Json
            final Form<Swagger_SingleLibrary_Version> form = Form.form(Swagger_SingleLibrary_Version.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_Version help = form.get();

            // Vyhledám Objekt
            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null)  return GlobalResult.notFoundObject("SingleLibrary library_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! singleLibrary.update_permission())  return GlobalResult.forbidden_Permission();

            // Vytvářím novou verzi
            Version_Object version_object = new Version_Object();
            version_object.date_of_create = new Date();
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;
            version_object.single_library = singleLibrary;

            // Ukládám objekt
            version_object.save();

            // vracím objekt
            return GlobalResult.created(Json.toJson(version_object));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "Upload file to SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "Upload file to SingleLibrary in file format",
            produces = "application/json",
            consumes = "multipart/form-data",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SingleLibrary.update_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "SingleLibrary_update" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            required = true,
                            paramType = "body",
                            value = "File in file.txt format"
                    )
            }
    )
    @ApiResponses(value = {

            @ApiResponse(code = 200, message = "Successfully uploaded",   response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result upload_SingleLibrary_Version( @ApiParam(required = true)  String version_id){
        try{

            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");


            // If fileRecord group is not null
            Version_Object version_object = Version_Object.find.byId(version_id);
            if(version_object == null ) return GlobalResult.result_BadRequest("Version_Object with version_id not exist");

            // Kontrola zda je verze verzí single library
            if(version_object.single_library == null ) return GlobalResult.result_BadRequest("Version is not version of SingleLibrary");

            // ZDa už neobsahuje soubor
            if (version_object.files.size() > 0) return GlobalResult.result_BadRequest("Version_Object has file already.. Create new Version_Object ");
            version_object.date_of_create = new Date();
            version_object.update();

            // Kontrola jména
            String fileName = file.getFilename();
            if(fileName.length()< 5 )return GlobalResult.result_BadRequest("Too short FileName -> " + fileName);

            // Tvorba souboru
            File libraryFile = file.getFile();

            // Nahraji soubor na Azure

            UtilTools.uploadAzure_Version(libraryFile, "library.txt",  version_object.single_library.get_path() ,version_object);

            // Vrácení verze
            return GlobalResult.result_ok(Json.toJson(version_object));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want get SingleLibrary by query = library_id",
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
            @ApiResponse(code = 200, message = "Ok result",                response = SingleLibrary.class),
            @ApiResponse(code = 400, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",     response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_SingleLibrary(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Kontrola objektu
            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject("SingleLibrary library_id not found" );

            // Kontrola oprávnění
            if(!singleLibrary.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(singleLibrary));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Library with Filters parameters",
            tags = {"SingleLibrary"},
            notes = "If you want get all or only some SingleLibraries you can use filter parameters in Json. But EveryTime i will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... I will give you also" +
                    "information how many results you can show \n ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_Single_Library_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_SingleLibrary_Filter( @ApiParam(value = "page_number is Integer. 1,2,3...n For first call, use 1",required = true) Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_SingleLibrary_Filter> form = Form.form(Swagger_SingleLibrary_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_Filter help = form.get();

            Query<SingleLibrary> query = Ebean.find(SingleLibrary.class);

            if (help.processors_id != null) {
                query.where().in("processors.id", help.processors_id);
            }

            if (help.library_name != null) {
                query.where().ieq("library_name", help.library_name);
            }

            if (help.order != null) {

                String order = help.order;
                String value = help.value;

                OrderBy<SingleLibrary> orderBy = new OrderBy<>();

                if (order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }

            // Skládám Filt seznam
            Swagger_Single_Library_List result = new Swagger_Single_Library_List(query, page_number);

            return GlobalResult.result_ok(Json.toJson(result));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want edit name or description of SingleLibrary by query = library_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SingleLibrary.edit_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "SingleLibrary_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = SingleLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_SingleLibrary(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Zpracování Json
            final Form<Swagger_SingleLibrary_New> form = Form.form(Swagger_SingleLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_New help = form.get();

            // Vyhledám Objekt
            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject("SingleLibrary library_id not found" );


            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! singleLibrary.edit_permission())  return GlobalResult.forbidden_Permission();

            // Uprava objektu
            singleLibrary.library_name = help.library_name;
            singleLibrary.description = help.description;

            // uložení objektu
            singleLibrary.update();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(singleLibrary));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "If you want delete SingleLibrary by query = library_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SingleLibrary.delete_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "SingleLibrary_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_SingleLibrary(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Vyhledám Objekt
            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject("SingleLibrary library_id not found" );

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! singleLibrary.delete_permission())  return GlobalResult.forbidden_Permission();

            // Smažu z Azure
            UtilTools.azureDelete(singleLibrary.get_Container(), singleLibrary.get_path());

            // Smažu z databáze
            singleLibrary.delete();

            // Vracím potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

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
            @ApiResponse(code = 201, message = "Successfully created",    response = Producer.class),
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
            Producer producer = new Producer();
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Producer.class),
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
            Producer producer = Producer.find.byId(producer_id);
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Producer.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producers() {
        try {

            // Získání seznamu
            List<Producer> producers = Producer.find.all();

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
            @ApiResponse(code = 200, message = "Ok Result",               response = Producer.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producer(@ApiParam(required = true)  String producer_id) {
        try {

            // Kontrola objektu
            Producer producer = Producer.find.byId(producer_id);
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Producer(@ApiParam(required = true) String producer_id) {
        try {

            // Kontrola objektu
            Producer producer = Producer.find.byId(producer_id);
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
            @ApiResponse(code = 201, message = "Successfully created",    response = TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_TypeOfBoard() {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Producer producer = Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Processor processor = Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Tvorba objektu
            TypeOfBoard typeOfBoard = new TypeOfBoard();
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
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_TypeOfBoard(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontrola objektu
            Producer producer = Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Processor processor = Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.edit_permission()) return GlobalResult.forbidden_Permission();

            // Uprava objektu
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_TypeOfBoard(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
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
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_TypeOfBoard_all() {
        try {

            // Získání seznamu
            List<TypeOfBoard> typeOfBoards = TypeOfBoard.find.all();

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
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_TypeOfBoard(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola validity objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board",
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
            @ApiResponse(code = 201, message = "Successful created",      response = Board.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Board() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New> form = Form.form(Swagger_Board_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_New help = form.get();

            // Kotrola objektu
            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId( help.type_of_board_id  );
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.register_new_device_permission()) return GlobalResult.forbidden_Permission();

            // Odstraním duplikáty ze seznamu
            Set<String> hs = new HashSet<>();
            hs.addAll(help.hardware_unique_ids);
            help.hardware_unique_ids.clear();
            help.hardware_unique_ids.addAll(hs);

            List<Board> exist_boards = Board.find.where().in( "id", help.hardware_unique_ids).findList();

            for(Board board : exist_boards)  help.hardware_unique_ids.remove( board.id );

            // Seznam vytvořené
            List<Board> created_board = new ArrayList<>();

            // Vytvoření desky
            for(String hw_id : help.hardware_unique_ids) {
                Board board = new Board();
                board.id = hw_id;
                board.isActive = false;
                board.date_of_create = new Date();
                board.type_of_board = typeOfBoard;

                // Uložení desky do DB
                board.save();

                // Přidáno do seznamu který budu vracet
                created_board.add(board);
            }

            // Vracím seznam zařízení k registraci
            return GlobalResult.created(Json.toJson(created_board));

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
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Board_User_Description(@ApiParam(required = true)  String board_id){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Personal> form = Form.form(Swagger_Board_Personal.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Personal help = form.get();

            // Kotrola objektu
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(board.edit_permission()) return GlobalResult.forbidden_Permission();

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
    public Result get_Board_Filter(@ApiParam(value = "page_number is Integer. Contain  1,2...n. For first call, use 1", required = false)  Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = Form.form(Swagger_Board_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Board> query = Ebean.find(Board.class);

            // If Json contains TypeOfBoards list of id's
            if(help.typeOfBoards != null ){
                query.where().in("type_of_board.id", help.typeOfBoards);
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
            Swagger_Board_List result = new Swagger_Board_List(query, page_number);

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
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result deactivate_Board(@ApiParam(required = true)  String board_id) {
        try {

            // Kotrola objektu
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(board.update_permission()) return GlobalResult.forbidden_Permission();

            // Úprava stavu
            board.isActive = false;

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
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Board(@ApiParam(required = true) String board_id) {
        try {

            // Kotrola objektu
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.read_permission()) return GlobalResult.forbidden_Permission();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

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
                            @ExtensionProperty(name = "Board_Connection", value = Board.connection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.first_connect_permission", value = "true"),
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result connect_Board_with_Project(@ApiParam(required = true)   String board_id, @ApiParam(required = true) String project_id){
        try {

            // Kotrola objektu
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kotrola objektu
            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if(!board.first_connect_permission()) return GlobalResult.result_BadRequest("Board is already registered");

            // Kontrola oprávnění
            if(!project.update_permission()) return GlobalResult.forbidden_Permission();

            // uprava desky
            board.project = project;
            project.boards.add(board);

            // Update databáze -> propojení
            project.update();
            board.update();

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
                            @ExtensionProperty(name = "Board_Disconnection", value = Board.disconnection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result disconnect_Board_from_Project(@ApiParam(required = true)   String board_id){
        try {

            // Kontrola objektu
            Board board = Board.find.byId(board_id);
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
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_all_details_for_blocko(@ApiParam(required = true)   String project_id){
        try {

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return GlobalResult.forbidden_Permission();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.boards = project.boards;
            boards_for_blocko.typeOfBoards = TypeOfBoard.find.where().eq("boards.project.id", project.id ).findList();
            boards_for_blocko.c_programs = project.c_programs;

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
