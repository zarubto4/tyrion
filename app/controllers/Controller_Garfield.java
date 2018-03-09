package controllers;

import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.Model_Garfield;
import models.Model_Hardware;
import models.Model_HardwareBatch;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
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
import utilities.swagger.input.Swagger_Bug_Description;
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
    private _BaseFormFactory baseFormFactory;

    @Inject public Controller_Garfield(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

// REST - API GARFIELD  #################################################################################################

    @ApiOperation(value = "edit Garfield",
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
    public Result edit_Garfield(@ApiParam(required = true) UUID garfield_id) {
        try {

            // Get and Validate Object
            Swagger_Garfield_Edit help  = baseFormFactory.formFromRequestWithValidation(Swagger_Garfield_Edit.class);

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfield_id);

            garfield.name = help.name;
            garfield.description = help.description;
            garfield.hardware_tester_id = help.hardware_tester_id;
            garfield.print_label_id_1 =  help.print_label_id_1;  // 12 mm
            garfield.print_label_id_2 =  help.print_label_id_2;  // 24 mm
            garfield.print_sticker_id =  help.print_sticker_id; // 65 mm

            garfield.update();

            // Vrácení objektu
            return ok(garfield.json());

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
            Swagger_Garfield_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_Garfield_New.class);

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

            return ok(garfield.json());

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
    public Result remove_Garfield(@ApiParam(required = true) UUID garfield_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfield_id);

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
    public Result get_Garfield(@ApiParam(required = true) UUID garfield_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfield_id);

            // Vrácení objektu
            return ok(Json.toJson(garfield));

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
    public Result print_label(@ApiParam(required = true) UUID board_id) {
        try {


            // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.getById(board_id);

            Model_HardwareBatch batch = Model_HardwareBatch.getById(hardware.batch_id);

            // TODO tady je potřeba pohlídat online tiskárny - tiskne se na prvním garfieldovy - to není uplně super cool věc
            // Zrovna mě ale nenapadá jak v rozumném čase doprogramovat řešení lépe - snad jen pomocí selektoru tiskáren???
            // Tím pádem bude potřeba mít tiskárny trochu lépe pošéfované
            List<UUID> garfields_id = Model_Garfield.find.query().where().eq("hardware_type_id", hardware.hardware_type_id()).findIds();
            if (garfields_id.isEmpty()) {
                logger.error("print_label:: garfields for this type of hardware not found");
                return notFound("Garfield for this type of hardware not found");
            }

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfields_id.get(0));

            Printer_Api api = new Printer_Api();

            // Label 62 mm
            try {
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(hardware, batch, garfield);
            } catch (IllegalArgumentException e) {
                logger.error("print_label:: Label_62_mm_package printer info Error, " + e.getMessage());
                return badRequest("Something is wrong: " + e.getMessage());
            }

            // Label 62 mm
            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(hardware, batch, garfield);
            api.printFile(garfield.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(hardware);
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
            return ok(Json.toJson(garfield_s));

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
    public Result online_state_Printer(@ApiParam(required = true) String garfield_id, @ApiParam(required = true) Integer printer_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfield_id);

            if (!( garfield.print_label_id_1.equals(printer_id) || garfield.print_label_id_2.equals(printer_id) || garfield.print_sticker_id.equals(printer_id))) {
                return forbidden();
            }

            Printer printer =  Printer_Api.get_printer(printer_id);

            // Vrácení objektu
            return ok(Json.toJson(printer));

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
    public Result print_test_Printer(@ApiParam(required = true) String garfield_id, @ApiParam(required = true) Integer printer_id) {
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.getById(garfield_id);

            if (garfield.print_label_id_1.equals(printer_id)) {
                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if (garfield.print_label_id_2.equals(printer_id)) {

                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if (garfield.print_sticker_id.equals(printer_id)) {

                Model_Hardware board = new Model_Hardware();
                board.id = UUID.randomUUID();
                board.full_id = "123456789123456789123456";

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
                Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(board, info, garfield);

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
