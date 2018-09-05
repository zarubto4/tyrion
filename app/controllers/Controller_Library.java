package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Junction;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_C_Program_List;
import utilities.swagger.output.filter_results.Swagger_Library_List;

import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Library extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Library.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Library(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
    }

// LIBRARY #############################################################################################################
    
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
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_create() {
        try {

            // Get and Validate Object
            Swagger_Library_New help = formFromRequestWithValidation(Swagger_Library_New.class);

            // Vytvářím objekt
            Model_Library library = new Model_Library();
            library.name = help.name;
            library.description = help.description;
            library.publish_type = ProgramType.PRIVATE;

            if (help.project_id != null) {
                Model_Project project = Model_Project.find.byId(help.project_id);
                library.project = project;
            }

            for (UUID hardware_type_id : help.hardware_type_ids) {

                Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_id);
                if (hardwareType != null) {
                    library.hardware_types.add(hardwareType);
                }
            }

            // Ukládám objekt
            library.save();

            library.setTags(help.tags);

            // Vracím objekt
            return created(library);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone Library",
            tags = {"Library"},
            notes = "clone Library for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_clone() {
        try {

            // Get and Validate Object
            Swagger_Library_Copy help = formFromRequestWithValidation(Swagger_Library_Copy.class);

            // Vyhledám Objekt
            Model_Library library_old = Model_Library.find.byId(help.library_id);

            // Vyhledám Objekt
            Model_Project project = Model_Project.find.byId(help.project_id);

            Model_Library library_new =  new Model_Library();
            library_new.name = help.name;
            library_new.description = help.description;
            library_new.hardware_types = library_old.hardware_types;
            library_new.project = project;
            library_new.save();

            library_new.refresh();


            for (Model_LibraryVersion version : library_old.versions) {

                Model_LibraryVersion copy_object = new Model_LibraryVersion();
                copy_object.name            = version.name;
                copy_object.description     = version.description;
                copy_object.library         = library_new;
                copy_object.publish_type    = ProgramType.PRIVATE;
                copy_object.author_id          = version.author_id;

                // Zkontroluji oprávnění
                copy_object.save();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version.file;

                copy_object.file = Model_Blob.upload(fileRecord.get_fileRecord_from_Azure_inString(), "library.json" , library_new.get_path());
                copy_object.update();

            }

            library_new.refresh();

            // Vracím Objekt
            return ok(library_new);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Library",
            tags = {"Library"},
            notes = "if you want to get Library.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Library.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_get(UUID library_id) {
        try {

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(library_id);

            // Vrácneí objektu
            return ok(library);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Library List by Filter",
            tags = {"Library"},
            notes = "if you want to get Libraries filtered by specific parameters. For private Libraries under project set project_id, for all public use empty JSON",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Library_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_getByFilter(@ApiParam(value = "page_number is Integer. Contain  1,2...n. For first call, use 1", required = false)  int page_number) {
        try {

            // Get and Validate Object
            Swagger_Library_Filter help = formFromRequestWithValidation(Swagger_Library_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(help.project_id != null || help.public_library || help.pending_library)) {
                return ok(new Swagger_Library_List());
            }


            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Library> query = Ebean.find(Model_Library.class);
            query.where().eq("deleted", false);


            ExpressionList<Model_Library> list = query.where();
            Junction<Model_Library> disjunction = list.disjunction();

            if (!help.hardware_type_ids.isEmpty()) {
                query.where().in("hardware_types.id", help.hardware_type_ids);
            }

            if (help.project_id != null) {
                Model_Project.find.byId(help.project_id);
                disjunction
                        .conjunction()
                        .eq("project.id", help.project_id)
                        .endJunction();

            }

            if (help.public_library) {
                disjunction
                        .conjunction()
                        .eq("publish_type", ProgramType.PUBLIC.name())
                        .endJunction();
            }

            if (help.pending_library) {
                if (!person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return forbidden();
                disjunction
                        .conjunction()
                        .eq("versions.approval_state", Approval.PENDING.name())
                        .ne("publish_type", ProgramType.DEFAULT_MAIN)
                        .endJunction();
            }

            disjunction.endJunction();

            // Vyvoření odchozího JSON
            Swagger_Library_List result = new Swagger_Library_List(query,page_number, help);

            // Vrácneí objektu
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Library",
            tags = {"Library"},
            notes = "Edit Library name and description",
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_edit(UUID library_id) {
        try {

            // Get and Validate Object
            Swagger_Library_New help = formFromRequestWithValidation(Swagger_Library_New.class);

            // Vyhledání objektu
            Model_Library library = Model_Library.find.byId(library_id);

            // Change values
            library.name = help.name;
            library.description = help.description;

            // Uložení změn
            library.update();

            library.setTags(help.tags);

            // Vrácení objektu
            return ok(library);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag Library",
            tags = {"Library"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(help.object_id);

            library.addTags(help.tags);

            // Vrácení objektu
            return ok(library);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Library",
            tags = {"Library"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Library.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(help.object_id);

            library.removeTags(help.tags);

            // Vrácení objektu
            return ok(library);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Library",
            tags = {"Library"},
            notes = "For remove Library",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_delete(UUID library_id) {
        try {

            // Kontrola objektu
            Model_Library library = Model_Library.find.byId(library_id);

            library.delete();

            // Vrácneí potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// LIBRARY VERSIONS ###################################################################################################

    @ApiOperation(value = "create Library_Version",
            tags = {"Library"},
            notes = "If you want add new code to Library",
            produces = "application/json",
            protocols = "https",
            code = 201
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
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_LibraryVersion.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_version_create(UUID library_id) {
        try {

            // Get and Validate Object
            Swagger_Library_Version_New help = formFromRequestWithValidation(Swagger_Library_Version_New.class);

            // Ověření objektu
            Model_Library library = Model_Library.find.byId(library_id);

            // První nová Verze
            Model_LibraryVersion version = new Model_LibraryVersion();
            version.name             = help.name;
            version.description      = help.description;
            version.library          = library;
            version.publish_type     = ProgramType.PRIVATE;

            version.save();

            Swagger_Library_File_Load library_file_collection = new Swagger_Library_File_Load();
            library_file_collection.files = help.files;

            version.file = Model_Blob.upload(Json.toJson(library_file_collection).toString(), "library.json" , library.get_path());

            version.refresh();
            library.refresh();

            // Vracím vytvořený objekt
            return created(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Library_Version",
            tags = {"Library"},
            notes = "get Version of Library by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_LibraryVersion.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_version_get(UUID version_id) {
        try {

            // Vyhledám Objekt
            Model_LibraryVersion version = Model_LibraryVersion.find.byId(version_id);

            // Vracím Objekt
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Library_Version",
            tags = {"Library"},
            notes = "For update basic (name and description) information in Version of Library. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_LibraryVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result library_version_edit(UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Ověření objektu
            Model_LibraryVersion version = Model_LibraryVersion.find.byId(version_id);

            //Uprava objektu
            version.name = help.name;
            version.description = help.description;

            // Uložení změn
            version.update();

            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Library_Version",
            tags = {"Library"},
            notes = "delete Library by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_version_delete(UUID version_id) {
        try {

            // Ověření objektu
            Model_LibraryVersion version = Model_LibraryVersion.find.byId(version_id);

            // Smažu zástupný objekt
            version.delete();

            // Vracím potvrzení o smazání
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


// PUBLIC LIBRARIES ####################################################################################################

    @ApiOperation(value = "make Library_Version public",
            tags = {"Library"},
            notes = "Make Library public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Bad Request",               response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_version_make_public(UUID version_id) {
        try {

            // Kontrola objektu
            Model_LibraryVersion version = Model_LibraryVersion.find.byId(version_id);

            if (Model_LibraryVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("library.project.participants.person.id", _BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 Libraries. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (version.approval_state != null) {
                return badRequest("You cannot publish same program twice!");
            }

            // Úprava objektu
            version.approval_state = Approval.PENDING;

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Library_Version Response publication",
            tags = {"Admin-Library"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result library_public_response() {
        try {

            // Get and Validate Object
            Swagger_Community_Version_Publish_Response help = formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

            // Kontrola objektu
            Model_LibraryVersion version_old = Model_LibraryVersion.find.byId(help.version_id);

            // Kontrola objektu
            Model_Library library_old = version_old.library;

            // Zkontroluji oprávnění

            if (!library_old.community_publishing_permission()) {
                return forbidden();
            }

            if (help.decision) {

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval.APPROVED;
                version_old.update();


                Model_Library library = Model_Library.find.byId(library_old.id); // TODO + "_public_copy");

                if (library == null) {
                    library = new Model_Library();
                    // library.id = library_old.id + "_public_copy"; TODO
                    library.name = help.program_name;
                    library.description = help.program_description;
                    library.publish_type  = ProgramType.PUBLIC;
                    library.save();
                }


                Model_LibraryVersion version = new Model_LibraryVersion();
                version.name             = help.version_name;
                version.description      = help.version_description;
                version.library          = library;
                version.publish_type     = ProgramType.PUBLIC;
                version.author_id           = version_old.author_id;

                // Zkontroluji oprávnění
                version.save();

                library.refresh();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version_old.file;

                version.file = Model_Blob.upload(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , library.get_path());
                version.update();

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
                                .send(version_old.get_library().get_project().getProduct().customer, "Publishing your Library" );

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
                                .text(Email.bold("Thanks!") + Email.newLine() + _BaseController.person().full_name())
                                .send(version_old.get_library().get_project().getProduct().customer, "Publishing your program" );

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
                            .send(version_old.get_library().get_project().getProduct().customer, "Publishing your program");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

            // Potvrzení
            return  ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

}
