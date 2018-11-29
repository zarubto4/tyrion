package controllers;

import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.CompilationStatus;
import utilities.enums.HardwareUpdateState;
import utilities.enums.FirmwareType;
import utilities.enums.UpdateType;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_ActualizationProcedureTask_List;
import utilities.swagger.output.filter_results.Swagger_ActualizationProcedure_List;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Update extends _BaseController {


// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Update.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Update(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

// ACTUALIZATION PROCEDURE #############################################################################################

    @ApiOperation(value = "get ActualizationProcedure",
            tags = {"Actualization"},
            notes = "get Actualization Procedure by ID",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_Actualization_Procedure(@ApiParam(required = true) UUID actualization_procedure_id) {
        try {
            return read(Model_UpdateProcedure.find.byId(actualization_procedure_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get ActualizationProcedure by Filter",
            tags = {"Actualization"},
            notes = "get actualization Procedure by query",
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ActualizationProcedure_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_Procedures_by_filter(int page_number) {
        try {

            // Get and Validate Object
            Swagger_ActualizationProcedure_Filter help  = formFromRequestWithValidation(Swagger_ActualizationProcedure_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_UpdateProcedure> query = Ebean.find(Model_UpdateProcedure.class);
            query.order().desc("created");

            if (help.project_id != null) {

                Model_Project.find.byId(help.project_id);
                query.where().eq("project_id", help.project_id);

            }
            if (help.project_id == null) {
                query.where().isNull("project.id");
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedure_List result = new Swagger_ActualizationProcedure_List(query,page_number, help);

            // TODO permissions

            // Vrácení objektu
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "cancel ActualizationProcedure",
            tags = {"Actualization"},
            notes = "cancel (terminate) procedure",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result canceled_procedure(@ApiParam(required = true) UUID procedure_id) {
        try {

            // Kontrola objektu
            Model_UpdateProcedure procedure = Model_UpdateProcedure.find.byId(procedure_id);

            this.checkUpdatePermission(procedure);

            procedure.cancel_procedure();

            return ok(procedure);
        } catch (Exception e) {
            return controllerServerError(e);
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
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Created",              response = Model_UpdateProcedure.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result make_actualization_procedure() {
        try {

            // Get and Validate Object
            Swagger_ActualizationProcedure_Make help  = formFromRequestWithValidation(Swagger_ActualizationProcedure_Make.class);

            // Kontrola Firmware Type
            FirmwareType firmware_type = FirmwareType.getFirmwareType(help.firmware_type);
            if (firmware_type == null)  return notFound("firmware_type not found");

            // Kontrola Projektu
            Model_Project project = Model_Project.find.byId(help.project_id);

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

            Model_HardwareGroup group = Model_HardwareGroup.find.byId(help.hardware_group_id);

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

                Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_settings.hardware_type_id);

                Model_CProgramVersion c_program_version = null;

                if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
                    c_program_version = Model_CProgramVersion.find.byId(hardware_type_settings.c_program_version_id);
                    if(c_program_version.status() != CompilationStatus.SUCCESS) {
                        return badRequest("Selected Version is not succesfully compiled and restored. Its not possible to make a update procedure with it");
                    }
                }

                Model_BootLoader bootLoader = null;

                if (firmware_type == FirmwareType.BOOTLOADER) {
                    bootLoader = Model_BootLoader.find.byId(hardware_type_settings.bootloader_id);
                    if (!bootLoader.getHardwareTypeId().equals(hardwareType.id)) badRequest("Invalid type of Bootloader for HardwareType");
                }

                List<UUID> uuid_ids = Model_Hardware.find.query().where().eq("hardware_groups.id", group.id).eq("hardware_type.id", hardwareType.id).select("id").findIds();

                for (UUID uuid_id : uuid_ids) {
                    Model_Hardware hardware = Model_Hardware.find.byId(uuid_id);

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

                    if(!hardware.database_synchronize) {
                        plan.state = HardwareUpdateState.PROHIBITED_BY_CONFIG;
                    }


                    procedure.updates.add(plan);
                }
            }

            procedure.save();

            return created(procedure);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "upload C_Program Bin File",
            tags = {"Actualization"},
            notes = "Upload manually build file on your own risk",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Upload_BIN_to_HW_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result uploadBinaryFileToBoard() {
        try {

            // Slouží k nahrávání firmwaru do deviců, které jsou ve fakce instnaci pro testování
            // nejsou databázovaný a tedy nejde spustit regulérní update procedura na kterou jsme zvyklé - viz metoda nad tímto
            // Slouží jen pro Admin rozhraní Tyriona

            // Get and Validate Object
            Swagger_Upload_BIN_to_HW_BASE64_FILE help = formFromRequestWithValidation(Swagger_Upload_BIN_to_HW_BASE64_FILE.class);

            final byte[] utf8Bytes = help.file.getBytes(StandardCharsets.UTF_8);
            System.out.println("hardwareType_uploadBin - update bin: size in bits: " + utf8Bytes.length); // prints "11"

            String file_name =   "manual_upload_file_cron_remove.bin";


            logger.debug("hardwareType_uploadBin- File Name:: " + file_name );

            // Create File - its not owned by any other model object - and there is a Cron Job witch remove this file after 24 hours.
            Model_Blob file = Model_Blob.upload(help.file, "bin", file_name , Model_Blob.get_path_for_bin());

            String build_id = "dasfsdfsdfsd TODO";


            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            for (UUID hardware_id : help.hardware_ids) {

                Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.hardware = hardware;
                b_pair.blob = file;

                b_pairs.add(b_pair);
            }

            if (!b_pairs.isEmpty()) {
                new Thread(() -> {
                    try {

                        Model_Hardware.create_update_procedure(help.firmware_type, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }

            // Vracím odpověď
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareUpdate.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_Actualization_CProgramUpdatePlan(@ApiParam(required = true) UUID plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareUpdate plan = Model_HardwareUpdate.find.byId(plan_id);

            // Vrácení objektu
            return ok(plan);

        } catch (Exception e) {
            return controllerServerError(e);
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_ActualizationProcedureTask_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Actualization_CProgramUpdatePlan_by_filter(int page_number) {
        try {

            // Get and Validate Object
            Swagger_ActualizationProcedureTask_Filter help  = formFromRequestWithValidation(Swagger_ActualizationProcedureTask_Filter.class);

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
                    Model_Hardware.find.byId(hardware_id);
                }

                query.where().in("hardware.id", help.hardware_ids);
            }

            if (!help.instance_ids.isEmpty()) {

                for (UUID instance_id : help.instance_ids) {
                    Model_Instance.find.byId(instance_id);
                }

                query.where().in("actualization_procedure.instance.instance.id", help.instance_ids);
            }

            if (!help.instance_snapshot_ids.isEmpty()) {

                for (UUID instance_id : help.instance_snapshot_ids) {
                    Model_InstanceSnapshot.find.byId(instance_id);
                }

                query.where().in("actualization_procedure.instance.id", help.instance_ids);
            }

            if (!help.actualization_procedure_ids.isEmpty()) {

                for (UUID procedure_id : help.actualization_procedure_ids) {
                    Model_UpdateProcedure.find.byId(procedure_id);
                }

                query.where().in("actualization_procedure.id", help.actualization_procedure_ids);
            }

            // Vyvoření odchozího JSON
            Swagger_ActualizationProcedureTask_List result = new Swagger_ActualizationProcedureTask_List(query, page_number,help);

            // Vrácení objektu
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}