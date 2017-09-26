package controllers;

import io.swagger.annotations.*;
import models.Model_Board;
import models.Model_Garfield;
import models.Model_TypeOfBoard_Batch;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.lablel_printer_service.printNodeModels.Printer;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Ok;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_Garfield_Edit;
import utilities.swagger.documentationClass.Swagger_Garfield_New;

import java.util.List;
import java.util.UUID;

/**
 * Created by zaruba on 23.08.17.
 */

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Garfield extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Garfield.class);

// REST - API GARFIELD -------------------------------------------------------------------------------------------------

    @ApiOperation(value = "edit Garfield",
            tags = {"Garfield"},
            notes = "edit Garfield",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Garfield_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_Garfield(@ApiParam(required = true) String garfield_id){
        try {

            // Zpracování Json
            final Form<Swagger_Garfield_Edit> form = Form.form(Swagger_Garfield_Edit.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Garfield_Edit help = form.get();

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.get_byId(garfield_id);
            if (garfield == null) return GlobalResult.result_notFound("Garfield not found");

            garfield.name = help.name;
            garfield.description = help.description;
            garfield.hardware_tester_id = help.hardware_tester_id;
            garfield.print_label_id_1 =  help.print_label_id_1;  // 12 mm
            garfield.print_label_id_2 =  help.print_label_id_2;  // 24 mm
            garfield.print_sticker_id =  help.print_sticker_id; // 65 mm

            // Kontrola oprávnění
            if (!garfield.edit_permission()) return GlobalResult.result_forbidden();

            garfield.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(garfield));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Garfield",
            tags = {"Garfield"},
            notes = "create Garfield",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Garfield_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result create_Garfield(){
        try {

            // Zpracování Json
            final Form<Swagger_Garfield_New> form = Form.form(Swagger_Garfield_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Garfield_New help = form.get();

            // Kontrola objektu
            Model_Garfield garfield = new Model_Garfield();

            garfield.name = help.name;
            garfield.description = help.description;
            garfield.hardware_tester_id = help.hardware_tester_id;
            garfield.print_label_id_1 =  help.print_label_id_1;  // 12 mm
            garfield.print_label_id_2 =  help.print_label_id_2;  // 24 mm
            garfield.print_sticker_id =  help.print_sticker_id; // 65 mm

            garfield.type_of_board_id = help.type_of_board_id;
            garfield.producer_id = help.producer_id;

            // Kontrola oprávnění
            if (!garfield.create_permission()) return GlobalResult.result_forbidden();

            garfield.save();

            return GlobalResult.result_ok(Json.toJson(garfield));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Garfield",
            tags = {"Garfield"},
            notes = "get Garfield  by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Garfield(@ApiParam(required = true) String garfield_id){
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.get_byId(garfield_id);
            if (garfield == null) return GlobalResult.result_notFound("Garfield not found");

            // Kontrola oprávnění
            if (! garfield.delete_permission()) return GlobalResult.result_forbidden();

            // Odsranit objekt
            garfield.delete();

            // Vrácení objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Garfield",
            tags = {"Garfield"},
            notes = "get Garfield  by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Garfield(@ApiParam(required = true) String garfield_id){
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.get_byId(garfield_id);
            if (garfield == null) return GlobalResult.result_notFound("Garfield not found");

            // Kontrola oprávnění
            if (! garfield.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(garfield));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Garfield List",
            tags = {"Garfield"},
            notes = "get Garfield List",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Garfield.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Garfield_list(){
        try {

            if(!Controller_Security.get_person().has_permission(Model_Garfield.permissions.Garfield_read.name()))  return GlobalResult.result_forbidden();

            // Kontrola objektu
            List<Model_Garfield> garfield_s = Model_Garfield.find.where().orderBy("UPPER(name) ASC").findList();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(garfield_s));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// Printer TASK --------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get_Online_State Printer",
            tags = {"Garfield"},
            notes = "get online state Printer by ID",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Printer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result online_state_Printer(@ApiParam(required = true) String garfield_id, @ApiParam(required = true) Integer printer_id){
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);
            if (garfield == null) return GlobalResult.result_notFound("Garfield not found");

            // Kontrola oprávnění
            if (! garfield.read_permission()) return GlobalResult.result_forbidden();


            if(! ( garfield.print_label_id_1.equals(printer_id) || garfield.print_label_id_2.equals(printer_id) || garfield.print_sticker_id.equals(printer_id))){

                return GlobalResult.result_forbidden();
            }

            Printer printer =  Printer_Api.get_printer(printer_id);

            if(printer == null) return GlobalResult.result_notFound("Printer not found");

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(printer));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "test_printing Printer",
            tags = {"Garfield"},
            notes = "Random Generated Print test",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result print_test_Printer(@ApiParam(required = true) String garfield_id, @ApiParam(required = true) Integer printer_id){
        try {

            // Kontrola objektu
            Model_Garfield garfield = Model_Garfield.find.byId(garfield_id);
            if (garfield == null) return GlobalResult.result_notFound("Garfield not found");

            // Kontrola oprávnění
            if (! garfield.read_permission()) return GlobalResult.result_forbidden();


            if(garfield.print_label_id_1.equals(printer_id)) {
                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if(garfield.print_label_id_2.equals(printer_id)) {

                // TODO Lexa - odzkoušet a naimlementovat tiskárny P750W
            }

            if(garfield.print_sticker_id.equals(printer_id)) {

                Model_Board board = new Model_Board();
                board.id = "123456789123456789123456";
                board.hash_for_adding = UUID.randomUUID().toString();

                Model_TypeOfBoard_Batch info = new Model_TypeOfBoard_Batch();
                info.revision = "1.9.9";
                info.production_batch = "1.9.9";
                info.date_of_assembly = "1.9.9";
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
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }



// REST - BURN TASK ----------------------------------------------------------------------------------------------------



}
