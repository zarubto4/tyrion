package controllers;

import io.swagger.annotations.*;
import models.Model_ActualizationProcedure;
import models.Model_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Unauthorized;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Actualization extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Actualization.class);

// REST - API ----------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get actualization Procedure",
            tags = {"Actualization"},
            notes = "get Actualization Procedure by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ActualizationProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_Procedure(@ApiParam(required = true) String actualization_procedure_id){
        try {

            // Kontrola objektu
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.result_notFound("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(procedure));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get actualization Procedure by Project",
            tags = {"Actualization"},
            notes = "get actualization Procedure by Project",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ActualizationProcedure.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_Procedures_by_project(@ApiParam(required = true) String project_id){
        try {


            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Model_Project project_id not found");

            if(!project.read_permission())return GlobalResult.result_forbidden();

            // Kontrola objektu
            List<Model_ActualizationProcedure> procedures = Model_ActualizationProcedure.find.where().eq("updates.board.project.id", project_id).order().asc("date_of_create").findList();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(procedures));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "cancel actualization Procedure",
            tags = {"Actualization"},
            notes = "cancel (terminate) procedure",
            produces = "application/json",
            protocols = "https",
            code = 200,
            hidden = false,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = Model_ActualizationProcedure.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Actualization_Procedure_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_ActualizationProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result canceled_procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.result_notFound("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.result_forbidden();

            procedure.cancel_procedure();

            return GlobalResult.result_ok(Json.toJson(procedure));
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}