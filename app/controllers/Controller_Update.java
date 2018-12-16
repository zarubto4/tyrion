package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import exceptions.BadRequestException;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.CompilationStatus;
import utilities.enums.FirmwareType;
import utilities.enums.UpdateType;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_HardwareUpdate_List;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Update extends _BaseController {


// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Update.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private final UpdateService updateService;

    @Inject
    public Controller_Update(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService, NotificationService notificationService, UpdateService updateService) {
        super(ws, formFactory, config, permissionService, notificationService);
        this.updateService = updateService;
    }

// ACTUALIZATION PROCEDURE #############################################################################################

    @ApiOperation(value = "make HardwareUpdateProcedure",
            tags = {"HardwareUpdate"},
            notes = "make procedure",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareUpdate_Make",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Created",              response = Swagger_HardwareUpdate_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result make_hardwareUpdateRelease() {
        try {

            // Get and Validate Object
            Swagger_HardwareUpdate_Make help = formFromRequestWithValidation(Swagger_HardwareUpdate_Make.class);

            // Kontrola Projektu
            Model_Project project = Model_Project.find.byId(help.project_id);

            List<Model_Hardware> hardwareList = Model_Hardware.find.query().where()
                    .or(
                         Expr.in("hardware_groups.id", help.hardware_group_ids),
                         Expr.in("id", help.hardware_ids)
                    ).findList();


            Map<String, UUID> tracking_hash = new HashMap<>();
            tracking_hash.put("PROCEDURE", UUID.randomUUID());

            for(Model_Hardware hardware : hardwareList) {
                // Planed
                if (help.time != null && help.time != 0L) {
                    updateService.scheduleUpdate(new Date(help.time), hardware, help.getComponent(hardware.hardware_type), help.firmware_type, UpdateType.MANUALLY_RELEASE_MANAGER, tracking_hash);

                // Not planed
                } else {
                    updateService.update(hardware, help.getComponent(hardware.hardware_type), help.firmware_type, UpdateType.MANUALLY_RELEASE_MANAGER, tracking_hash);
                }
            }


            Query<Model_HardwareUpdate> query = Ebean.find(Model_HardwareUpdate.class);
            query.where().in("tracking_procedure_id", tracking_hash.get("PROCEDURE"));


        // Vyvoření odchozího JSON
        Swagger_HardwareUpdate_List result = new Swagger_HardwareUpdate_List(query, 0, help);


            return created(result);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "upload C_Program into Hardware",
            tags = {"HardwareUpdate"},
            notes = "Upload compilation to list of hardware. Compilation is on Version oc C_Program. And before uplouding compilation, you must succesfuly compile required version before! " +
                    "Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_DeployFirmware",
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
    public Result hardware_updateFirmware() {
        try {

            // Get and Validate Object
            Swagger_DeployFirmware help = formFromRequestWithValidation(Swagger_DeployFirmware.class);

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.find.byId(help.c_program_version_id);

            this.checkReadPermission(version);

            if (version.getCompilation() == null) {
                throw new BadRequestException("Compilation is missing");
            }

            // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
            if (version.getCompilation().status != CompilationStatus.SUCCESS) return badRequest("You cannot upload code in state:: " + version.getCompilation().status.name());

                // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(help.hardware_id);
            this.updateService.update(hardware, version, FirmwareType.FIRMWARE, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, new HashMap<>());


            // Vracím odpověď
            return ok();

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

                        // TODO update Model_Hardware.create_update_procedure(help.firmware_type, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);

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
    public Result get_HardwareUpdate_CProgramUpdatePlan(@ApiParam(required = true) UUID plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareUpdate plan = Model_HardwareUpdate.find.byId(plan_id);

            // Vrácení objektu
            return ok(plan);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareUpdateTask by Filter",
            tags = {"HardwareUpdate"},
            notes = "get actualization Tasks by query",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareUpdates_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_HardwareUpdate_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_HardwareUpdate_CProgramUpdatePlan_by_filter(int page_number) {
        try {

            // Get and Validate Object
            Swagger_HardwareUpdates_Filter help  = formFromRequestWithValidation(Swagger_HardwareUpdates_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_HardwareUpdate> query = Ebean.find(Model_HardwareUpdate.class);
            query.order().desc("created");


            if (help.update_states != null && !help.update_states.isEmpty()) {
                query.where().in("state", help.update_states);
            }

            if (help.type_of_updates != null && !help.type_of_updates.isEmpty()) {
                 query.where().in("type_of_update", help.type_of_updates);
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

                query.where().in("tracking_id_instance_id", help.instance_ids);
            }

            if (!help.instance_snapshot_ids.isEmpty()) {

                for (UUID instance_id : help.instance_snapshot_ids) {
                    Model_InstanceSnapshot.find.byId(instance_id);
                }

                query.where().in("tracking_id_snapshot_id", help.instance_snapshot_ids);
            }


            // Vyvoření odchozího JSON
            Swagger_HardwareUpdate_List result = new Swagger_HardwareUpdate_List(query, page_number,help);

            // Vrácení objektu
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}