package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_GSM;
import play.mvc.BodyParser;
import play.mvc.Result;
import responses.*;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_List;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_List_list;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GSM_Filter;
import utilities.swagger.input.Swagger_GSM_New;
import utilities.swagger.output.filter_results.Swagger_GSM_List;

import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_GSM extends _BaseController {

// LOGGER ##############################################################################################################

        private static final Logger logger = new Logger(Controller_GSM.class);

// CONTROLLER CONFIGURATION ############################################################################################

        private _BaseFormFactory baseFormFactory;

        @Inject
        public Controller_GSM(_BaseFormFactory formFactory) {
            this.baseFormFactory = formFactory;
        }
///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    @ApiOperation(value = "create Sim",
            tags = {"GSM"},
            notes = "create new Sim with unique MSINumber",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GSM_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class ),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result register_Sim(){
        try {
            //hledam si list se sim
            Controller_Things_Mobile things_mobile = new Controller_Things_Mobile();
            TM_Sim_List_list list = things_mobile.sim_list();

            //procházím list a hledám pokud v něm sim s MSINumber existuje
            //pokud ne vytvářím si novou a ukládám jí do databáze
            for (TM_Sim_List sim : list.sims) {
                if (Model_GSM.find.query().where().eq("MSINumber", sim.msisdn).findCount() == 0) {
                    Model_GSM gsm = new Model_GSM();
                    gsm.MSINumber = sim.msisdn;
                    gsm.save();
                }
            }

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Sim",
            tags = {"GSM"},
            notes = "get Sim",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GSM.class ),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_Sim(UUID sim_id) {
        try {

            Model_GSM sim = Model_GSM.getById(sim_id);

            return ok(sim);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get GSM Short List by filter",
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
    public Result get_Sim_by_filter(Integer page_number) {
        try {

            // Get and Validate Object
            Swagger_GSM_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_Filter.class);


            // Tvorba parametru dotazu
            Query<Model_GSM> query = Ebean.find(Model_GSM.class);
            query.where().eq("deleted", false);

            if (help.project_id != null ) {
                query.where().eq("project_id", help.project_id);
            }

            if (help.project_id == null) {
                query.where().isNull("project.id");
            }

            // Vytvářím seznam podle stránky
            Swagger_GSM_List result = new Swagger_GSM_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}