package controllers;

import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.Homer_Instance;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;
import utilities.webSocket.WS_Becki_Website;

import javax.websocket.server.PathParam;
import java.util.Date;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {

  //####################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public enum Notification_level {
      info,
      success,
      warning,
      error,
    }

    public enum Notification_type {

      SINGLE_STRING_MESSAGE,
      BOARD_UPDATE, // Typ zprávy při updatu desky
      PROJECT_INVITE, // Typ zprávy, když uživatel pozve jiného do projektu
    }


  private static void send_notification(Person person, Notification notification) {

    if (WebSocketController_Incoming.becki_website.containsKey(person.id) ) {
      WebSocketController_Incoming.becki_sendNotification( (WS_Becki_Website) WebSocketController_Incoming.becki_website.get(person.id)  , notification );
    }
  }


  // Tvroba objektů jednotlivých notifikací ############################################################################


  public static void starting_of_compilation(Person person, Version_Object version_object){

        Notification notification = new Notification();
        notification.person = person;
        notification.level = Notification_level.info;
        notification.type = Notification_type.SINGLE_STRING_MESSAGE;
        notification.created = new Date();
        notification.confirmation_required = false;

        notification.content = Json.newObject().put("message", "Server start with compilation on Version " + version_object.version_name ).toString();

        // notification.save(); - Neukládám zbytečné
        send_notification(person, notification);

  }

  public static void successful_compilation(Person person, Version_Object version_object ){

      Notification notification = new Notification();
      notification.person = person;
      notification.level = Notification_level.success;
      notification.type = Notification_type.SINGLE_STRING_MESSAGE;
      notification.created = new Date();
      notification.confirmation_required = false;

      notification.content = Json.newObject().put("message", "Compilation on Version " + version_object.version_name + " was successful").toString();

      // notification.save(); - Neukládám zbytečné
      send_notification(person, notification);
  }

  public static void unsuccessful_compilation_warn(Person person, Version_Object version_object, String reason){

      Notification notification = new Notification();
      notification.person = person;
      notification.level = Notification_level.warning;
      notification.type = Notification_type.SINGLE_STRING_MESSAGE;
      notification.created = new Date();
      notification.confirmation_required = false;

      notification.content = Json.newObject().put("message", "Compilation on Version " + version_object.version_name + " was unsuccessful, for reason: \"" + reason + "\"").toString();

      // notification.save(); - Neukládám zbytečné
      send_notification(person, notification);
  }

  public static void unsuccessful_compilation_error(Person person, Version_Object version_object){

    Notification notification = new Notification();
    notification.person = person;
    notification.level = Notification_level.error;
    notification.type = Notification_type.SINGLE_STRING_MESSAGE;
    notification.created = new Date();
    notification.confirmation_required = false;

    notification.content = Json.newObject().put("message", "Compilation on Version " + version_object.version_name + " with Critical unknown Error").toString();
    notification.save();

    send_notification(person, notification);
  }

  public static void uploud_of_Instance_start(Person person, Homer_Instance instance){

    Notification notification = new Notification();
    notification.person = person;
    notification.level = Notification_level.info;
    notification.type = Notification_type.SINGLE_STRING_MESSAGE;
    notification.created = new Date();
    notification.confirmation_required = false;

    notification.content = Json.newObject().put("message", "Server start creating new Blocko Instance on Blocko Version <b>" + instance.version_object.version_name + "</b> from Blocko program <b>" + instance.version_object.b_program.name + "</b>").toString();
    //notification.save();

    send_notification(person, notification);

  }

  public static void uploud_of_Instance_was_succesfull(Person person, Homer_Instance instance){

    Notification notification = new Notification();
    notification.person = person;
    notification.level = Notification_level.success;
    notification.type = Notification_type.SINGLE_STRING_MESSAGE;
    notification.created = new Date();
    notification.confirmation_required = false;

    notification.content = Json.newObject().put("message", "Server created successfully instance in cloud on Blocko Version <b>" + instance.version_object.version_name + "</b> from Blocko program <b>" + instance.version_object.b_program.name + "</b>").toString();
    notification.save();

    send_notification(person, notification);
  }

  public static void uploud_of_Instance_was_unsuccesfull(Person person, Homer_Instance instance, String reason){

    Notification notification = new Notification();
    notification.person = person;
    notification.level = Notification_level.warning;
    notification.type = Notification_type.SINGLE_STRING_MESSAGE;
    notification.created = new Date();
    notification.confirmation_required = false;

    notification.content =  Json.newObject().put("message", "Server not upload instance to cloud on Blocko Version <b>" + instance.version_object.version_name + "</b> from Blocko program <b>" + instance.version_object.b_program.name + "</b> for <b> reason:\"" +  reason + "\" </b> Server try do that as soon as possible").toString();
    notification.save();

    send_notification(person, notification);

  }

  public static void uploud_of_Instance_was_unsuccesfull_with_error(Person person, Version_Object version_object){

    Notification notification = new Notification();
    notification.person = person;
    notification.level = Notification_level.success;
    notification.type = Notification_type.SINGLE_STRING_MESSAGE;
    notification.created = new Date();
    notification.confirmation_required = false;

    notification.content = Json.newObject().put("message", "Server not upload instance to cloud on Blocko Version" + version_object.version_name + "</b> from Blocko program <b>" + version_object.b_program.name + " with Critical unknown Error, Probably some bug").toString();
    notification.save();

    send_notification(person, notification);
  }





  public static void uploud_of_firmare_was_succesfull(Person person, C_Program_Update_Plan plan){

  }

  public static void uploud_of_firmare_was_Unsuccesfull(Person person, C_Program_Update_Plan plan){

  }

  public static void actualization_procedure_update(Person person, Actualization_procedure procedure){

  }


  public static void board_connect(Person person, Board board){

  }

  public static void board_disconnect(Person person, Board board){

  }


  // Public REST-API (Zdokumentované ve SWAGGER)  #######################################################################

  @ApiOperation(value = "get latest notification",
          tags = {"Notifications"},
          notes = "Get list of latest user notifications. Server return maximum 25 latest objects. \n\n " +
                  "For get another page (next 25 notifications) call this api with \"page_number\" path parameter. \n\n " +
                  "May missing or you can insert Integer values from page[1,2...,n] in Json",
          produces = "application/json",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Notification_List.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured.class)
  public Result get_history_log_page(@ApiParam(value = "page_number is Integer. Contain  1,2... " + " For first call, use 1", required = false) @PathParam("page_number") Integer page_number){
     try {

        Query<Notification> query =  Notification.find.where().eq("person.id", SecurityController.getPerson().id).order().desc("created");

        Swagger_Notification_List result = new Swagger_Notification_List(query, page_number);

        return GlobalResult.result_ok(Json.toJson(result));

     } catch (Exception e) {
      e.printStackTrace();
      return GlobalResult.internalServerError();
     }
  }



}
