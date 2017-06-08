package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Library_List;
import utilities.swagger.outboundClass.Swagger_Library_Version_Short_Detail;

import java.util.Date;


@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Library extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Library.class);

///############################################################################################################š#######*/
    
    @ApiOperation(value = "create new Library",
            tags = {"Library"},
            notes = "TODO",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Library_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Library_New> form = Form.form(Swagger_Library_New.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Library_New help = form.get();



            // Vytvářím objekt
            Model_Library library = new Model_Library();
            library.name = help.name;
            library.description = help.description;

            if(help.project_id != null){

                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null || !project.update_permission()) return GlobalResult.result_notFound("Project project_id not found");
                library.project_id = project.id;
            }

            for (String type_of_board_id : help.type_of_board_ids) {

                Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
                if (typeOfBoard != null) {

                    library.type_of_boards.add(typeOfBoard);
                }
            }

            // Kontorluji oprávnění těsně před uložením
            if(!library.create_permission()) return GlobalResult.result_forbidden();

            // Ukládám objekt
            library.save();

            // Vracím objekt
            return GlobalResult.result_created(Json.toJson(library));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Library",
            tags = {"Library"},
            notes = "if you want to get Library.",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Library.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result library_get(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(library_id);
            if(library == null ) return GlobalResult.result_notFound("Library not found");

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(library));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get List of Libraries details by filter",
            tags = {"Library"},
            notes = "if you want to get Libraries filtered by specific parameters. For private Libraries under project set project_id, for all public use empty JSON",
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
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Library_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result library_getByFilter(@ApiParam(value = "page_number is Integer. Contain  1,2...n. For first call, use 1", required = false)  int page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Library_Filter> form = Form.form(Swagger_Library_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Library_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Library> query = Ebean.find(Model_Library.class);
            query.where().eq("removed_by_user", false);

            if (!help.type_of_board_ids.isEmpty())
                query.where().in("type_of_boards.id", help.type_of_board_ids);

            if(help.project_id != null){
                if(help.inlclude_public) {
                    query.where().or(
                            com.avaje.ebean.Expr.eq("project_id", help.project_id),
                            com.avaje.ebean.Expr.isNull("project_id")
                    );
                } else {
                    query.where().eq("project_id", help.project_id);
                }
            } else {
                query.where().isNull("project_id");
            }





            // Vyvoření odchozího JSON
            Swagger_Library_List result = new Swagger_Library_List(query,page_number);

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Library",
            tags = {"Library"},
            notes = "TODO",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Library_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_update(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Library_New> form = Form.form(Swagger_Library_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Library_New help = form.get();

            // Vyhledání objektu
            Model_Library library = Model_Library.find.byId(library_id);
            if (library == null) return GlobalResult.result_notFound("Library not found");

            // Kontrola oprávnění
            if(!library.edit_permission()) return GlobalResult.result_forbidden();

            library.name = help.name;
            library.description = help.description;

            // Uložení změn
            library.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(library));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Library",
            tags = {"Library"},
            notes = "For remove Library",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Library_delete"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result library_delete(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(library_id);
            if(library == null ) return GlobalResult.result_notFound("Library not found");

            // Kontrola oprávnění
            if (!library.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            library.removed_by_user = true;
            library.update();

            // Vrácneí potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }


/// VERSIONS IN LIBRARIES ##############################################################################################*/

    @ApiOperation(value = "new Version of Library",
            tags = {"Library"},
            notes = "If you want add new code to Library",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "Library_update"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Library_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_Library_Version.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result libraryVersion_create(@ApiParam(value = "library_id String query", required = true)  String library_id){
        try{

            // Zpracování Json
            Form<Swagger_Library_Version_New> form = Form.form(Swagger_Library_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Library_Version_New help = form.get();

            // Ověření objektu
            Model_Library library = Model_Library.find.byId(library_id);
            if(library == null) return GlobalResult.result_notFound("Library library_id not found");

            // Zkontroluji oprávnění
            if(!library.update_permission()) return GlobalResult.result_forbidden();

            // První nová Verze
            Model_VersionObject version_object = new Model_VersionObject();
            version_object.version_name        = help.version_name;
            version_object.version_description = help.version_description;
            version_object.author              = Controller_Security.get_person();
            version_object.date_of_create      = new Date();
            version_object.library             = library;
            version_object.public_version      = true;

            version_object.save();

            Swagger_Library_File_Load library_file_collection = new Swagger_Library_File_Load();
            library_file_collection.files = help.files;

            Model_FileRecord.uploadAzure_Version(Json.toJson(library_file_collection).toString(), "library.json" , library.get_path() ,  version_object);

            // Vracím vytvořený objekt
            return GlobalResult.result_created(Json.toJson(version_object.library.library_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Library Version",
            tags = {"Library"},
            notes = "get Version of Library by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "Library.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "ImporLibrary_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Library_Version.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result libraryVersion_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.result_notFound("Version_Object version_object not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if(version_object.library == null) return GlobalResult.result_badRequest("Version_Object is not version of Library");

            // Zkontroluji oprávnění
            if(! version_object.library.read_permission())  return GlobalResult.result_forbidden();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(version_object.library.library_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update basic information in Version of Library",
            tags = {"Library"},
            notes = "For update basic (name and description) information in Version of Library. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "Library_edit"),
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Library_Version.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result libraryVersion_update(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();

            // Ověření objektu
            Model_VersionObject version_object= Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if(version_object.library == null) return GlobalResult.result_badRequest("Version_Object is not version of Library");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.result_forbidden();

            //Uprava objektu
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            // Uložení změn
            version_object.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.library.library_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Version in Library",
            tags = {"Library"},
            notes = "delete Library by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "Library_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response =  Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result libraryVersion_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id){
        try{

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if(version_object.library == null) return GlobalResult.result_badRequest("Version_Object is not version of Library");

            // Kontrola oprávnění
            if(!version_object.library.delete_permission()) return GlobalResult.result_forbidden();

            version_object.removed_by_user = true;

            // Smažu zástupný objekt
            version_object.update();

            // Vracím potvrzení o smazání
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }



/// EXAMPLES IN LIBRARIES ##############################################################################################*/

    @ApiOperation(value = "upload example to Version of Library",
            hidden = true,
            tags = {"Library"},
            notes = "For linking examples to Version of Library.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "Library_edit"),
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Library_Version_Short_Detail.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result libraryVersion_uploadExample(@ApiParam(value = "version_id String query",   required = true)  String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_C_Program_Version_New> form = Form.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Library Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if(version_object.library == null) return GlobalResult.result_badRequest("Version_Object is not version of Library");

            // Kontrola oprávnění
            if(!version_object.library.edit_permission()) return GlobalResult.result_forbidden();

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
            content.set("files", Json.toJson( help.files) );
            content.set("imported_libraries", Json.toJson(help.imported_libraries) );

            // Content se nahraje na Azure

            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , cProgram.get_path() ,  example);
            example.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_short_library_version()));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove example from Version of Library",
            hidden = true,
            tags = {"Library"},
            notes = "For deleting examples from Version of Library.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "Library_edit"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result libraryVersion_removeExample(@ApiParam(value = "example_id String query",   required = true)  String example_id){
        try{

            // Ověření objektu
            Model_CProgram cProgram = Model_CProgram.find.byId(example_id);
            if (cProgram == null) return GlobalResult.result_notFound("Example example_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if(cProgram.example_library == null) return GlobalResult.result_badRequest("Program is not example of Library");

            Model_VersionObject returnObject = cProgram.example_library;

            // Kontrola oprávnění
            if(!cProgram.example_library.library.edit_permission()) return GlobalResult.result_forbidden();

            if (cProgram.version_objects.size() > 0)
                cProgram.version_objects.get(0).delete();

            cProgram.delete();

            returnObject.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(returnObject.get_short_library_version()));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

}
