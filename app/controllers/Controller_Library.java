package controllers;

import com.google.inject.Inject;
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
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Library_Version;
import utilities.swagger.output.filter_results.Swagger_Library_List;


@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Library extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Library.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Library(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

///############################################################################################################š#######*/
    
    @ApiOperation(value = "create Library",
            tags = {"Library"},
            notes = "Create Library for C programs ",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Library_New",
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
            final Form<Swagger_Library_New> form = formFactory.form(Swagger_Library_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Library_New help = form.get();

            // Vytvářím objekt
            Model_Library library = new Model_Library();
            library.name = help.name;
            library.description = help.description;
            library.publish_type = ProgramType.PRIVATE;

            if (help.project_id != null) {

                Model_Project project = Model_Project.getById(help.project_id);
                if (project == null || !project.update_permission()) return notFound("Project project_id not found");
                library.project = project;
            }

            for (String type_of_board_id : help.type_of_board_ids) {

                Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(type_of_board_id);
                if (typeOfBoard != null) {

                    library.type_of_boards.add(typeOfBoard);
                }
            }

            // Kontorluji oprávnění těsně před uložením
            if (!library.create_permission()) return forbiddenEmpty();

            // Ukládám objekt
            library.save();

            // Vracím objekt
            return created(Json.toJson(library));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


    @ApiOperation(value = "make_Clone Library",
            tags = {"Library"},
            notes = "clone Library for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Library_Copy",
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
    public Result library_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_Library_Copy> form = formFactory.form(Swagger_Library_Copy.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Library_Copy help = form.get();

            // Vyhledám Objekt
            Model_Library library_old = Model_Library.getById(help.library_id);
            if (library_old == null) return notFound("Model_Library library not found");

            // Zkontroluji oprávnění
            if (!library_old.read_permission())  return forbiddenEmpty();

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if (!project.update_permission())  return forbiddenEmpty();


            Model_Library library_new =  new Model_Library();
            library_new.name = help.name;
            library_new.description = help.description;
            library_new.type_of_boards = library_old.type_of_boards;
            library_new.project = project;
            library_new.save();

            library_new.refresh();


            for (Model_Version version : library_old.versions) {

                Model_Version copy_object = new Model_Version();
                copy_object.name            = version.name;
                copy_object.description     = version.description;
                copy_object.library         = library_new;
                copy_object.public_version  = false;
                copy_object.author          = version.author;

                // Zkontroluji oprávnění
                copy_object.save();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version.files.get(0);

                Model_Blob.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "library.json" , library_new.get_path() ,  copy_object);
                copy_object.update();

            }

            library_new.refresh();

            // Vracím Objekt
            return ok(Json.toJson(library_new));

        } catch (Exception e) {
            return internalServerError(e);
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
            Model_Library library = Model_Library.getById(library_id);
            if (library == null ) return notFound("Library not found");

            // Vrácneí objektu
            return ok(Json.toJson(library));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Library Short List by filter",
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
                            dataType = "utilities.swagger.input.Swagger_Library_Filter",
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
            final Form<Swagger_Library_Filter> form = formFactory.form(Swagger_Library_Filter.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Library_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Library> query = Ebean.find(Model_Library.class);

            query.orderBy("UPPER(name) ASC");


            if (!help.type_of_board_ids.isEmpty()) {
                query.where().in("type_of_boards.id", help.type_of_board_ids);
            }

            if (help.project_id != null && !help.project_id.equals("")) {

                Model_Project project = Model_Project.getById(help.project_id);
                if (project == null )return notFound("Project not found");
                if (!project.read_permission())return forbiddenEmpty();

                query.where().eq("project_id", help.project_id).eq("deleted", false);

            } else {
                query.where().isNull("project_id");
            }

            if (help.public_library) {
                query.where().isNull("project_id").eq("deleted", false).eq("publish_type", ProgramType.PUBLIC.name());
            }

            if (help.pending_library) {
                if (!BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return forbiddenEmpty();
                query.where().eq("versions.approval_state", Approval.PENDING.name());
            }

            // Vyvoření odchozího JSON
            Swagger_Library_List result = new Swagger_Library_List(query,page_number);

            // Vrácneí objektu
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Library",
            tags = {"Library"},
            notes = "Edit Library name and description",
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
                            dataType = "utilities.swagger.input.Swagger_Library_New",
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
    public Result library_edit(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Library_New> form = formFactory.form(Swagger_Library_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Library_New help = form.get();

            // Vyhledání objektu
            Model_Library library = Model_Library.getById(library_id);
            if (library == null) return notFound("Library not found");

            // Kontrola oprávnění
            if (!library.edit_permission()) return forbiddenEmpty();

            // Change values
            library.name = help.name;
            library.description = help.description;

            // Uložení změn
            library.update();

            // Vrácení objektu
            return ok(Json.toJson(library));

        } catch (Exception e) {
            return internalServerError(e);
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
            Model_Library library = Model_Library.getById(library_id);
            if (library == null ) return notFound("Library not found");

            // Kontrola oprávnění
            if (!library.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            library.deleted = true;
            library.update();

            // Vrácneí potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

/// VERSIONS IN LIBRARIES ##############################################################################################*/

    @ApiOperation(value = "create Library_Version",
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
                            dataType = "utilities.swagger.input.Swagger_Library_Version_New",
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
    public Result library_version_create(@ApiParam(value = "library_id String query", required = true)  String library_id) {
        try {

            // Zpracování Json
            Form<Swagger_Library_Version_New> form = formFactory.form(Swagger_Library_Version_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Library_Version_New help = form.get();

            // Ověření objektu
            Model_Library library = Model_Library.getById(library_id);
            if (library == null) return notFound("Library library_id not found");

            // Zkontroluji oprávnění
            if (!library.update_permission()) return forbiddenEmpty();

            // První nová Verze
            Model_Version version = new Model_Version();
            version.name             = help.name;
            version.description      = help.description;
            version.author           = person();
            version.library          = library;
            version.public_version   = false;

            version.save();

            Swagger_Library_File_Load library_file_collection = new Swagger_Library_File_Load();
            library_file_collection.files = help.files;

            Model_Blob.uploadAzure_Version(Json.toJson(library_file_collection).toString(), "library.json" , library.get_path() ,  version);

            version.refresh();

            library.refresh();

            // Vracím vytvořený objekt
            return created(Json.toJson(version.library.library_version(version)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Library_Version",
            tags = {"Library"},
            notes = "get Version of Library by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "Library.Version.read_permission", value = Model_Version.read_permission_docs),
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
    public Result library_version_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version_Object version not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if (version_object.library == null) return badRequest("Version_Object is not version of Library");

            // Zkontroluji oprávnění
            if (! version_object.library.read_permission())  return forbiddenEmpty();

            // Vracím Objekt
            return ok(Json.toJson(version_object.library.library_version(version_object)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Library_Version",
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
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
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
    public Result library_version_edit(@ApiParam(value = "version_id String query",   required = true)  String version_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Ověření objektu
            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if (version.library == null) return badRequest("Version_Object is not version of Library");

            // Kontrola oprávnění
            if (!version.library.edit_permission()) return forbiddenEmpty();

            //Uprava objektu
            version.name = help.name;
            version.description = help.description;

            // Uložení změn
            version.update();

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Library_Version",
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
    public Result library_version_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id) {
        try {

            // Ověření objektu
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k Library
            if (version_object.library == null) return badRequest("Version_Object is not version of Library");

            // Kontrola oprávnění
            if (!version_object.library.delete_permission()) return forbiddenEmpty();

            // Smažu zástupný objekt
            version_object.delete();

            // Vracím potvrzení o smazání
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


/// Public Libraries

    @ApiOperation(value = "make Library_Version public",
            tags = {"Library"},
            notes = "Make Library public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_version_make_public(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Kontrola objektu
            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version not found");

            if (version.library == null )return notFound("Version not found");


            if (Model_Version.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("library.project.participants.person.id", BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 Libraries. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (version.approval_state != null) {
                return badRequest("You cannot publish same program twice!");
            }

            // Úprava objektu
            version.approval_state = Approval.PENDING;

            // Kontrola oprávnění
            if (!(version.library.edit_permission())) return forbiddenEmpty();

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


    @ApiOperation(value = "edit Library_Version Response publication",
            tags = {"Admin-Library"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Community_Version_Publish_Response",
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
    public Result library_public_response() {
        try {

            // Získání Json
            final Form<Swagger_Community_Version_Publish_Response> form = formFactory.form(Swagger_Community_Version_Publish_Response.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Community_Version_Publish_Response help = form.get();

            // Kontrola objektu
            Model_Version version_old = Model_Version.getById(help.version_id);
            if (version_old == null) return notFound("Version not found");

            // Ověření objektu


            Model_Library library_old = version_old.library;
            if (library_old == null) return notFound("Library Librarry not found");

            // Zkontroluji oprávnění

            if (!library_old.community_publishing_permission()) {
                return forbiddenEmpty();
            }

            if (help.decision) {

                System.out.println("help.decision je true!!!");

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval.APPROVED;
                version_old.update();


                Model_Library library = Model_Library.getById(library_old.id); // TODO + "_public_copy");

                if (library == null) {
                    library = new Model_Library();
                    // library.id = library_old.id + "_public_copy"; TODO
                    library.name = help.program_name;
                    library.description = help.program_description;
                    library.publish_type  = ProgramType.PUBLIC;
                    library.save();
                }


                Model_Version version_object = new Model_Version();
                version_object.name             = help.version_name;
                version_object.description      = help.version_description;
                version_object.library          = library;
                version_object.public_version   = true;
                version_object.author           = version_old.author;

                // Zkontroluji oprávnění
                version_object.save();

                library.refresh();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version_old.files.get(0);


                Model_Blob.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , library.get_path() ,  version_object);
                version_object.update();

                version_object.compile_program_thread(version_old.compilation.firmware_version_lib);

                // Admin to schválil bez dalších keců
                if ((help.reason == null || help.reason.length() < 4)) {
                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("Library Name: ") +        library_old.name + Email.newLine() +
                                        Email.bold("Library Description: ") + library_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +        library_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") + library_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible.")
                                .text(Email.bold("Thanks!") + Email.newLine() + person().full_name())
                                .send(version_old.get_c_program().get_project().get_product().customer, "Publishing your program" );

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }


                    // Admin to schválil ale měl nějaký keci k tomu
                } else {

                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("Library Name: ") +          library_old.name + Email.newLine() +
                                        Email.bold("Library Description: ") +   library_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +          library_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") +   library_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible. We also had to make some changes to your program or rename something.")
                                .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                                .text(Email.bold("Thanks!") + Email.newLine() + BaseController.person().full_name())
                                .send(version_old.get_c_program().get_project().get_product().customer, "Publishing your program" );

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

            } else {

                version_old.approval_state = Approval.DISAPPROVED;
                version_old.update();

                try {

                    new Email()
                            .text("First! Thank you for publishing your program!")
                            .text(Email.bold("Library Name: ") + library_old.name + Email.newLine() +
                                    Email.bold("Library Description: ") + library_old.name + Email.newLine() +
                                    Email.bold("Version Name: ") + library_old.name + Email.newLine() +
                                    Email.bold("Version Description: ") + library_old.name + Email.newLine())
                            .divider()
                            .text("We are sorry, but we found some problems in your program, so we did not publish it. But do not worry and do not give up! " +
                                    "We are glad that you want to contribute to our public libraries. Here are some tips what to improve, so you can try it again.")
                            .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                            .text(Email.bold("Thanks!") + Email.newLine() + person().full_name())
                            .send(version_old.get_c_program().get_project().get_product().customer, "Publishing your program");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

            // Potvrzení
            return  okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}
