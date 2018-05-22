package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.BoardRegistrationStatus;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_GSM_label_Details;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Entity_Registration_Status;
import utilities.swagger.output.filter_results.Swagger_GSM_List;

import java.util.UUID;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_GSM extends _BaseController {

// LOGGER ##############################################################################################################

        private static final Logger logger = new Logger(Controller_GSM.class);

// CONTROLLER CONFIGURATION ############################################################################################

        @Inject
        public static _BaseFormFactory baseFormFactory;

        @Inject
        public Controller_GSM(_BaseFormFactory formFactory) {
            this.baseFormFactory = formFactory;
        }


///###################################################################################################################*/


    @ApiOperation(value = "register Sim",
            tags = {"GSM"},
            notes = "register SIM to project",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GSM_Register",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GSM.class ),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result register_sim(){
        try {

            // Všechny Simkarty jsou dopředu registrované v naší databázi a to díky speciální třídě Job_ThingsMobileSimListOnlySynchronizer
            // která se spouští každých cirka 30 minut. Tato třída vezme celý seznam simkaret z !ThingsMobile (nebo do budoucna dalších providerů SIM)
            // a synchronizuje databázi. To jest pokud objekt neexistuje, pak ho vytvoří. Tento Model_GSM zatím nemá vlastníka, ale má registrační token.
            // Tento token se spolu s dalšími údaji vytiskne na lepící štítek a nalepí na zadní stranu GSM modulu, který nese simku, anténu atd..
            // Simkarty totiž používáme SMD - to jsou takové, které jsou přímo připájené na tišták. Vypadají jako maličký procesor.

            // Zde se ted\ registruje simkarta nikoliv podle čísla msinumber, ale podle registračního HASHe. To proto, že msinumber jdou hezky po sobě 87000000001, 87000000002, 87000000003, 87000000004....
            // a to je velký problém. Proto při ukládání použijeme registration_hash což je UUID string.

            // Get and Validate Object
            Swagger_GSM_Register help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_Register.class);

            Model_GSM gsm = Model_GSM.find.query().where().eq("registration_hash", help.registration_hash).findOne();
            Model_Project project = Model_Project.getById(help.project_id);

            //gsm.registratione_permission();

            if(gsm.get_project() != null) {
                return badRequest("GSM Modul is already registred!");
            }

            gsm.project = project;
            gsm.update();

            return ok(gsm);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "unregister Sim",
            tags = {"GSM"},
            notes = "unregister SIM from project",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class ),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result unregister_sim(UUID sim_id){
        try{

            Model_GSM sim = Model_GSM.getById(sim_id);
            sim.un_registration_permission();

            sim.project = null;
            sim.update();

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Sim",
            tags = {"GSM"},
            notes = "get Sim by id, not by msinumber",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GSM.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_sim(UUID sim_id) {
        try {
            // Najdu sim
            Model_GSM sim = Model_GSM.getById(sim_id);

            //vypíšu sim
            return ok(sim);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Sim List by filter",
            tags = {"GSM"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GSM_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_GSM_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_sim_by_filter(Integer page_number) {
        try {
            // Get and Validate Object
            Swagger_GSM_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_Filter.class);

            // Tvorba parametru dotazu
            Query<Model_GSM> query = Ebean.find(Model_GSM.class);
            query.where().eq("deleted", false);

            if (help.project_id != null ) {
                query.where().eq("project_id", help.project_id);
            }

            // Vytvářím seznam podle stránky
            Swagger_GSM_List result = new Swagger_GSM_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Sim",
            tags = {"GSM-admin"},
            notes = "delete sim by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result delete_sim(UUID sim_id){
        try{
            // Kontrola objektu
            Model_GSM gsm = Model_GSM.getById(sim_id);

            if(!person().is_admin()) {
                return forbidden();
            }

            // Smazání objektu
            gsm.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "print Sim Sticker",
            tags = {"GSM-admin"},
            notes = "print Sticker for GSM Modul",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result print_sim(UUID sim_id) {
        try {

            // Najdu sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            gsm.check_read_permission();

            // Vytvořím PRINT SERVISE
            Printer_Api api = new Printer_Api();

            // Label qith QR kode on Ethernet connector
            Label_62_GSM_label_Details label_12_mm_details = new Label_62_GSM_label_Details(gsm);

            Model_Garfield garfield = Model_Garfield.find.query().setMaxRows(1).findOne();

            api.printFile(garfield.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "activate Sim",
            tags = {"GSM"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result active_sim(UUID sim_id) {
        try {

            // Najdu sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            gsm.block();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Sim",
            tags = {"GSM"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result de_active_sim(UUID sim_id) {
        try {

            // Najdu sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            gsm.unblock();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Sim ",
            tags = {"GSM"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GSM_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GSM.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result edit_sim(UUID sim_id) {
        try {

            Swagger_GSM_Edit help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_Edit.class);

            //hledám objekt
            Model_GSM gsm = Model_GSM.getById(sim_id);

            //zde objekt edituju
            gsm.name = help.name;
            gsm.description = help.description;

            gsm.daily_traffic_threshold                 = help.daily_traffic_threshold;
            gsm.daily_traffic_threshold_exceeded_limit  = help.daily_traffic_threshold_exceeded_limit;
            gsm.daily_traffic_threshold_notify_type     = help.daily_traffic_threshold_notify_type;

            gsm.monthly_traffic_threshold                = help.monthly_traffic_threshold;
            gsm.monthly_traffic_threshold_exceeded_limit = help.monthly_traffic_threshold_exceeded_limit;
            gsm.monthly_traffic_threshold_notify_type    = help.monthly_traffic_threshold_notify_type;

            gsm.total_traffic_threshold                 = help.total_traffic_threshold;
            gsm.total_traffic_threshold_exceeded_limit  = help.total_traffic_threshold_exceeded_limit;
            gsm.total_traffic_threshold_notify_type     = help.total_traffic_threshold_notify_type;

            gsm.update();


            gsm.setTags(help.tags);


            // Set Trashold to Things Mobile
            gsm.set_trashholds(
                    gsm.daily_traffic_threshold , gsm.daily_traffic_threshold_exceeded_limit,
                    gsm.monthly_traffic_threshold , gsm.monthly_traffic_threshold_exceeded_limit,
                    gsm.total_traffic_threshold , gsm.total_traffic_threshold_exceeded_limit
            );

            return ok(gsm);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "check Sim registration status",
            tags = {"GSM"},
            notes = "Check SIM state for new Registration. Types of responses in JSON state value" +
                    "[CAN_REGISTER, NOT_EXIST, ALREADY_REGISTERED_IN_YOUR_ACCOUNT, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE]... \n " +
                    "PERMANENTLY_DISABLED - sim was removed by Byzance. \n" +
                    "BROKEN_DEVICE - modul exist - but its not possible to registered that. Damaged during manufacturing. ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Entity_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result gsm_check(UUID registration_hash, UUID project_id) {
        try {

            logger.trace("hardware_check:: Registration_hash: {} ",  registration_hash);
            logger.trace("hardware_check:: Project_id: {}", project_id.toString());

            Swagger_Entity_Registration_Status status = new Swagger_Entity_Registration_Status();

            // Kontrola projektu
            Model_Project.getById(project_id);

            // Kotrola objektu
            Model_GSM gsm = Model_GSM.find.query().where().eq("registration_hash", registration_hash).findOne();

            if (gsm == null) {
                status.status = BoardRegistrationStatus.NOT_EXIST;
                return ok(status);
            }

            /* GSM Model to nemá integrované
                if(gsm.state != null && hardware.state.equals("PERMANENTLY_DISABLED")) {
                    status.status = BoardRegistrationStatus.PERMANENTLY_DISABLED;
                    return ok(status);
                }
            */

            if (Model_GSM.find.query().where().eq("id", gsm.id).isNull("project.id").findCount() == 1) {
                status.status = BoardRegistrationStatus.CAN_REGISTER;
                return ok(status);
            }
            if (Model_GSM.find.query().where().eq("id", gsm.id).eq("project.id", project_id).findCount() == 1) {
                status.status = BoardRegistrationStatus.ALREADY_REGISTERED_IN_YOUR_ACCOUNT;
                return ok(status);
            }

            status.status = BoardRegistrationStatus.ALREADY_REGISTERED;
            return ok(status);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "get Sim credit usage",
            tags = {"GSM"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = DataSim_overview.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result credit_usage(UUID sim_id) {
        try {
            Swagger_GSM_From_To help = new Swagger_GSM_From_To();
            //swagger třída odkdy, do kdy Long 234567876543456 a od kdy 5643256432146
            // nebo to bude string kde "period" = "7days" nebo "LastMonth"

            DataSim_overview overview = Model_GSM.getById(sim_id).louskani(help.from,help.to);

            return ok(overview);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}