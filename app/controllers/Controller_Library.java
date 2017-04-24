package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.Model_FileRecord;
import models.Model_ImportLibrary;
import models.Model_VersionObject;
import models.Model_CProgram;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Library_state;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_ImportLibrary_List;
import utilities.swagger.outboundClass.Swagger_ImportLibrary_Short_Detail;
import utilities.swagger.outboundClass.Swagger_ImportLibrary_Version_Short_Detail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Security.Authenticated(Secured_API.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Library extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Library.class);

///###################################################################################################################*/
    
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
            library.state = Enum_Library_state.NEW;

            // Kontorluji oprávnění těsně před uložením
            if(!library.create_permission()) return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            library.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(library));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            version_object.author              = Controller_Security.get_person();
            version_object.date_of_create      = new Date();
            version_object.library             = library;
            version_object.public_version      = true;

            version_object.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content = Json.newObject();
            content.set("library_files", Json.toJson(help.library_files) );

            // Content se nahraje na Azure

            Model_FileRecord.uploadAzure_Version(content.toString(), "library.json" , library.get_path() ,  version_object);
            version_object.update();

            // Vracím vytvořený objekt
            return GlobalResult.created(Json.toJson(version_object.get_short_import_library_version()));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
        }
    }

}
