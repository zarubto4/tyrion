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
import responses.Result_Forbidden;
import responses.Result_InternalServerError;
import responses.Result_NotFound;
import responses.Result_Unauthorized;
import utilities.authentication.Authentication;
import utilities.enums.HardwareUpdateState;
import utilities.enums.FirmwareType;
import utilities.enums.UpdateType;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_ActualizationProcedureTask_List;
import utilities.swagger.output.filter_results.Swagger_ActualizationProcedure_List;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Actualization extends BaseController {
    
    private FormFactory formFactory;
    
    @Inject
    public Controller_Actualization(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Actualization.class);

// ACTUALIZATION PROCEDURE #############################################################################################

    @ApiOperation(value = "get ActualizationProcedure",
            tags = {"Actualization"},
            notes = "get Actualization Procedure by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_Actualization_Procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Model_UpdateProcedure procedure = Model_UpdateProcedure.getById(actualization_procedure_id);
            if (procedure == null) return notFound("ActualizationProcedure not found");

            // Kontrola oprávnění
            if (!procedure.read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(procedure));

        } catch (Exception e) {
            return internalServerError(e);
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
                            dataType = "utilities.swagger.input.Swagger_ActualizationProcedure_Filter",
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
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_Procedures_by_filter(int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Filter> form = formFactory.form(Swagger_ActualizationProcedure_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedure_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_UpdateProcedure> query = Ebean.find(Model_UpdateProcedure.class);
            query.order().desc("created");

            if (!help.project_ids.isEmpty()) {

                for (UUID project_id : help.project_ids) {
                    Model_Project project = Model_Project.getById(project_id);
                    if (project == null) return notFound("Project not found");
                    if (!project.read_permission()) return forbiddenEmpty();
                }

                query.where().in("project_id", help.project_ids);

            } else {
                return notFound("Project project_id not included");
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedure_List result = new Swagger_ActualizationProcedure_List(query,page_number);

            // Vrácení objektu
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result canceled_procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Model_UpdateProcedure procedure = Model_UpdateProcedure.getById(actualization_procedure_id);
            if (procedure == null) return notFound("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (!procedure.read_permission()) return forbiddenEmpty();

            procedure.cancel_procedure();

            return ok(Json.toJson(procedure));
        } catch (Exception e) {
            return internalServerError(e);
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
                            dataType = "utilities.swagger.input.Swagger_ActualizationProcedure_Make",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Created",              response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result make_actualization_procedure() {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedure_Make> form = formFactory.form(Swagger_ActualizationProcedure_Make.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedure_Make help = form.get();

            if (help != null) return created(Json.toJson(help));

            // Kontrola Firmware Type
            FirmwareType firmware_type = FirmwareType.getFirmwareType(help.firmware_type);
            if (firmware_type == null)  return notFound("firmware_type not found");

            // Kontrola Projektu
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null)  return notFound("firmware_type not found");
            if (!project.update_permission()) return forbiddenEmpty();

            // Kontrola

            // Only for controling
            if (help.time != null && help.time != 0L) {
                try {
                    Date date_of_planing = new Date(help.time);
                    if (date_of_planing.getTime() < (new Date().getTime() - 5000)) {
                        return badRequest("Invalid Time Format - Past time is not legal");
                    }
                } catch (Exception e) {
                    return badRequest("Invalid Time Format");
                }
            }

            Model_HardwareGroup group = Model_HardwareGroup.getById(help.hardware_group_id);
            if (group == null)  return notFound("HardwareGroup not found");
            if (!group.read_permission()) return forbiddenEmpty();

            Model_UpdateProcedure procedure = new Model_UpdateProcedure();
            procedure.type_of_update = UpdateType.MANUALLY_RELEASE_MANAGER;
            procedure.project_id = project.id;

            if (help.time != null && help.time != 0L) {
                // Planed
                procedure.date_of_planing = new Date(help.time);
            } else {
                // Immediately
                procedure.date_of_planing = new Date();
            }

            for (Swagger_ActualizationProcedure_Make_HardwareType hardware_type_settings : help.hardware_type_settings) {

                Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_settings.hardware_type_id);
                if (hardwareType == null) return notFound("firmware_type not found");

                Model_Version c_program_version = null;

                if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
                    c_program_version = Model_Version.getById(hardware_type_settings.c_program_version_id);
                    if (c_program_version == null) return notFound("firmware_type not found");
                    if (c_program_version.get_c_program() == null) return notFound("Version is not c Program");
                    if (!c_program_version.get_c_program().read_permission()) return forbiddenEmpty();
                    if (!c_program_version.get_c_program().getHardwareType().id.equals(hardwareType.id)) badRequest("Invalid type of CProgram for HardwareType");
                }

                Model_BootLoader bootLoader = null;

                if (firmware_type == FirmwareType.BOOTLOADER) {
                    bootLoader = Model_BootLoader.getById(hardware_type_settings.bootloader_id);
                    if (bootLoader == null) return notFound("firmware_type  found");
                    if (!bootLoader.read_permission()) return forbiddenEmpty();
                    if (!bootLoader.hardware_type.id.equals(hardwareType.id)) badRequest("Invalid type of Bootloader for HardwareType");
                }

                List<Model_HardwareRegistration> hw = Model_HardwareRegistration.find.query().where().eq("group.id", group.id).eq("hardware.hardware_type.id", hardwareType.id).select("id").findList();

                for (Model_HardwareRegistration hardware_not_cached : hw) {
                    Model_HardwareRegistration hardware = Model_HardwareRegistration.getById(hardware_not_cached.id);
                    if (hardware == null) return notFound("hardware_id not found");
                    if (!hardware.update_permission()) return forbiddenEmpty();
                    if (!hardware.getProject().id.equals(project.id))
                        return notFound("hardware_id is not from same project");

                    Model_HardwareUpdate plan = new Model_HardwareUpdate();
                    plan.hardware = hardware;
                    plan.firmware_type = firmware_type;
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;

                    if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
                        plan.c_program_version_for_update = c_program_version;
                    }

                    if (firmware_type == FirmwareType.BOOTLOADER) {
                        plan.bootloader = bootLoader;
                    }

                    procedure.updates.add(plan);
                }
            }

            procedure.save();

            return created(procedure.json());
        } catch (Exception e) {
            return internalServerError(e);
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareUpdate.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_Actualization_CProgramUpdatePlan(@ApiParam(required = true) String plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareUpdate plan = Model_HardwareUpdate.getById(plan_id);
            if (plan == null) return notFound("CProgramUpdatePlan not found");

            // Kontrola oprávnění
            if (!plan.read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(plan));

        } catch (Exception e) {
            return internalServerError(e);
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
                            dataType = "utilities.swagger.input.Swagger_ActualizationProcedureTask_Filter",
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
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_CProgramUpdatePlan_by_filter(int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_ActualizationProcedureTask_Filter> form = formFactory.form(Swagger_ActualizationProcedureTask_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_ActualizationProcedureTask_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_HardwareUpdate> query = Ebean.find(Model_HardwareUpdate.class);
            query.order().desc("actualization_procedure.created");


            if (help.update_states != null && !help.update_states.isEmpty()) {
                query.where().in("state", help.update_states);
            }

            if (help.type_of_updates != null && !help.type_of_updates.isEmpty()) {
                query.where().in("actualization_procedure.type_of_update", help.type_of_updates);
            }


            if (!help.hardware_ids.isEmpty()) {

                for (UUID hardware_id : help.hardware_ids) {
                    Model_Hardware hardware = Model_Hardware.getById(hardware_id);
                    if (hardware == null) return notFound("Hardware not found");
                    if (!hardware.read_permission()) return forbiddenEmpty();
                }

                query.where().in("hardware.id", help.hardware_ids);
            }

            if (!help.instance_ids.isEmpty()) {

                for (UUID instance_id : help.instance_ids) {
                    Model_Instance instance = Model_Instance.getById(instance_id);
                    if (instance == null) return notFound("Instance not found");
                    if (!instance.read_permission()) return forbiddenEmpty();
                }

                query.where().in("actualization_procedure.homer_instance_record.main_instance_history.id", help.instance_ids); // TODO
            }

            if (!help.actualization_procedure_ids.isEmpty()) {

                for (UUID procedure_id : help.actualization_procedure_ids) {
                    Model_UpdateProcedure procedure = Model_UpdateProcedure.getById(procedure_id);
                    if (procedure == null) return notFound("ActualizationProcedure not found");
                    if (!procedure.read_permission()) return forbiddenEmpty();
                }

                query.where().in("actualization_procedure.id", help.actualization_procedure_ids);
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedureTask_List result = new Swagger_ActualizationProcedureTask_List(query, page_number);

            // Vrácení objektu
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}