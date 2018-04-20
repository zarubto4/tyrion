package controllers;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import models.Model_GSM;
import play.mvc.BodyParser;
import play.mvc.Result;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GSM_New;

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
    public Result createSim(){
        try {
            Swagger_GSM_New help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_New.class);

            if (Model_GSM.find.query().where().eq("MSINumber", help.MSINumber).findOne() == null)
                return badRequest("MSINumber does not exist");

            Model_GSM sim = new Model_GSM();


            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


}
