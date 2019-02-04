package _projects.cez;

import _projects.cez.swagger_model.in.Swagger_CEZ_data_request;
import _projects.eon.swagger_model.in.Swagger_EON_data_request;
import _projects.eon.swagger_model.out.Swagger_EON_data_values;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.*;
import mongo.mongo_services._MongoNativeCollection;
import mongo.mongo_services._MongoNativeConnector;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Result;
import responses.*;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;


// @Security.Authenticated(Authentication.class)
@Api(value = "CEZ")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_CEZ extends _BaseController {

    private HttpExecutionContext httpExecutionContext;
    private _MongoNativeConnector mongoNativeConnector;

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_projects.eon.Controller_EON.class);

    @Inject
    public Controller_CEZ(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                          NotificationService notificationService, EchoService echoService, _MongoNativeConnector mongoNativeConnector, HttpExecutionContext httpExecutionContext) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.httpExecutionContext = httpExecutionContext;
        this.mongoNativeConnector = mongoNativeConnector;
    }


    @ApiOperation(
            value = "get Sensor_Data",
            tags = {"CEZ"},
            notes = "values in given date range, from given hardware, averaged inside time intervals given in minutes"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "_projects.cez.swagger_model.in.Swagger_CEZ_data_request",
                            required = true,
                            paramType = "body",
                            value = "constraints for query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_EON_data_values.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result getValues() {
        try {

            Swagger_CEZ_data_request request = formFromRequestWithValidation(Swagger_CEZ_data_request.class);
            _MongoNativeCollection collection = mongoNativeConnector.getDatabaseCollection("4d7fd102-a3b4-4dde-914c-c0dcfaa545ac", "PRIME");

            // Project ID::

            // return ok_mongo();

            return ok();

        } catch (Throwable e) {
            return controllerServerError(e);
        }
    }



}
