package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_GSM;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GSM_From_To;
import utilities.swagger.input.Swagger_HardwareType_New;

import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    // Nothing
    private Config config;

    @Inject
    public static _BaseFormFactory baseFormFactory;

    @Inject
    public Controller_ZZZ_Tester(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }


    // CONTROLLER CONTENT ##################################################################################################
    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {

             return ok("Valid version for mode");
         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2(UUID sim_id) {
        try {

            Swagger_GSM_From_To help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_From_To.class);

            // nalezení sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            // ověření jestli existuje
            if (gsm == null) {
                return notFound("sim wasn't found");
            }
            DataSim_overview overview = gsm.louskani(help.from, help.to);

           return ok( Json.toJson(overview) );
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try{
            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {

            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }




}