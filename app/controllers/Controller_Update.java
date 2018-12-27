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
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.*;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_HardwareReleaseUpdate_List;
import utilities.swagger.output.filter_results.Swagger_HardwareUpdate_List;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Update extends _BaseController {


// LOGGER ##############################################################################################################

    public static final Logger logger = new Logger(Controller_Update.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private final UpdateService updateService;
    private final HardwareService hardwareService;

    @Inject
    public Controller_Update(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                             NotificationService notificationService, UpdateService updateService,  HardwareService hardwareService) {
        super(ws, formFactory, config, permissionService, notificationService);
        this.updateService = updateService;
        this.hardwareService = hardwareService;
    }



// SINGLE UPDATES  #####################################################################################################


    @ApiOperation(value = "update Hardware",
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


            // Chech Harware permission
            Model_Hardware hardware = Model_Hardware.find.byId(help.hardware_id);

            // For CProgram
            if (help.firmware_type == FirmwareType.FIRMWARE || help.firmware_type == FirmwareType.BACKUP ) {

                if (help.c_program_version != null) {

                    this.checkUpdatePermission(hardware);
                    this.checkUpdatePermission(help.c_program_version);

                    this.updateService.update(hardware, help.c_program_version, help.firmware_type, UpdateType.MANUALLY_BY_USER_INDIVIDUAL);

                } else {

                    Model_Compilation compilation = new Model_Compilation();
                    compilation.blob = Model_Blob.upload( help.file_base_64, "blob", help.firmware_type + ".bin", hardware.getPath());
                    compilation.firmware_build_id = help.firmware_build_id;
                    compilation.firmware_version_lib = "Manual Update"; // TODO https://youtrack.byzance.cz/youtrack/issue/HW-1375
                    compilation.firmware_build_datetime = new Date(); // TODO https://youtrack.byzance.cz/youtrack/issue/HW-1375
                    compilation.save();
                    compilation.refresh();

                    this.updateService.update(hardware, compilation, help.firmware_type, UpdateType.MANUALLY_BY_USER_INDIVIDUAL);

                }

            }


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


// ACTUALIZATION PROCEDURE #############################################################################################

    @ApiOperation(value = "make HardwareReleaseUpdate",
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
            @ApiResponse(code = 201, message = "Ok Created",              response = Model_HardwareReleaseUpdate.class),
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
            this.checkUpdatePermission(project);

            List<Model_Hardware> hardwareList = Model_Hardware.find.query().where()
                    .or(
                         Expr.in("hardware_groups.id", help.hardware_group_ids),
                         Expr.in("id", help.hardware_ids)
                    ).findList();


            Model_HardwareReleaseUpdate releaseUpdate = new Model_HardwareReleaseUpdate();
            releaseUpdate.project_id = help.project_id;
            releaseUpdate.save();

            for (Model_Hardware hardware : hardwareList) {
                // Planed
                if (help.time != null && help.time != 0L) {
                    this.updateService.schedule(new Date(help.time), hardware, help.getComponent(hardware.hardware_type), help.firmware_type, UpdateType.MANUALLY_RELEASE_MANAGER, releaseUpdate.getId());

                // Not planed
                } else {
                    this.updateService.update(hardware, help.getComponent(hardware.hardware_type), help.firmware_type, UpdateType.MANUALLY_RELEASE_MANAGER, releaseUpdate.getId());
                }
            }

            return created(releaseUpdate);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareReleaseUpdate",
            tags = {"Actualization"},
            notes = "get Actualization task by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareReleaseUpdate.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_hardwareUpdateRelease(@ApiParam(required = true) UUID plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareReleaseUpdate plan = Model_HardwareReleaseUpdate.find.byId(plan_id);

            // Vrácení objektu
            return ok(plan);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "cancel HardwareReleaseUpdate",
            tags = {"Actualization"},
            notes = "get Actualization task by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareReleaseUpdate.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result cancel_hardwareUpdateRelease(@ApiParam(required = true) UUID plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareReleaseUpdate plan = Model_HardwareReleaseUpdate.find.byId(plan_id);

            List<Model_HardwareUpdate> updates = Model_HardwareUpdate.find.query().where()
                    .eq("tracking_id", plan_id)
                    .eq("state", HardwareUpdateState.PENDING)
                    .findList();

            for (Model_HardwareUpdate update : updates) {
                updateService.cancel(update.getId());
            }

            // Vrácení objektu
            return ok(plan);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareReleaseUpdate by Filter",
            tags = {"HardwareUpdate"},
            notes = "get release Update by query",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareReleaseUpdate_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_HardwareReleaseUpdate_List.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_HardwareReleaseUpdate_by_filter(int page_number) {
        try {


            Swagger_HardwareReleaseUpdate_Filter help  = formFromRequestWithValidation(Swagger_HardwareReleaseUpdate_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_HardwareReleaseUpdate> query = Ebean.find(Model_HardwareReleaseUpdate.class);
            query.order().desc("created");

            if (help.project_id != null) {
                query.where().in("project_id", help.project_id);
            }

            // Vyvoření odchozího JSON
            Swagger_HardwareReleaseUpdate_List result = new Swagger_HardwareReleaseUpdate_List(query, page_number,help);

            // Vrácení objektu
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// C PROGRAM ACTUALIZATION PLAN ########################################################################################

    @ApiOperation(value = "get HardwareUpdate",
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

    @ApiOperation(value = "cancel HardwareUpdate",
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
    public Result cancel_HardwareUpdate_CProgramUpdatePlan(@ApiParam(required = true) UUID plan_id) {
        try {

            // Kontrola objektu
            Model_HardwareUpdate plan = Model_HardwareUpdate.find.byId(plan_id);

            updateService.cancel(plan_id);

            // Vrácení objektu
            return ok(plan);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareUpdate by Filter",
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
                 query.where().in("type", help.type_of_updates);
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

                query.where().in("tracking_id", help.instance_ids);
            }

            if (!help.instance_snapshot_ids.isEmpty()) {

                for (UUID instance_id : help.instance_snapshot_ids) {
                    Model_InstanceSnapshot.find.byId(instance_id);
                }
                query.where().in("tracking_id", help.instance_snapshot_ids);
            }

            if (!help.release_update_ids.isEmpty()) {

                for (UUID release_update_id : help.release_update_ids) {
                    Model_HardwareReleaseUpdate.find.byId(release_update_id);
                }

                query.where().in("tracking_id", help.release_update_ids);
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