package controllers;

import io.swagger.annotations.*;
import models.project.c_program.actualization.Model_ActualizationProcedure;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Actualization extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// REST - API ----------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get actualization Procedure",
            tags = {"Actualization"},
            notes = "get all versions (content) from independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_ActualizationProcedure.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_Procedure(@ApiParam(required = true) String actualization_procedure_id){
        try {

            // Kontrola objektu
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.find.byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.notFoundObject("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(procedure));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_ActualizationProcedure.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result canceled_procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.find.byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.notFoundObject("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.forbidden_Permission();

            procedure.cancel_procedure();

            return GlobalResult.result_ok(Json.toJson(procedure));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


// Private -------------------------------------------------------------------------------------------------------------


// ---------------------------------------------------------------------------------------------------------------------






}


