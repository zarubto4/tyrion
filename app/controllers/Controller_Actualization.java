package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Filter;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Make;
import utilities.swagger.documentationClass.Swagger_C_Program_Filter;
import utilities.swagger.outboundClass.Filter_List.Swagger_ActualizationProcedure_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_C_Program_List;
import utilities.swagger.outboundClass.Swagger_ActualizationProcedure_Short_Detail;

import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Actualization extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Actualization.class);

// REST - API ----------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get actualizationProcedure",
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

    @ApiOperation(value = "get ActualizationProcedure by Filter",
            tags = {"Actualization"},
            notes = "get actualization Procedure by Project",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ActualizationProcedure_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_Procedures_by_filter(int page_number){
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Filter> form = Form.form(Swagger_ActualizationProcedure_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_ActualizationProcedure_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_ActualizationProcedure> query = Ebean.find(Model_ActualizationProcedure.class);
            query.order().asc("date_of_create");



            if(help.project_ids.isEmpty()) {

                for(String project_id : help.project_ids) {
                    Model_Project project = Model_Project.find.byId(project_id);
                    if (project == null) return GlobalResult.result_notFound("Model_Project project_id not found");
                    if (!project.read_permission()) return GlobalResult.result_forbidden("Model_Project project_id not found");
                }

                query.where().in("updates.board.project.id", help.project_ids);
            }else {
                return GlobalResult.result_notFound("Project project_id not included");
            }


            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedure_List result = new Swagger_ActualizationProcedure_List(query,page_number);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "cancel ActualizationProcedure",
            tags = {"Actualization"},
            notes = "cancel (terminate) procedure",
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


    @ApiOperation(value = "make ActualizationProcedure",
            tags = {"Actualization"},
            notes = "make procedure",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.ActualizationProcedure_Make",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ActualizationProcedure_Short_Detail.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result make_actualization_procedure() {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Make> form = Form.form(Swagger_ActualizationProcedure_Make.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_ActualizationProcedure_Make help = form.get();


            // Kontrola Firmware Type
            Enum_Firmware_type firmware_type = Enum_Firmware_type.getFirmwareType(help.firmware_type);
            if(firmware_type == null)  return GlobalResult.result_notFound("firmware_type not found");

            // Kontrola Projektu
            Model_Project project = Model_Project.get_byId(help.project_id);
            if(project == null)  return GlobalResult.result_notFound("firmware_type not found");
            if(!project.update_permission()) return GlobalResult.result_forbidden();

            // Kontrola

            // Only for controling
            if(help.time != null) {
                try {
                    Date date_of_planing = new Date(help.time);
                    if (date_of_planing.getTime() < (new Date().getTime() - 5000)) {
                        return GlobalResult.result_badRequest("Invalid Time Format - Past time is not legal");
                    }
                } catch (Exception e) {
                    return GlobalResult.result_badRequest("Invalid Time Format");
                }
            }

            Model_VersionObject c_program_version = Model_VersionObject.get_byId(help.c_program_version_id);
            if(c_program_version == null)  return GlobalResult.result_notFound("firmware_type not found");
            if(c_program_version.c_program == null) return GlobalResult.result_notFound("Version is not c Program");
            if(!c_program_version.c_program.read_permission()) return GlobalResult.result_forbidden();


            Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
            procedure.type_of_update = Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL;
            procedure.date_of_create = new Date();
            procedure.project_id = project.id;

            if(help.time != null) {
                // Planed
                procedure.date_of_planing = new Date(help.time);
            }else {
                // Immediately
                procedure.date_of_planing = new Date();
            }


            for(String group_id : help.hardware_group_ids ) {

                Model_BoardGroup group = Model_BoardGroup.get_byId(group_id);
                if(group == null)  return GlobalResult.result_notFound("Model_BoardGroup group_id recognized");
                if(!group.read_permission()) return GlobalResult.result_forbidden();

                for(String hardware_id : group.get_hardware_id_list()) {

                    Model_Board board = Model_Board.get_byId(hardware_id);
                    if(board == null) return GlobalResult.result_notFound("hardware_id not found");
                    if(!board.update_permission()) return GlobalResult.result_forbidden();
                    if(!board.project_id().equals( project.id)) return GlobalResult.result_notFound("hardware_id is not from same project");

                    Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                    plan.board = board;
                    plan.firmware_type = firmware_type;
                    plan.c_program_version_for_update = c_program_version;
                    procedure.updates.add(plan);
                }
            }

            procedure.save();

            return GlobalResult.result_ok(Json.toJson(procedure.short_detail()));
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }


}