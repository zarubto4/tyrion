package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Junction;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.permission.PermissionService;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_M_Program_Interface;
import utilities.swagger.output.Swagger_M_Project_Interface;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.filter_results.Swagger_GridProjectList;
import utilities.swagger.output.filter_results.Swagger_GridWidget_List;

import java.util.*;

import static play.mvc.Controller.ctx;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Grid extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Grid.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_Grid(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService) {
        super(ws, formFactory, config, permissionService);
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create GridProject",
            tags = {"Grid"},
            notes = "GridProject is package for GridPrograms -> presupposition is that you need more control terminal for your IoT project. " +
                    "Different screens for family members, for employee etc.. But of course - you can used that for only one GridProgram",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_GridProject.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProject_create(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId( project_id );

            Model_GridProject gridProject = new Model_GridProject();
            gridProject.description = help.description;
            gridProject.name = help.name;
            gridProject.project = project;

            this.checkCreatePermission(gridProject);

            gridProject.save();

            gridProject.setTags(help.tags);

            return created(gridProject);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GridProject",
            tags = {"Grid"},
            notes = "get GridProject by query = grid_project_id",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProject.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProject_get(UUID grid_project_id) {
        try {
            return read(Model_GridProject.find.byId(grid_project_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GridProject by Filter",
            tags = {"Grid"},
            notes = "get GridProject by filter parameters",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridProject_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_GridProjectList.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProject_get_filterByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number) {
        try {

            // Get and Validate Object
            Swagger_GridProject_Filter help = formFromRequestWithValidation(Swagger_GridProject_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_GridProject> query = Ebean.find(Model_GridProject.class);

            query.orderBy("UPPER(name) ASC");
            query.orderBy("project.id");
            query.where().ne("deleted", true);


            if (help.project_id != null) {
                query.where().eq("project.id", help.project_id);
            }

            if (help.project_id == null) {
                query.where().isNull("project.id");
            }

            // TODO permissions

            Swagger_GridProjectList result = new Swagger_GridProjectList(query, page_number, help);

            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit GridProject",
            tags = {"Grid"},
            notes = "edit basic information in M_Project by query = grid_project_id",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridProject.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProject_update(UUID grid_project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.find.byId(grid_project_id);

            gridProject.name = help.name;
            gridProject.description = help.description;
            gridProject.setTags(help.tags);
            
            return update(gridProject);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag GridProject",
            tags = {"Grid"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProject.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result gridProject_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.find.byId(help.object_id);

            this.checkUpdatePermission(gridProject);

            gridProject.addTags(help.tags);

            // Vrácení objektu
            return ok(gridProject);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag GridProject",
            tags = {"Grid"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProject.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result gridProject_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.find.byId(help.object_id);

            this.checkUpdatePermission(gridProject);

            gridProject.removeTags(help.tags);

            // Vrácení objektu
            return ok(gridProject);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "delete GridProject",
            tags = {"Grid"},
            notes = "remove M_Project by query = grid_project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.delete_permission", value = "true")
                    })
            }
    )
     @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProject_delete(UUID grid_project_id) {
        try {
            return delete(Model_GridProject.find.byId(grid_project_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "getInterface GridProject",
            tags = {"Grid"},
            notes = "get accessible interface from M_Project",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_M_Project_Interface.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProject_getInterface(UUID grid_project_id) {
        try {

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.find.byId(grid_project_id);

            this.checkReadPermission(gridProject);
            
            Swagger_M_Project_Interface m_project_interface = new Swagger_M_Project_Interface();
            m_project_interface.name = gridProject.name;
            m_project_interface.description = gridProject.description;
            m_project_interface.id = gridProject.id;

            for (Model_GridProgram m_program : gridProject.getGridPrograms()) {

                Swagger_M_Program_Interface m_program_interface = new Swagger_M_Program_Interface();
                m_program_interface.description = m_program.description;
                m_program_interface.name        = m_program.name;
                m_program_interface.id          = m_program.id;

                m_program_interface.accessible_versions = m_program.program_versions_interface();
                m_project_interface.accessible_interface.add(m_program_interface);
            }

            return ok(m_project_interface);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


// GRID PROGRAM ########################################################################################################

    @ApiOperation(value = "create GridProgram",
            tags = {"Grid"},
            notes = "creating new GridProgram",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_GridProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProgram_create(UUID grid_project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.find.byId(grid_project_id);

            Model_GridProgram gridProgram = new Model_GridProgram();
            gridProgram.description         = help.description;
            gridProgram.name                = help.name;
            gridProgram.grid_project        = gridProject;
            gridProgram.setTags(help.tags);

            return create(gridProgram);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GridProgram",
            tags = {"Grid"},
            notes = "get GridProgram by query grid_program_id",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgram.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProgram_get(UUID grid_program_id) {
        try {
            return read(Model_GridProgram.find.byId(grid_program_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update GridProgram",
            tags = {"Grid"},
            notes = "update GridProgram - in this case we are not support versions of grid_project",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProgram_update(UUID grid_program_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.find.byId(grid_program_id);

            if (gridProgram.get_grid_project() == null) return badRequest("You cannot change program on version");

            gridProgram.description = help.description;
            gridProgram.name        = help.name;
            gridProgram.setTags(help.tags);

            return update(gridProgram);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag GridProgram",
            tags = {"Grid"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result gridProgram_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help  = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.find.byId(help.object_id);

            this.checkUpdatePermission(gridProgram);

            // Add Tags
            gridProgram.addTags(help.tags);

            // Vrácení objektu
            return ok(gridProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag GridProgram",
            tags = {"Grid"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result gridProgram_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help  = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.find.byId(help.object_id);

            this.checkUpdatePermission(gridProgram);

            // Remove Tags
            gridProgram.removeTags(help.tags);

            // Vrácení objektu
            return ok(gridProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete GridProgram",
            tags = {"Grid"},
            notes = "remove GridProgram by query = grid_program_id",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProgram_delete(UUID grid_program_id) {
        try {
            return delete(Model_GridProgram.find.byId(grid_program_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// GRID PROGRAM VERSION ################################################################################################

    @ApiOperation(value = "Create GridProgramVersion",
            tags = {"Grid"},
            notes = "creating new GridProgramVersion",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_M_Program_Version_New",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_GridProgramVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProgramVersion_create( UUID grid_program_id) {
        try {

            // Get and Validate Object
            Swagger_M_Program_Version_New help  = formFromRequestWithValidation(Swagger_M_Program_Version_New.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.find.byId(grid_program_id);
            
            Model_GridProgramVersion version       = new Model_GridProgramVersion();
            version.name                = help.name;
            version.description         = help.description;
            version.grid_program        = gridProgram;
            version.public_access       = help.public_access;
            version.m_program_virtual_input_output =  help.virtual_input_output;

            this.checkCreatePermission(version);

            version.save();

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);

            version.file = Model_Blob.upload(content.toString(), "grid_program.json" , gridProgram.get_path());
            version.update();

            return created(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GridProgramVersion",
            tags = {"Grid"},
            notes = "get GridProgramVersion by query version_id",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgramVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProgramVersion_get(UUID version_id) {
        try {
            return read(Model_GridProgramVersion.find.byId(version_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit GridProgramVersion",
            tags = {"Grid"},
            notes = "edit GridProgramVersion by query = version_id",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridProgramVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProgramVersion_update( UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProgramVersion version = Model_GridProgramVersion.find.byId(version_id);

            // Úprava objektu
            version.description = help.description;
            version.name        = help.name;

            return update(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete GridProgramVersion",
            tags = {"Grid"},
            notes = "remove GridProgramVersion",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result gridProgramVersion_delete(UUID version_id) {
        try {
            return delete(Model_GridProgramVersion.find.byId(version_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// FOR GRID APP ########################################################################################################

    @ApiOperation(value = "getByToken GridProgram",
            tags = {"APP-Api"},
            notes = "get Grid_Program by token",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",  response = Swagger_Mobile_Connection_Summary.class),
            @ApiResponse(code = 400, message = "Bad Request - Probably token is not valid anymore", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 477, message = "External Server is offline", response = Result_ServerOffline.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result gridProgram_getByQRToken(UUID instance_id, UUID grid_program_id) { // ins = instance_id && prg = program_version_id
        try {


            logger.trace("get_grid_byQR_Token_forMobile: Instance ID::  {}" , instance_id);
            logger.trace("get_grid_byQR_Token_forMobile: Grid Program ID:: {}" , grid_program_id);

            Model_Instance instance = Model_Instance.find.byId(instance_id);

            if(instance.current_snapshot() == null){
                logger.debug("get_grid_byQR_Token_forMobile: Instance ID:: {} not running", instance_id);
                return badRequest("Instance not running");
            }

            Swagger_Mobile_Connection_Summary result = instance.current_snapshot().get_connection_summary(grid_program_id , ctx());

            System.out.println("Co Vracím?? \n");
            System.out.println(Json.toJson(result));
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "check Terminal",
            tags = {"APP-Api"},
            notes = "For every app (terminal) opening you have to valid your terminal_id.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Terminal_Identf",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Valid Identification",      response = Model_GridTerminal.class),
            @ApiResponse(code = 400, message = "Invalid Identification",    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result check_identifiactor(UUID terminal_id) {
        try {

            // Get and Validate Object
            Swagger_Grid_Terminal_Identf help  = formFromRequestWithValidation(Swagger_Grid_Terminal_Identf.class);

            Model_GridTerminal terminal;

            try {
                terminal = Model_GridTerminal.find.byId(terminal_id);
                terminal.ws_permission = true;
                terminal.m_program_access = true;
                terminal.update();

            } catch (NotFoundException e) {
                terminal = new Model_GridTerminal();
                terminal.device_name = help.device_name;
                terminal.device_type = help.device_type;
                terminal.save();
            }

            return ok(terminal);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Terminal",
            tags = {"APP-Api"},
            notes = "Only for Grid Terminals! Before when you want connect terminal (grid) application with Tyrion throw WebSocker. " +
                    "You need unique identification key. If Person loggs to you application Tyrion connects this device with Person. Try to " +
                    "save this key to cookies or on mobile device, or you have to ask every time again",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Terminal_Identf",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_GridTerminal.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    //@Security.Authenticated(Authentication.class) - Není záměrně!!!! - Ověřuje se v read_permision program může být public!
    public Result get_identificator() {
        try {


            // Get and Validate Object
            Swagger_Grid_Terminal_Identf help  = formFromRequestWithValidation(Swagger_Grid_Terminal_Identf.class);

            Model_GridTerminal terminal = new Model_GridTerminal();
            terminal.device_name = help.device_name;
            terminal.device_type = help.device_type;

            if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  terminal.user_agent = "Unknown browser";


            // Tato část je určená pro nalezení tokenu a přihlášení uživatele - bylo totiž nutné zpřístupnit tuto metodu i nepřihlášeným (bez loginu). Kvuli tomu že by to přes  @Security.Authenticated(Authentication.class)  neprošlo
            String[] token_values =  Http.Context.current().request().headers().get("X-AUTH-TOKEN");


            if ((token_values != null) && (token_values.length == 1) && (token_values[0] != null)) {
                logger.debug("get_identificator :: HTTP request containts X-AUTH-TOKEN");
                Model_Person person = Model_Person.getByAuthToken(UUID.fromString(token_values[0]));
                if (person != null) {
                    logger.debug("get_identificator :: Person with X-AUTH-TOKEN found");
                  terminal.person = person;

                } else {
                    logger.warn("get_identificator :: Person with X-AUTH-TOKEN not found!");
                }
            }

            terminal.save();

            return created(terminal);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// WIDGET ##############################################################################################################

    @ApiOperation(value = "create Widget",
            tags = {"Widget"},
            notes = "creating new independent Widget object for Grid tools",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdOptional",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widget_create() {
        try {

            // Get and Validate Object
            Swagger_NameAndDesc_ProjectIdOptional help  = formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);

            Model_Project project = null;

            if (help.project_id == null) {
                if (Model_Widget.getPublicByName(help.name) != null) {
                    return badRequest("Widget with this name already exists, type a new one.");
                }
            } else {
                project = Model_Project.find.byId(help.project_id);
            }

            // Vytvoření objektu
            Model_Widget widget = new Model_Widget();
            widget.name = help.name;
            widget.description = help.description;
            widget.author_id = person().id;

            if (project != null) {
                widget.project = project;
                widget.publish_type = ProgramType.PRIVATE;
            } else {
                widget.publish_type = ProgramType.PUBLIC;
            }

            this.checkCreatePermission(widget);

            // Uložení objektu
            widget.save();

            widget.setTags(help.tags);

            // Získání šablony
            Model_WidgetVersion scheme = Model_WidgetVersion.find.query().nullable().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).findOne();

            // Kontrola objektu
            if (scheme == null) return created(widget);

            // Vytvoření objektu první verze
            Model_WidgetVersion gridWidgetVersion = new Model_WidgetVersion();
            gridWidgetVersion.name = "0.0.1";
            gridWidgetVersion.description = "This is a first version of widget.";
            gridWidgetVersion.approval_state = Approval.APPROVED;
            gridWidgetVersion.design_json = scheme.design_json;
            gridWidgetVersion.logic_json = scheme.logic_json;
            gridWidgetVersion.widget = widget;
            gridWidgetVersion.save();

            // Vrácení objektu
            return created(widget);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Widget",
            tags = {"Widget"},
            notes = "get independent Widget object",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_get(@ApiParam(value = "widget_id String path",   required = true) UUID grid_widget_id) {
        try {
            return read(Model_Widget.find.byId(grid_widget_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Widget List by Filter",
            tags = {"Grid-Widget"},
            notes = "get GridWidget List",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidget_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_GridWidget_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Get and Validate Object
            Swagger_GridWidget_Filter help  = formFromRequestWithValidation(Swagger_GridWidget_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(help.project_id != null || help.public_programs || help.pending_widgets)) {
                return ok(new Swagger_GridWidget_List());
            }

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Widget> query = Ebean.find(Model_Widget.class);

            // query.orderBy("UPPER(name) ASC");
            query.where().eq("deleted", false);


            ExpressionList<Model_Widget> list = query.where();
            Junction<Model_Widget> disjunction = list.disjunction();

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {
                Model_Project.find.byId(help.project_id);
                disjunction
                        .conjunction()
                            .eq("project.id", help.project_id)
                        .endJunction();
            }

            if (help.public_programs) {
                disjunction
                        .conjunction()
                            .eq("publish_type", ProgramType.PUBLIC)
                        .endJunction();
            }

            if (help.pending_widgets) {
                disjunction
                        .conjunction()
                            .eq("versions.approval_state", Approval.PENDING.name())
                            .ne("publish_type", ProgramType.DEFAULT_MAIN)
                        .endJunction();
            }

            disjunction.endJunction();

            // Vytvoření odchozího JSON
            Swagger_GridWidget_List result = new Swagger_GridWidget_List(query, page_number, help);

            // TODO permissions

            // Vrácení výsledku
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Widget",
            tags = {"Widget"},
            notes = "update basic information (name, and description) of the independent GridWidget",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdOptional",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widget_update(@ApiParam(value = "widget_id String path",   required = true)  UUID grid_widget_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDesc_ProjectIdOptional help  = formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.find.byId(grid_widget_id);

            // Úprava objektu
            widget.description = help.description;
            widget.name        = help.name;
            widget.setTags(help.tags);

            // Vrácení objektu
            return update(widget);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "tag Widget",
            tags = {"Widget"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result widget_addTags() {
        try {


            // Get and Validate Object
            Swagger_Tags help  = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.find.byId(help.object_id);

            this.checkUpdatePermission(widget);

            // Add Tags
            widget.addTags(help.tags);

            // Vrácení objektu
            return ok(widget);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Widget",
            tags = {"Widget"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result widget_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help  = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.find.byId(help.object_id);

            this.checkUpdatePermission(widget);

            // Remnove Tags
            widget.removeTags(help.tags);

            // Vrácení objektu
            return ok(widget);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Widget",
            tags = {"Widget"},
            notes = "delete Widget",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_delete(@ApiParam(value = "widget_id String path",   required = true)  UUID grid_widget_id) {
        try {
            return delete(Model_Widget.find.byId(grid_widget_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone Widget",
            tags = {"Grid_Widget"},
            notes = "clone Grid_Widget for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Widget_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_clone() {
        try {

            // Get and Validate Object
            Swagger_Grid_Widget_Copy help = formFromRequestWithValidation(Swagger_Grid_Widget_Copy.class);

            // Kontrola objekt
            Model_Widget grid_widget_old = Model_Widget.find.byId(help.widget_id);

            // Kontrola objekt
            Model_Project project = Model_Project.find.byId(help.project_id);

            Model_Widget grid_widget_new =  new Model_Widget();
            grid_widget_new.name = help.name;
            grid_widget_new.description = help.description;
            grid_widget_new.project = project;

            this.checkCreatePermission(grid_widget_new);

            grid_widget_new.save();

            grid_widget_new.refresh();

            for (Model_WidgetVersion version : grid_widget_old.get_versions()) {

                Model_WidgetVersion copy_object = new Model_WidgetVersion();
                copy_object.name        = version.name;
                copy_object.description = version.description;
                copy_object.author_id      = version.author_id;
                copy_object.design_json = version.design_json;
                copy_object.logic_json  = version.logic_json;
                copy_object.widget      = grid_widget_new;

                // Zkontroluji oprávnění
                copy_object.save();
            }

            grid_widget_new.refresh();

            // Vracím Objekt
            return ok(grid_widget_new);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Widget",
            tags = {"Admin-Grid-Widget"},
            notes = "deactivate Widget",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_deactivate(UUID grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget widget = Model_Widget.find.byId(grid_widget_id);

            this.checkActivatePermission(widget);

            if (!widget.active) return badRequest("Model_Widget is already deactivated");
            widget.active = false;

            widget.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Widget",
            tags = {"Admin-Grid-Widget"},
            notes = "activate Widget",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_activate(UUID grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget widget = Model_Widget.find.byId(grid_widget_id);

            this.checkActivatePermission(widget);

            if (widget.active) return badRequest("Model_Widget is already activate");
            widget.active = true;

            widget.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Widget Up",
            tags = {"Grid-Widget"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_order_up(@ApiParam(value = "widget_id String path",   required = true) UUID grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget gridWidget = Model_Widget.find.byId(grid_widget_id);

            // Shift Order Up
            // gridWidget.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Widget Down",
            tags = {"Grid-Widget"},
            notes = "set down order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_order_down(@ApiParam(value = "widget_id String path",   required = true) UUID grid_widget_id) {
        try {

            Model_Widget gridWidget =  Model_Widget.find.byId(grid_widget_id);

            // Shift Order down
            //gridWidget.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// WIDGET VERSION ######################################################################################################    

    @ApiOperation(value = "delete Widget_Version",
            tags = {"Grid-Widget"},
            notes = "delete GridWidget version",
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
    public Result widgetVersion_delete(@ApiParam(value = "version_id String path",   required = true) UUID version_id) {
        try {
            return delete(Model_WidgetVersion.find.byId(version_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "set_As_Main Widget_Version",
            tags = {"Admin-Grid-Widget"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_delete_permission")
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_set_main(UUID version_id) {
        try {

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.find.byId(version_id);
            if (version == null) return notFound("GridWidgetVersion version_id not found");

            if (!version.get_grid_widget_id().equals("00000000-0000-0000-0000-000000000001")) {
                return notFound("GridWidgetVersion version_id not from default program");
            }

            Model_WidgetVersion old_version = Model_WidgetVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).select("id").findOne();
            if (old_version != null) {
                old_version = Model_WidgetVersion.find.byId(old_version.id);
                old_version.publish_type = null;
                old_version.update();
            }

            version.publish_type = ProgramType.DEFAULT_VERSION;
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "make Widget_Version public",
            tags = {"C_Program"},
            notes = "Make C_Program public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
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
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_version_make_public(UUID grid_widget_version_id) {
        try {
            
            // Kontrola objektu
            Model_WidgetVersion gridWidgetVersion = Model_WidgetVersion.find.byId(grid_widget_version_id);
            
            if (Model_WidgetVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("author_id", _BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (gridWidgetVersion.approval_state != null)  return badRequest("You cannot publish same program twice!");

            // Úprava objektu
            gridWidgetVersion.approval_state = Approval.PENDING;

            // Uložení změn
            gridWidgetVersion.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Widget_Version",
            tags = {"Grid-Widget"},
            notes = "new GridWidget version",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_WidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_create(@ApiParam(value = "widget_id String path",   required = true) UUID grid_widget_id) {
        try {

            // Get and Validate Object
            Swagger_GridWidgetVersion_New help  = formFromRequestWithValidation(Swagger_GridWidgetVersion_New.class);

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.find.byId(grid_widget_id);

            // Vytvoření objektu
            Model_WidgetVersion version = new Model_WidgetVersion();
            version.name = help.name;
            version.description = help.description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.widget = gridWidget;

            return create(gridWidget);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Widget_Version",
            tags = {"Grid-Widget"},
            notes = "get version (content) from independent GridWidget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_get(@ApiParam(value = "version_id String path",   required = true) UUID version_id) {
        try {
            return read(Model_WidgetVersion.find.byId(version_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "edit Widget_Version",
            tags = {"Grid-Widget"},
            notes = "You can edit only basic information of the version. If you want to update the code, " +
                    "you have to create a new version!",
            produces = "application/json",
            protocols = "https",
            code = 200
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_edit(@ApiParam(value = "version_id String path",   required = true) UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.find.byId(version_id);
            
            // Úprava objektu
            version.name = help.name;
            version.description = help.description;

            return update(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Widget_Versions",
            tags = {"Grid-Widget"},
            notes = "get all versions (content) from independent GridWidget",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_getAll(@ApiParam(value = "widget_id String path",   required = true) UUID widget_id) {
        try {

            // Kontrola objektu
            Model_Widget widget = Model_Widget.find.byId(widget_id);

            this.checkReadPermission(widget);

            // Vrácení objektu
            return ok(widget.versions);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


// GRID ADMIN ##########################################################################################################

    @ApiOperation(value = "edit Widget_Version Response publication",
            tags = {"Admin-Grid-Widget"},
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
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widget_public_response() {
        try {

            // Get and Validate Object
            Swagger_Community_Version_Publish_Response help  = formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_WidgetVersion privateGridWidgetVersion = Model_WidgetVersion.find.byId(help.version_id);

            // Kontrola nadřazeného objektu
            Model_Widget widget_old = Model_Widget.find.byId(privateGridWidgetVersion.get_grid_widget_id());

            this.checkPublishPermission(widget_old);

            if (help.decision) {

                privateGridWidgetVersion.approval_state = Approval.APPROVED;
                privateGridWidgetVersion.update();

                UUID widget_previous_id = Model_Widget.find.query().where().eq("original_id", widget_old.id).select("id").findSingleAttribute();

                Model_Widget widget = null;

                if (widget_previous_id == null) {
                    // Vytvoření objektu
                    widget = new Model_Widget();
                    widget.original_id = widget_old.id;
                    widget.name = help.program_name;
                    widget.description = help.program_description;
                    widget.author_id = privateGridWidgetVersion.getWidget().get_author().id;
                    widget.publish_type = ProgramType.PUBLIC;
                    widget.active = true;
                    widget.save();
                } else {
                    widget = Model_Widget.find.byId(widget_previous_id);
                }

                // Vytvoření objektu
                Model_WidgetVersion version = new Model_WidgetVersion();
                version.name = help.version_name;
                version.description = help.version_description;
                version.design_json = privateGridWidgetVersion.design_json;
                version.logic_json = privateGridWidgetVersion.logic_json;
                version.approval_state = Approval.APPROVED;
                version.widget = widget;
                version.save();

                widget.refresh();

                // TODO notifikace a emaily

                return ok();

            } else {
                // Změna stavu schválení
                privateGridWidgetVersion.approval_state = Approval.DISAPPROVED;

                // Odeslání emailu s důvodem
                try {

                    new Email()
                            .text("Version of Widget " + privateGridWidgetVersion.getWidget().name + ": " + Email.bold(privateGridWidgetVersion.name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(privateGridWidgetVersion.getWidget().get_author().email, "Version of Widget disapproved" );

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

                // Uložení změn
                privateGridWidgetVersion.update();

                // Vrácení výsledku
                return ok();
            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
