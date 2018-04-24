package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_GSM;
import play.mvc.BodyParser;
import play.mvc.Result;
import responses.*;
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Swagger_GSM_List.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result create_Sim(){
        try {
            Swagger_GSM_New help = baseFormFactory.formFromRequestWithValidation(Swagger_GSM_New.class);

            if (Model_GSM.find.query().where().eq("MSINumber", help.MSINumber).findOne() == null){
                return badRequest("MSINumber does not exist");
            }

            Model_GSM sim = new Model_GSM();
            sim.MSINumber = help.MSINumber;

            return created(help);

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
