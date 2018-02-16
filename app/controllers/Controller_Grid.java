package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_M_Program_Interface;
import utilities.swagger.output.Swagger_M_Project_Interface;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.filter_results.Swagger_GridWidget_List;

import java.util.*;

import static play.mvc.Controller.ctx;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Grid extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Grid.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject public Controller_Grid(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create M_Project",
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
    public Result gridProject_create(String project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Project project = Model_Project.getById( project_id );

            Model_GridProject gridProject = new Model_GridProject();
            gridProject.description = help.description;
            gridProject.name = help.name;
            gridProject.project = project;

            gridProject.save();

            return created(gridProject.json());

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
    public Result gridProject_get(String grid_project_id) {
        try {
            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(grid_project_id);

            return ok(gridProject.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit M_Project",
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
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result gridProject_update(String grid_project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(grid_project_id);

            gridProject.name = help.name;
            gridProject.description = help.description;

            gridProject.update();
            
            return ok(gridProject.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag GridProject",
            tags = {"Grid"},
            notes = "",
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
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(help.object_id);

            gridProject.addTags(help.tags);

            // Vrácení objektu
            return ok(gridProject.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag GridProject",
            tags = {"Grid"},
            notes = "",
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
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(help.object_id);

            gridProject.removeTags(help.tags);

            // Vrácení objektu
            return ok(gridProject.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "delete M_Project",
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
    public Result gridProject_delete(String grid_project_id) {
        try {

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(grid_project_id);
            
            gridProject.delete();

            return ok();

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
    public Result gridProject_getInterface(String grid_project_id) {
        try {

            // Kontrola objektu
            Model_GridProject m_project = Model_GridProject.getById(grid_project_id);
            
            Swagger_M_Project_Interface m_project_interface = new Swagger_M_Project_Interface();
            m_project_interface.name = m_project.name;
            m_project_interface.description = m_project.description;
            m_project_interface.id = m_project.id;

            for (Model_GridProgram m_program : m_project.getGridPrograms()) {

                Swagger_M_Program_Interface m_program_interface = new Swagger_M_Program_Interface();
                m_program_interface.description = m_program.description;
                m_program_interface.name        = m_program.name;
                m_program_interface.id          = m_program.id;

                m_program_interface.accessible_versions = m_program.program_versions_interface();
                m_project_interface.accessible_interface.add(m_program_interface);
            }

            return ok(Json.toJson(m_project_interface));

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
    public Result gridProgram_create(String grid_project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProject gridProject = Model_GridProject.getById(grid_project_id);

            Model_GridProgram gridProgram = new Model_GridProgram();
            gridProgram.description         = help.description;
            gridProgram.name                = help.name;
            gridProgram.grid_project = gridProject;
            
            gridProgram.save();

            return created(gridProgram.json());
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
    public Result gridProgram_get( String grid_program_id) {
        try {

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(grid_program_id);

            return ok(gridProgram.json());
            
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
    public Result gridProgram_update(String grid_program_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(grid_program_id);

            if (gridProgram.grid_project == null) return badRequest("You cannot change program on version");

            gridProgram.description = help.description;
            gridProgram.name        = help.name;

            gridProgram.update();

            return ok(gridProgram.json());
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag GridProgram",
            tags = {"Grid"},
            notes = "",
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
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(help.object_id);

            // Add Tags
            gridProgram.addTags(help.tags);

            // Vrácení objektu
            return ok(gridProgram.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag GridProgram",
            tags = {"Grid"},
            notes = "",
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
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(help.object_id);

            // Remove Tags
            gridProgram.removeTags(help.tags);

            // Vrácení objektu
            return ok(gridProgram.json());

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
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result gridProgram_delete(String grid_program_id) {
        try {

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(grid_program_id);

            gridProgram.delete();

            return ok();

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
    public Result gridProgramVersion_create( String grid_program_id) {
        try {

            // Get and Validate Object
            Swagger_M_Program_Version_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_M_Program_Version_New.class);

            // Kontrola objektu
            Model_GridProgram gridProgram = Model_GridProgram.getById(grid_program_id);
            
            Model_GridProgramVersion version       = new Model_GridProgramVersion();
            version.name                = help.name;
            version.description         = help.description;
            version.grid_program        = gridProgram;
            version.public_access       = help.public_access;
            version.m_program_virtual_input_output =  help.virtual_input_output;
          
            version.save();

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);

            Model_Blob.uploadAzure_Version(content.toString(), "grid_program.json" , gridProgram.get_path() ,  version);

            return created(gridProgram.json());

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
    public Result gridProgramVersion_get(String version_id) {
        try {
            // Kontrola objektu
            Model_GridProgramVersion version = Model_GridProgramVersion.getById(version_id);

            // Vrácení objektu
            return ok(version.json());

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
    public Result gridProgramVersion_update( String version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_GridProgramVersion version = Model_GridProgramVersion.getById(version_id);

            // Úprava objektu
            version.description = help.description;
            version.name        = help.name;

            // Update
            version.update();

            return ok(version.json());

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
    public Result gridProgramVersion_delete(String version_id) {
        try {

            // Získání objektu
            Model_GridProgramVersion version  = Model_GridProgramVersion.getById(version_id);
            
            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

//######################################################################################################################

    // Příkazy pro Terminál

    @ApiOperation(value = "getByToken GridProgram",
            tags = {"APP-Api"},
            notes = "get M_Program by token",
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
    public Result gridProgram_getByQRToken(String qr_token) {
        try {


            logger.debug("get_M_Program_byQR_Token_forMobile: Connection token: " + qr_token);

            Model_MProgramInstanceParameter parameter = Model_MProgramInstanceParameter.find.query()
                    .where()
                    .eq("connection_token" , qr_token)
                    .isNotNull("grid_project_program_snapshot.instance_versions.instance_record.actual_running_instance")
                    .findOne();

            if (parameter == null) return notFound("MProgramInstanceParameter by token not found in database");

            return ok(Json.toJson(parameter.get_connection_summary( ctx())));

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
    public Result check_identifiactor(String terminal_id) {
        try {

            // Get and Validate Object
            Swagger_Grid_Terminal_Identf help  = baseFormFactory.formFromRequestWithValidation(Swagger_Grid_Terminal_Identf.class);

            // Kontrola objektu
            Model_GridTerminal terminal = Model_GridTerminal.getById(terminal_id);

            if (terminal == null) {

                terminal = new Model_GridTerminal();
                terminal.device_name = help.device_name;
                terminal.device_type = help.device_type;
                terminal.save();

                return ok(Json.toJson(terminal));

            } else {

                terminal.ws_permission = true;
                terminal.m_program_access = true;
                terminal.update();
                return ok(terminal.json());
            }

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
            Swagger_Grid_Terminal_Identf help  = baseFormFactory.formFromRequestWithValidation(Swagger_Grid_Terminal_Identf.class);

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

            return created(Json.toJson(terminal));

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
            Swagger_NameAndDesc_ProjectIdOptional help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);

            Model_Project project = null;

            if (help.project_id == null) {
                if (Model_Widget.getPublicByName(help.name) != null) {
                    return badRequest("Widget with this name already exists, type a new one.");
                }
            } else {
                project = Model_Project.getById(help.project_id);
            }

            // Vytvoření objektu
            Model_Widget widget = new Model_Widget();
            widget.name = help.name;
            widget.description = help.description;
            widget.author = person();

            if (project != null) {
                widget.project = project;
                widget.publish_type = ProgramType.PRIVATE;
            } else {
                widget.publish_type = ProgramType.PUBLIC;
            }

            // Uložení objektu
            widget.save();

            // Získání šablony
            Model_WidgetVersion scheme = Model_WidgetVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).findOne();

            // Kontrola objektu
            if (scheme == null) return created( Json.toJson(widget) );

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
            return created(widget.json());

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
    public Result widget_get(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {
            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget widget_id not found");

            // Vrácení objektu
            return ok(Json.toJson(gridWidget));

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "get Grid_Widget by Filter",
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
            Swagger_GridWidget_Filter help  = baseFormFactory.formFromRequestWithValidation(Swagger_GridWidget_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Widget> query = Ebean.find(Model_Widget.class);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {

                Model_Project project = Model_Project.getById(help.project_id);
                query.where().eq("type_of_widget.project.id", help.project_id);
            }

            if (help.pending_widget) {
                query.where().eq("versions.approval_state", Approval.PENDING.name()).eq("versions.deleted", false);
            }

            // Vytvoření odchozího JSON
            Swagger_GridWidget_List result = new Swagger_GridWidget_List(query, page_number);

            // Vrácení výsledku
            return ok(Json.toJson(result));

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
    public Result widget_update(@ApiParam(value = "widget_id String path",   required = true)  String grid_widget_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDesc_ProjectIdOptional help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.getById(grid_widget_id);

            // Úprava objektu
            widget.description = help.description;
            widget.name        = help.name;

            // Uložení objektu
            widget.update();

            // Vrácení objektu
            return ok(Json.toJson(widget));

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "tag Widget",
            tags = {"Widget"},
            notes = "",
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
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.getById(help.object_id);

            // Add Tags
            widget.addTags(help.tags);

            // Vrácení objektu
            return ok(widget.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Widget",
            tags = {"Widget"},
            notes = "",
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
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Widget widget = Model_Widget.getById(help.object_id);

            // Remnove Tags
            widget.removeTags(help.tags);

            // Vrácení objektu
            return ok(widget.json());

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
    public Result widget_delete(@ApiParam(value = "widget_id String path",   required = true)  String grid_widget_id) {
        try {

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            // Smazání objektu
            gridWidget.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone Grid_Widget",
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
            Swagger_Grid_Widget_Copy help  = baseFormFactory.formFromRequestWithValidation(Swagger_Grid_Widget_Copy.class);

            // Kontrola objekt
            Model_Widget grid_widget_old = Model_Widget.getById(help.widget_id);

            // Kontrola objekt
            Model_Project project = Model_Project.getById(help.project_id);

            // Zkontroluji oprávnění
            project.check_update_permission();

            Model_Widget grid_widget_new =  new Model_Widget();
            grid_widget_new.name = help.name;
            grid_widget_new.description = help.description;
            grid_widget_new.project = project;
            grid_widget_new.save();

            grid_widget_new.refresh();

            for (Model_WidgetVersion version : grid_widget_old.get_versions()) {

                Model_WidgetVersion copy_object = new Model_WidgetVersion();
                copy_object.name        = version.name;
                copy_object.description = version.description;
                copy_object.author      = version.author;
                copy_object.design_json = version.design_json;
                copy_object.logic_json  = version.logic_json;
                copy_object.widget      = grid_widget_new;

                // Zkontroluji oprávnění
                copy_object.save();
            }

            grid_widget_new.refresh();

            // Vracím Objekt
            return ok(Json.toJson(grid_widget_new));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Grid_Widget",
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
    public Result widget_deactivate(String grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            if (!gridWidget.active) return badRequest("Tariff is already deactivated");
            gridWidget.active = false;

            gridWidget.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Grid_Widget",
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
    public Result widget_activate(String grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            if (gridWidget.active) return badRequest("Tariff is already activated");
            gridWidget.active = true;

            gridWidget.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Grid_Widget Up",
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
    public Result widget_order_up(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            // Kontrola objekt
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            // Shift Order Up
            gridWidget.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Grid_Widget Down",
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
    public Result widget_order_down(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            Model_Widget gridWidget =  Model_Widget.getById(grid_widget_id);

            // Shift Order down
            gridWidget.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// WIDGET VERSION ######################################################################################################    

    @ApiOperation(value = "delete Grid_Widget_Version",
            tags = {"Grid-Widget"},
            notes = "delete GridWidget version",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_delete(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id) {
        try {

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "set_As_Main Grid_Widget_Version",
            tags = {"Admin-Grid-Widget"},
            notes = "",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_set_main(String grid_widget_version_id) {
        try {

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);
            if (version == null) return notFound("GridWidgetVersion grid_widget_version_id not found");

            if (!version.get_grid_widget_id().equals("00000000-0000-0000-0000-000000000001")) {
                return notFound("GridWidgetVersion grid_widget_version_id not from default program");
            }

            Model_WidgetVersion old_version = Model_WidgetVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).select("id").findOne();
            if (old_version != null) {
                old_version = Model_WidgetVersion.getById(old_version.id);
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

    @ApiOperation(value = "make Grid_Widget_Version public",
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
    public Result widgetVersion_version_make_public(String grid_widget_version_id) {
        try {
            
            // Kontrola objektu
            Model_WidgetVersion gridWidgetVersion = Model_WidgetVersion.getById(grid_widget_version_id);
            
            if (Model_WidgetVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("author.id", _BaseController.personId())
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

    @ApiOperation(value = "create Grid_Widget_Version",
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
    public Result widgetVersion_create(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            // Get and Validate Object
            Swagger_GridWidgetVersion_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_GridWidgetVersion_New.class);

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            // Vytvoření objektu
            Model_WidgetVersion version = new Model_WidgetVersion();
            version.name = help.name;
            version.description = help.description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.widget = gridWidget;
            
            // Uložení objektu
            version.save();

            // Vrácení objektu
            return created(Json.toJson(gridWidget));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Grid_Widget_Version",
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
    public Result widgetVersion_get(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id) {
        try {
            
            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);
       
            // Vrácení objektu
            return ok(version.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "edit Grid_Widget_Version",
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
    public Result widgetVersion_edit(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(version_id);

            // Kontrola oprávnění
            version.check_update_permission();
            
            // Úprava objektu
            version.name = help.name;
            version.description = help.description;
            
            // Uložení objektu
            version.update();

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Grid_Widget_Versions",
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
    public Result widgetVersion_getAll(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);

            // Vrácení objektu
            return ok(Json.toJson(gridWidget.versions));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


// GRID ADMIN ##########################################################################################################

    @ApiOperation(value = "edit Grid_Widget_Version Response publication",
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
            Swagger_Community_Version_Publish_Response help  = baseFormFactory.formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_WidgetVersion privateGridWidgetVersion = Model_WidgetVersion.getById(help.version_id);

            // Kontrola nadřazeného objektu
            Model_Widget widget_old = Model_Widget.getById(privateGridWidgetVersion.get_grid_widget_id());

            // Zkontroluji oprávnění
            widget_old.check_community_permission();
           

            if (help.decision) {

                privateGridWidgetVersion.approval_state = Approval.APPROVED;
                privateGridWidgetVersion.update();

                Model_Widget widget = Model_Widget.find.query().where().eq("id",widget_old.id.toString() + "_public_copy").findOne(); // TODO won't work

                if (widget == null) {
                    // Vytvoření objektu
                    widget = new Model_Widget();
                    widget.name = help.program_name;
                    widget.description = help.program_description;
                    widget.author = privateGridWidgetVersion.get_grid_widget().get_author();
                    widget.publish_type = ProgramType.PUBLIC;
                    widget.save();
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
                            .text("Version of Widget " + privateGridWidgetVersion.get_grid_widget().name + ": " + Email.bold(privateGridWidgetVersion.name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(privateGridWidgetVersion.get_grid_widget().get_author().email, "Version of Widget disapproved" );

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
