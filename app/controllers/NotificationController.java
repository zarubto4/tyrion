package controllers;

import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.notification.Notification;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.Homer_Instance;
import models.project.c_program.C_Program;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.global.Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.outboundClass.Filter_List.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.webSocket.WS_Becki_Website;

import javax.websocket.server.PathParam;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {

  //####################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public enum Notification_level {
      info,
      success,
      warning,
      error,
      question
    }


  private static void send_notification(Person person, Notification notification) {

    if (WebSocketController_Incoming.becki_website.containsKey(person.id) ) {
      WebSocketController_Incoming.becki_sendNotification( (WS_Becki_Website) WebSocketController_Incoming.becki_website.get(person.id)  , notification );
    }
  }


  // Tvroba objektů jednotlivých notifikací ############################################################################


  public static void starting_of_compilation(Person person, Version_Object version_object){

        Notification notification = new Notification(Notification_level.info, person)
                                     .setText("Server start with compilation on Version")
                                     .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name + ".");

        send_notification(person, notification);
  }

  public static void successful_compilation(Person person, Version_Object version_object ){

      Notification notification = new Notification(Notification_level.success, person)
                                    .setText("Compilation on Version")
                                    .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name )
                                    .setText("was successful.");

      send_notification(person, notification);
  }

  public static void unsuccessful_compilation_warn(Person person, Version_Object version_object, String reason){

      Notification notification = new Notification( Notification_level.warning , person)
                                      .setText("Compilation on Version")
                                      .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name )
                                      .setText("was unsuccessful, for reason:")
                                      .setBoldText(reason);

      send_notification(person, notification);
  }

  public static void unsuccessful_compilation_error(Person person, Version_Object version_object, String result){

    Notification notification = new Notification(Notification_level.error, person)
                                      .setText( "Compilation on Version")
                                      .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name )
                                      .setText("with critical Error:")
                                      .setBoldText(result)
                                      .setPerson(person)
                                      .save_object();

    send_notification(person, notification);
  }

  public static void upload_Instance_start(Person person, Homer_Instance instance){

    Notification notification = new Notification( Notification_level.info, person)
                                  .setText("Server start creating new Blocko Instance on Blocko Version  <b>" + instance.version_object.b_program.name + "</b>")
                                  .setObject(Swagger_B_Program_Version.class, instance.version_object.id, instance.version_object.version_name )
                                  .setText("from Blocko program")
                                  .setObject(B_Program.class, instance.version_object.b_program.id, instance.version_object.b_program.name + ".");

    send_notification(person, notification);
  }

  public static void upload_Instance_was_successful(Person person, Homer_Instance instance){

    Notification notification = new Notification(Notification_level.success, person)
                                    .setText("Server created successfully instance in cloud on Blocko Version")
                                    .setObject(Swagger_B_Program_Version.class, instance.version_object.id, instance.version_object.version_name )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, instance.version_object.b_program.id, instance.version_object.b_program.name + ".");

    send_notification(person, notification);
  }

  public static void unload_Instance_was_unsuccessfull(Person person, Homer_Instance instance, String reason){

    Notification notification = new Notification( Notification_level.warning, person)
                                    .setText("Server not upload instance to cloud on Blocko Version <b>" + instance.version_object.version_name + "</b> from Blocko program <b>" + instance.version_object.b_program.name + "</b> for <b> reason:\"" +  reason + "\" </b> ")
                                    .setObject(Swagger_B_Program_Version.class, instance.version_object.id, instance.version_object.version_name )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, instance.version_object.b_program.id, instance.version_object.b_program.name )
                                    .setText("Server will try to do that as soon as possible.")
                                    .setPerson(person)
                                    .save_object();

    send_notification(person, notification);

  }

  public static void unload_of_Instance_was_unsuccessfull_with_error(Person person, Version_Object version_object){

    Notification notification = new Notification(Notification_level.error, person)
                                    .setText("Server not upload instance to cloud on Blocko Version")
                                    .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, version_object.b_program.id, version_object.b_program.name )
                                    .setText("with Critical unknown Error, Probably some bug.")
                                    .setPerson(person)
                                    .save_object();

    send_notification(person, notification);
  }

  public static void new_actualization_request_with_file(Person person, Board board, String file_name){

    Notification notification = new Notification(Notification_level.info, person)
            .setText("New actualization task was added to Task Queue on")
            .setObject(Board.class, board.id, "board")
            .setText("with File " + file_name);


    send_notification(person, notification);

  }

  public static void new_actualization_request_on_version(Person person, Version_Object version_object){

    Notification notification = new Notification(Notification_level.info, person)
            .setText("New actualization task was added to Task Queue on ")
            .setObject(Swagger_C_Program_Version.class, version_object.id, "Version " + version_object.version_name )
            .setText("from Program ")
            .setObject(C_Program.class, version_object.c_program.id, "Program " + version_object.c_program.program_name );


    send_notification(person, notification);

  }

  public static void new_actualization_request_homer_instance(Person person, Homer_Instance homer_instance){

    Notification notification = new Notification(Notification_level.info, person)
            .setText("New actualization task was added to Task Queue on ")
            .setObject(Swagger_B_Program_Version.class, homer_instance.version_object.id, "Version " + homer_instance.version_object.version_name );


    send_notification(person, notification);

  }

  public static void upload_firmware_was_successful(Person person, C_Program_Update_Plan plan){

  }

  public static void uplood_firmware_was_Unsuccessful(Person person, C_Program_Update_Plan plan){

  }

  public static void actualization_procedure_update(Person person, Actualization_procedure procedure){

  }


  public static void board_connect(Person person, Board board){

      Notification notification = new Notification(Notification_level.info, person)
              .setText("One of your Board " + (board.personal_description != null ? board.personal_description : null ))
              .setObject(Board.class, board.id, board.id)
              .setText("connected");

      send_notification(person, notification);

  }

  public static void board_disconnect(Person person, Board board){

    Notification notification = new Notification(Notification_level.info, person)
            .setText("One of your Board " + (board.personal_description != null ? board.personal_description : null ))
            .setObject(Board.class, board.id, board.id)
            .setText("disconnect");

    send_notification(person, notification);

  }


  public static void project_invitation(Person owner, Person person, Project project, Invitation invitation){

    Notification notification = new Notification(Notification_level.info, person)
            .setText("User")
            .setObject(Person.class, owner.id, owner.full_name)
            .setText("wants to invite you into the project ")
            .setBoldText(project.project_name +".")
            .setText("Do you agree?")
            .setLink_ToTyrion("Yes", Server.tyrion_serverAddress + "/project/project/addParticipant/" + invitation.id + "/true")
            .setText(" / ")
            .setLink_ToTyrion("No", Server.tyrion_serverAddress + "/project/project/addParticipant/" + invitation.id + "/false")
            .setText(".")
            .setPerson(person)
            .save_object();

    send_notification(person, notification);

  }


  public static void project_accepted_by_invited_person(Person owner, Person person, Project project){

    Notification notification = new Notification(Notification_level.info, owner)
            .setText("User ")
            .setObject(Person.class, person.id, person.full_name)
            .setText("did not accept your invitation to the project ")
            .setBoldText(project.project_name +".")
            .setPerson(owner)
            .save_object();

    send_notification(owner,notification);

  }

  public static void project_rejected_by_invited_person(Person owner, Person person, Project project){

    Notification notification = new Notification(Notification_level.info, owner)
            .setText("User ")
            .setObject(Person.class, person.id, person.full_name)
            .setText("accepted your invitation to the project ")
            .setBoldText(project.project_name +".")
            .setPerson(owner)
            .save_object();

    send_notification(owner,notification);

  }

  public Result notification_confirm(String notification_id){

    try{
      Notification notification = Notification.find.byId(notification_id);

      notification.confirmed = true;
      notification.update();

      return GlobalResult.result_ok();

    }catch (Exception e){
      return GlobalResult.internalServerError();
    }


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

  @ApiOperation(value = "get unconfirmed notifications",
          tags = {"Notifications"},
          notes = "This API should by called right after user logs in. Sends notifications which require confirmation",
          produces = "application/json",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured.class)
  public Result get_unconfirmed_notifications(){
    try{
      List<Notification> notifications = Notification.find.where().eq("person", SecurityController.getPerson()).eq("confirmation_required", true).eq("confirmed", false).findList();
      if(notifications.isEmpty()) return GlobalResult.result_ok("No new notifications");

      for (Notification notification : notifications){
          NotificationController.send_notification(SecurityController.getPerson(), notification);
      }

      return GlobalResult.result_ok("Notifications were sent again");

    }catch (Exception e){
      return GlobalResult.internalServerError();
    }
  }
}
