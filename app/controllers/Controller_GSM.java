package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_GSM;
import models.Model_Garfield;
import models.Model_HardwareRegistrationEntity;
import models.Model_Project;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import responses.*;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status_cdr;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status_list;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_GSM_label_Details;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GSM_Date;
import utilities.swagger.input.Swagger_GSM_Filter;
import utilities.swagger.input.Swagger_GSM_Register;
import utilities.swagger.output.filter_results.Swagger_GSM_List;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.UUID;

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

            Model_GSM sim = Model_GSM.getById(sim_id);

            return ok(sim);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GSM List by filter",
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

    @ApiOperation(value = "delete sim",
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
        Model_GSM gsm = Model_GSM.getById(sim_id);

        gsm.delete();

        return ok();
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

    @ApiOperation(value = "todo dokumentace!", hidden =  true)
    @BodyParser.Of(BodyParser.Json.class)
    public Result credit_usage(UUID sim_id) {
        try {


            Swagger_GSM_Date help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_Date.class);

            LocalDate date_first =  help.date_first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate date_last =  help.date_last.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // nalezení sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            // ověření jestli existuje
            if (gsm == null) {
                return notFound("sim wasn't found");
            }

            TM_Sim_Status status = new Controller_Things_Mobile().sim_status(gsm.MSINumber);

            Long pocet_spotrebvonych_bitu = 0L;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm:ss");

            for(TM_Sim_Status_cdr state :  status.cdrs) {

                LocalDate cdr_start =  LocalDate.parse(state.cdrDateStart, formatter);
                LocalDate cdr_stop =  LocalDate.parse(state.cdrDateStop, formatter);

                System.out.println("CDR START: " + state.cdrDateStart + "(" + cdr_start + ")" + " END: " + state.cdrDateStop + "(" + cdr_stop + ")");

                // Tady potřebujeme porovnat zda date start je později než date_fist
                if(cdr_stop.isBefore(date_last) && cdr_start.isAfter(date_first)) {
                    System.out.println("Údaj splňuje podmínku pro přičtení");
                    pocet_spotrebvonych_bitu += state.cdrTraffic.longValue();
                }else {
                    System.out.println("Údaj nesplňuje podmínku pro přičtení");
                }

            }

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}