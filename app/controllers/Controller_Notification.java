package controllers;

import com.google.inject.Inject;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_Notification;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationState;
import utilities.logger.Logger;
import utilities.notifications.NotificationActionHandler;
import utilities.swagger.input.Swagger_Notification_Confirm;
import utilities.swagger.input.Swagger_Notification_Read;
import utilities.swagger.output.filter_results.Swagger_Notification_List;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Notification extends BaseController {

// LOGGER ##############################################################################################################

  private static final Logger logger = new Logger(Controller_Notification.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Notification(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

// PUBLIC CONTROLLER METHODS ###########################################################################################

  @ApiOperation(value = "get Notification latest",
          tags = {"Notifications"},
          notes = "Get list of latest user notifications. Server return maximum 25 latest objects. \n\n " +
                  "For get another page (next 25 notifications) call this api with \"page_number\" path parameter. \n\n " +
                  "May missing or you can insert Integer values from page[1,2...,n] in Json" +
                  "Notification body cannot by documented through swagger. Visit wiki.byzance.cz",
          produces = "application/json",
          protocols = "https",
          code = 200
  )
  @ApiResponses({
          @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Notification_List.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
  })
  @Security.Authenticated(Authentication.class)
  public Result notification_getByFilter(@ApiParam(value = "page_number is Integer. Contain  1,2... " + " For first call, use 1", required = false) Integer page_number) {
     try {

        Query<Model_Notification> query =  Model_Notification.find.query().where().eq("person.id", BaseController.personId()).order().desc("created");

        Swagger_Notification_List result = new Swagger_Notification_List(query, page_number);

        return ok(Json.toJson(result));

     } catch (Exception e) {
       return internalServerError(e);
     }
  }



  @ApiOperation(value = "delete Notification",
          tags = {"Notifications"},
          notes = "remove notification by id",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
  )
  @ApiResponses({
          @ApiResponse(code = 200, message = "Delete Successful",       response = Result_Ok.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
          @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
          @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
  })
  @Security.Authenticated(Authentication.class)
  public Result notification_delete(@ApiParam(value = "notification_id String path", required = true) String notification_id) {
    try {

      Model_Notification notification = Model_Notification.getById(notification_id);
      if (notification == null) return notFound("Notification does not exist");

      if (!notification.delete_permission()) return forbiddenEmpty();

      notification.delete();
      return okEmpty();

    } catch (Exception e) {
      return internalServerError(e);
    }
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
    try {

      final Form<Swagger_Notification_Read> form = formFactory.form(Swagger_Notification_Read.class).bindFromRequest();
      if (form.hasErrors()) return invalidBody(form.errorsAsJson());
      Swagger_Notification_Read help = form.get();

      List<Model_Notification> notifications = Model_Notification.find.query().where().idIn(help.notification_id).findList();

      for (Model_Notification notification : notifications) {

        notification.set_read();
        notification.state = NotificationState.UPDATED;
        notification.send();
      }

      return okEmpty();

    } catch (Exception e) {
      return internalServerError(e);
    }
  }

  @ApiOperation(value = "get Notifications unconfirmed",
          tags = {"Notifications"},
          notes = "This API should by called right after user logs in. Sends notifications which require confirmation via websocket.",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
  )
  @ApiResponses({
          @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
  })
  @Security.Authenticated(Authentication.class)
  public Result notifications_getUnconfirmed() {
    try {

      List<Model_Notification> notifications = Model_Notification.find.query().where().eq("person.id", BaseController.personId()).eq("notification_importance", NotificationImportance.HIGH).eq("confirmed", false).findList();
      if (notifications.isEmpty()) return ok("No new notifications");

      for (Model_Notification notification : notifications) {
          notification.state = NotificationState.UNCONFIRMED;
          notification.send();
      }

      return ok("Notifications were sent again");

    } catch (Exception e) {
      return internalServerError(e);
    }
  }

  @ApiOperation(value = "confirm Notification",
          tags = {"Notifications"},
          notes = "Confirms notification",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
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
  public Result notification_confirm(@ApiParam(value = "notification_id String path", required = true) String notification_id) {
      try {

          final Form<Swagger_Notification_Confirm> form = formFactory.form(Swagger_Notification_Confirm.class).bindFromRequest();
          if (form.hasErrors()) return invalidBody(form.errorsAsJson());
          Swagger_Notification_Confirm help = form.get();

          Model_Notification notification = Model_Notification.getById(notification_id);
          if (notification == null) return notFound("Notification no longer exists");

          if (!notification.confirm_permission()) return forbiddenEmpty();

          if (notification.confirmed) return badRequest("Notification is already confirmed");

          try {

              NotificationActionHandler.perform(help.action, help.payload);

          } catch (IllegalArgumentException e) {
              BaseController.person().notification_error(e.getMessage());
          } catch (Exception e) {
              logger.internalServerError(e);
          }

          notification.confirm();

          return okEmpty();
      } catch (Exception e) {
          return internalServerError(e);
      }
  }
}