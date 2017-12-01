package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedureTask_Filter;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Filter;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Make;
import utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Make_TypeOfBoard;
import utilities.swagger.outboundClass.Filter_List.Swagger_ActualizationProcedureTask_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_ActualizationProcedure_List;
import utilities.swagger.outboundClass.Swagger_ActualizationProcedure_Short_Detail;

import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Actualization extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Actualization.class);

// ACTUALIZATION PROCEDURE #############################################################################################

    @ApiOperation(value = "get ActualizationProcedure",
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
    public Result get_Actualization_Procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.result_notFound("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (!procedure.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(procedure));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ActualizationProcedure by Filter",
            tags = {"Actualization"},
            notes = "get actualization Procedure by query",
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_Procedures_by_filter(int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Filter> form = Form.form(Swagger_ActualizationProcedure_Filter.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedure_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_ActualizationProcedure> query = Ebean.find(Model_ActualizationProcedure.class);
            query.order().desc("date_of_create");

            if (!help.project_ids.isEmpty()) {

                for (String project_id : help.project_ids) {
                    Model_Project project = Model_Project.get_byId(project_id);
                    if (project == null) return GlobalResult.result_notFound("Model_Project project_id not found");
                    if (!project.read_permission()) return GlobalResult.result_forbidden();
                }

                query.where().in("project_id", help.project_ids);

            } else {
                return GlobalResult.result_notFound("Project project_id not included");
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedure_List result = new Swagger_ActualizationProcedure_List(query,page_number);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
            if (!procedure.read_permission()) return GlobalResult.result_forbidden();

            procedure.cancel_procedure();

            return GlobalResult.result_ok(Json.toJson(procedure));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make ActualizationProcedure",
            tags = {"Actualization"},
            notes = "make procedure",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ActualizationProcedure_Make",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Created",              response = Swagger_ActualizationProcedure_Short_Detail.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result make_actualization_procedure() {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Make> form = Form.form(Swagger_ActualizationProcedure_Make.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedure_Make help = form.get();

            // Kontrola Firmware Type
            Enum_Firmware_type firmware_type = Enum_Firmware_type.getFirmwareType(help.firmware_type);
            if (firmware_type == null)  return GlobalResult.result_notFound("firmware_type not found");

            // Kontrola Projektu
            Model_Project project = Model_Project.get_byId(help.project_id);
            if (project == null)  return GlobalResult.result_notFound("firmware_type not found");
            if (!project.update_permission()) return GlobalResult.result_forbidden();

            // Kontrola

            // Only for controling
            if (help.time != null && help.time != 0L) {
                try {
                    Date date_of_planing = new Date(help.time);
                    if (date_of_planing.getTime() < (new Date().getTime() - 5000)) {
                        return GlobalResult.result_badRequest("Invalid Time Format - Past time is not legal");
                    }
                } catch (Exception e) {
                    return GlobalResult.result_badRequest("Invalid Time Format");
                }
            }

            Model_BoardGroup group = Model_BoardGroup.get_byId(help.hardware_group_id);
            if (group == null)  return GlobalResult.result_notFound("Model_BoardGroup group_id recognized");
            if (!group.read_permission()) return GlobalResult.result_forbidden();

            Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
            procedure.type_of_update = Enum_Update_type_of_update.MANUALLY_RELEASE_MANAGER;
            procedure.date_of_create = new Date();
            procedure.project_id = project.id;

            if (help.time != null && help.time != 0L) {
                // Planed
                procedure.date_of_planing = new Date(help.time);
            } else {
                // Immediately
                procedure.date_of_planing = new Date();
            }

            for (Swagger_ActualizationProcedure_Make_TypeOfBoard type_of_boards_settings : help.type_of_boards_settings) {

                Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(type_of_boards_settings.type_of_board_id);
                if (typeOfBoard == null) return GlobalResult.result_notFound("firmware_type not found");

                Model_VersionObject c_program_version = null;

                if (firmware_type == Enum_Firmware_type.FIRMWARE || firmware_type == Enum_Firmware_type.BACKUP) {
                    c_program_version = Model_VersionObject.get_byId(type_of_boards_settings.c_program_version_id);
                    if (c_program_version == null) return GlobalResult.result_notFound("firmware_type not found");
                    if (c_program_version.get_c_program() == null) return GlobalResult.result_notFound("Version is not c Program");
                    if (!c_program_version.get_c_program().read_permission()) return GlobalResult.result_forbidden();
                    if (!c_program_version.get_c_program().get_type_of_board().id.equals(typeOfBoard.id)) GlobalResult.result_badRequest("Invalid type of CProgram for TypeOfBoard");
                }

                Model_BootLoader bootLoader = null;

                if (firmware_type == Enum_Firmware_type.BOOTLOADER) {
                    bootLoader = Model_BootLoader.get_byId(type_of_boards_settings.bootloader_id);
                    if (bootLoader == null) return GlobalResult.result_notFound("firmware_type  found");
                    if (!bootLoader.read_permission()) return GlobalResult.result_forbidden();
                    if (!bootLoader.type_of_board.id.equals(typeOfBoard.id)) GlobalResult.result_badRequest("Invalid type of Bootloader for TypeOfBoard");
                }

                List<Model_Board> boards = Model_Board.find.where().eq("board_groups.id", group.id).eq("type_of_board.id", typeOfBoard.id).select("id").findList();

                for (Model_Board hardware_not_cached : boards) {
                    Model_Board board = Model_Board.get_byId(hardware_not_cached.id);
                    if (board == null) return GlobalResult.result_notFound("hardware_id not found");
                    if (!board.update_permission()) return GlobalResult.result_forbidden();
                    if (!board.project_id().equals(project.id))
                        return GlobalResult.result_notFound("hardware_id is not from same project");

                    Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                    plan.board = board;
                    plan.firmware_type = firmware_type;
                    plan.state = Enum_CProgram_updater_state.not_start_yet;

                    if (firmware_type == Enum_Firmware_type.FIRMWARE || firmware_type == Enum_Firmware_type.BACKUP) {
                        plan.c_program_version_for_update = c_program_version;
                    }

                    if (firmware_type == Enum_Firmware_type.BOOTLOADER) {
                        plan.bootloader = bootLoader;
                    }

                    procedure.updates.add(plan);
                }
            }

            procedure.save();

            return GlobalResult.result_created(Json.toJson(procedure.short_detail()));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }


// C PROGRAM ACTUALIZATION PLAN ########################################################################################

    @ApiOperation(value = "get ActualizationTask",
            tags = {"Actualization"},
            notes = "get Actualization task by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_CProgramUpdatePlan.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_CProgramUpdatePlan(@ApiParam(required = true) String plan_id) {
        try {

            // Kontrola objektu
            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(plan_id);
            if (plan == null) return GlobalResult.result_notFound("Model_CProgramUpdatePlan plan_id not found");

            // Kontrola oprávnění
            if (!plan.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(plan));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ActualizationTask by Filter",
            tags = {"Actualization"},
            notes = "get actualization Tasks by query",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ActualizationProcedureTask_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ActualizationProcedureTask_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_CProgramUpdatePlan_by_filter(int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedureTask_Filter> form = Form.form(Swagger_ActualizationProcedureTask_Filter.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedureTask_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgramUpdatePlan> query = Ebean.find(Model_CProgramUpdatePlan.class);
            query.order().desc("actualization_procedure.date_of_create");

            if (!help.board_ids.isEmpty()) {

                for (String board_id : help.board_ids) {
                    Model_Board board = Model_Board.get_byId(board_id);
                    if (board == null) return GlobalResult.result_notFound("Model_Board board_id not found");
                    if (!board.read_permission()) return GlobalResult.result_forbidden();
                }

                query.where().in("board.id", help.board_ids);
            }

            if (!help.instance_ids.isEmpty()) {

                for (String instance_id : help.instance_ids) {
                    Model_HomerInstance instance = Model_HomerInstance.get_byId(instance_id);
                    if (instance == null) return GlobalResult.result_notFound("Model_HomerInstance board_id not found");
                    if (!instance.read_permission()) return GlobalResult.result_forbidden();
                }

                query.where().in("actualization_procedure.homer_instance_record.main_instance_history.id", help.instance_ids);
            }

            if (!help.actualization_procedure_ids.isEmpty()) {

                for (String procedure_id : help.actualization_procedure_ids) {
                    Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(procedure_id);
                    if (procedure == null) return GlobalResult.result_notFound("Model_ActualizationProcedure procedure_id not found");
                    if (!procedure.read_permission()) return GlobalResult.result_forbidden();
                }

                query.where().in("actualization_procedure.id", help.actualization_procedure_ids);
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedureTask_List result = new Swagger_ActualizationProcedureTask_List(query,page_number);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

}