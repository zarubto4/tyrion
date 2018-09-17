package controllers;

import com.typesafe.config.Config;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.lablel_printer_service.printNodeModels.Printer;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_Garfield_Edit;
import utilities.swagger.input.Swagger_Garfield_New;

import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Garfield extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Garfield.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Garfield(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

// REST - API GARFIELD  #################################################################################################


    // TODO Tyrio-334
    @ApiOperation(value = "edit and save Garfield",
            tags = {"Garfield"},
            notes = "edit Garfield",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Garfield_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Garfield(UUID garfield_id) {
        try {

            // Get and Validate Object
            Swagger_Garfield_Edit help  = formFromRequestWithValidation(Swagger_Garfield_Edit.class);

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);

            garfield.name = help.name;
            garfield.description = help.description;
            garfield.hardware_tester_id = help.hardware_tester_id;
            garfield.print_label_id_1 =  help.print_label_id_1;  // 12 mm
            garfield.print_label_id_2 =  help.print_label_id_2;  // 24 mm
            garfield.print_sticker_id =  help.print_sticker_id; // 65 mm

            garfield.update();

            // Vrácení objektu
            return ok(garfield);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Garfield",
            tags = {"Garfield"},
            notes = "create Garfield",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Garfield_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Garfield() {
        try {

            // Get and Validate Object
            Swagger_Garfield_New help  = formFromRequestWithValidation(Swagger_Garfield_New.class);

            // Kontrola objektu
            Model_Garfield garfield = new Model_Garfield();

            garfield.name = help.name;
            garfield.description = help.description;
            garfield.hardware_tester_id = help.hardware_tester_id;
            garfield.print_label_id_1 =  help.print_label_id_1;  // 12 mm
            garfield.print_label_id_2 =  help.print_label_id_2;  // 24 mm
            garfield.print_sticker_id =  help.print_sticker_id; // 65 mm

            garfield.hardware_type_id = help.hardware_type_id;
            garfield.producer_id = help.producer_id;

            garfield.save();

            return ok(garfield);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Garfield",
            tags = {"Garfield"},
            notes = "get Garfield  by ID",
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
    public Result remove_Garfield(UUID garfield_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);

            // Odsranit objekt
            garfield.delete();

            // Vrácení objektu
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Garfield",
            tags = {"Garfield"},
            notes = "get Garfield  by ID",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_Garfield(UUID garfield_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);

            // Vrácení objektu
            return ok(garfield);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "print_label Board",
            tags = {"Garfield"},
            notes = "Print Labels Board - Not working properly yet!",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result print_label(UUID board_id) {
        try {


            // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(board_id);
            Model_HardwareRegistrationEntity entity = Model_HardwareRegistrationEntity.getbyFull_id(hardware.full_id);

            Model_HardwareBatch batch = Model_HardwareBatch.getById(hardware.batch_id);

            // TODO tady je potřeba pohlídat online tiskárny - tiskne se na prvním garfieldovy - to není uplně super cool věc
            // Zrovna mě ale nenapadá jak v rozumném čase doprogramovat řešení lépe - snad jen pomocí selektoru tiskáren???
            // Tím pádem bude potřeba mít tiskárny trochu lépe pošéfované
            List<UUID> garfields_id = Model_Garfield.find.query().where().eq("hardware_type_id", hardware.getHardwareTypeCache_id()).findIds();
            if (garfields_id.isEmpty()) {
                logger.error("print_label:: garfields for this type of hardware not found");
                return notFound("Garfield for this type of hardware not found");
            }

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfields_id.get(0));

            Printer_Api api = new Printer_Api();

            // Label 62 mm
            try {
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(entity, batch, hardware.getHardwareTypeCache(), garfield);
            } catch (IllegalArgumentException e) {
                logger.error("print_label:: Label_62_mm_package printer info Error, " + e.getMessage());
                return badRequest("Something is wrong: " + e.getMessage());
            }

            // Label 62 mm
            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(entity, batch, hardware.getHardwareTypeCache(), garfield);
            api.printFile(garfield.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(entity);
            api.printFile(garfield.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);


            // Vrácení objektu
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Garfield List",
            tags = {"Garfield"},
            notes = "get Garfield List",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result get_Garfield_list() {
        try {

            if (!person().has_permission(Model_Garfield.Permission.Garfield_read.name()))  return forbidden();

            // Kontrola objektu
            List<Model_Garfield> garfield_s = Model_Garfield.find.query().where().orderBy("UPPER(name) ASC").findList();

            // Vrácení objektu
            return ok(garfield_s);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// Printer TASK  #######################################################################################################

    @ApiOperation(value = "get_Online_State Printer",
            tags = {"Garfield"},
            notes = "get online state Printer by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Printer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result online_state_Printer(UUID garfield_id, Integer printer_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);

            if (!( garfield.print_label_id_1.equals(printer_id) || garfield.print_label_id_2.equals(printer_id) || garfield.print_sticker_id.equals(printer_id))) {
                return forbidden();
            }

            Printer printer =  Printer_Api.get_printer(printer_id);

            // Vrácení objektu
            return ok(printer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "test_printing Printer",
            tags = {"Garfield"},
            notes = "Random Generated Print test",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result print_test_Printer(UUID garfield_id, Integer printer_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);

            if (garfield.print_label_id_1.equals(printer_id)) {
                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if (garfield.print_label_id_2.equals(printer_id)) {

                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if (garfield.print_sticker_id.equals(printer_id)) {

                Model_HardwareRegistrationEntity board = new Model_HardwareRegistrationEntity();
                board.full_id = "123456789123456789123456";
                board.hash_for_adding = "dsfasdfsdfsdfsdfasdfsdfsdfsadf";

                Model_HardwareType type = new Model_HardwareType();
                type.name = "test name";

                Model_HardwareBatch info = new Model_HardwareBatch();
                info.revision = "1.9.9";
                info.production_batch = "1.9.9";
                info.date_of_assembly = "12.11.2017";
                info.pcb_manufacture_name = "1.9.9";
                info.pcb_manufacture_id = "1.9.9";
                info.assembly_manufacture_name = "1.9.9";
                info.assembly_manufacture_id = "1.9.9";
                info.customer_product_name = "1.9.9";
                info.customer_company_name = "1.9.9";
                info.customer_company_made_description = "1.9.9";

                Printer_Api api = new Printer_Api();
                Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(board, info, type, garfield);

                api.printFile(printer_id, 1, "test", label_62_mmPackage.get_label(), null);

            }

            // Vrácení objektu
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }



// REST - BURN TASK ####################################################################################################



}
