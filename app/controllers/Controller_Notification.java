package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_Notification;
import play.libs.ws.WSClient;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationState;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationConfirmationService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.Swagger_Notification_Confirm;
import utilities.swagger.input.Swagger_Notification_Read;
import utilities.swagger.output.filter_results.Swagger_Notification_List;

import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Notification extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Notification.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private final NotificationConfirmationService notificationConfirmationService;

    @Inject
    public Controller_Notification(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                                   NotificationService notificationService, NotificationConfirmationService notificationConfirmationService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.notificationConfirmationService = notificationConfirmationService;
    }

// PUBLIC CONTROLLER METHODS ###########################################################################################

    @ApiOperation(value = "get Notification latest",
            tags = {"Notifications"},
            notes = "Get list of latest user notifications. Server return maximum 25 latest objects. \n\n " +
                    "For get another page (next 25 notifications) call this api with \"page_number\" path parameter. \n\n " +
                    "May missing or you can insert Integer values from page[1,2...,n] in Json" +
                    "Notification body cannot by documented through swagger. Visit wiki.byzance.cz",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Notification_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result notification_getByFilter(@ApiParam(value = "page_number is Integer. Contain  1,2... " + " For first call, use 1", required = false) Integer page_number) {
        Query<Model_Notification> query =  Model_Notification.find.query().where().eq("person.id", _BaseController.personId()).eq("deleted", false).order().desc("created");

        Swagger_Notification_List result = new Swagger_Notification_List(query, page_number);

        return ok(result);
    }

    @ApiOperation(value = "delete Notification",
            tags = {"Notifications"},
            notes = "remove notification by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Delete Successful",       response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result notification_delete(UUID notification_id) {
        Model_Notification notification = Model_Notification.find.byId(notification_id);
        notification.state = NotificationState.DELETED;
        this.notificationService.send(notification.getPerson(), notification);
        return delete(notification);
    }

    @ApiOperation(value = "mark Notifications as read",
            tags = {"Notifications"},
            notes = "Mark notifications as read. Send list with ids",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Notification_Read",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully marked as read", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",                response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",        response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",           response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result notification_read() {

        // Get and Validate Object
        Swagger_Notification_Read help = formFromRequestWithValidation(Swagger_Notification_Read.class);

        List<Model_Notification> notifications = Model_Notification.find.query().where().idIn(help.notification_id).findList();

        for (Model_Notification notification : notifications) {

            this.checkUpdatePermission(notification);

            notification.set_read();
            notification.state = NotificationState.UPDATED;
            this.notificationService.send(notification.getPerson(), notification);
        }

        return ok();
    }

    @ApiOperation(value = "get Notifications unconfirmed",
            tags = {"Notifications"},
            notes = "This API should by called right after user logs in. Sends notifications which require confirmation via websocket.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result notifications_getUnconfirmed() {

        List<Model_Notification> notifications = Model_Notification.find.query().where().eq("person.id", personId()).eq("notification_importance", NotificationImportance.HIGH).eq("confirmed", false).findList();
        if (notifications.isEmpty()) return ok("No new notifications");

        for (Model_Notification notification : notifications) {
            notification.state = NotificationState.UNCONFIRMED;
            this.notificationService.send(notification.getPerson(), notification);
        }

        return ok("Notifications were sent again");
    }

    @ApiOperation(value = "confirm Notification",
            tags = {"Notifications"},
            notes = "Confirms notification",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Notification_Confirm",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result notification_confirm(UUID notification_id) {

        // Get and Validate Object
        Swagger_Notification_Confirm help = formFromRequestWithValidation(Swagger_Notification_Confirm.class);

        // Kontrola objektu
        Model_Notification notification = Model_Notification.find.byId(notification_id);

        this.checkUpdatePermission(notification);

        this.notificationConfirmationService.confirm(notification, help.action, help.payload);

        return ok();
    }
}
