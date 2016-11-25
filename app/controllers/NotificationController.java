package controllers;

import com.avaje.ebean.Query;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.notification.Notification;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.instnace.Homer_Instance;
import models.project.c_program.C_Program;
import models.project.global.Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Notification_action;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.notifications.Notification_Handler;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_B_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_Notification_Confirm;
import utilities.swagger.documentationClass.Swagger_Notification_Read;
import utilities.swagger.documentationClass.Swagger_Notification_Test;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_Notification_Button;
import utilities.webSocket.WS_Becki_Website;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {

  @Inject
  ProgramingPackageController programingPackageController;

  //####################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");

  private static void send_notification(Person person, Notification notification) {

    // Pokud je notification_importance vyšší než "low" notifikaci uložím
    if((notification.notification_importance != Notification_importance.low)&&(notification.id == null))
      notification.save_object();

    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
    if (WebSocketController.becki_website.containsKey(person.id) ) {
      WebSocketController.becki_sendNotification( (WS_Becki_Website) WebSocketController.becki_website.get(person.id)  , notification );
    }
  }


  // Tvroba objektů jednotlivých notifikací ############################################################################


  // Hotovo
  public static void starting_of_compilation(Person person, Version_Object version_object){

        Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
                                     .setText("Server start with compilation on Version")
                                     .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name + ".", version_object.c_program.project_id());

        send_notification(person, notification);
  }

  public static void upload_firmware_progress(Person person, String version_object){
    // TODO a taky zařadit pod objekt
  }


  // Hotovo
  public static void successful_compilation(Person person, Version_Object version_object ){

      Notification notification = new Notification(Notification_importance.low, Notification_level.success, person)
                                    .setText("Compilation on Version")
                                    .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name, version_object.c_program.project_id() )
                                    .setText("was successful.");

      send_notification(person, notification);
  }

  // Hotovo
  public static void unsuccessful_compilation_warn(Person person, Version_Object version_object, String reason){

      Notification notification = new Notification(Notification_importance.normal,  Notification_level.warning , person)
                                      .setText("Compilation on Version")
                                      .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name, version_object.c_program.project_id() )
                                      .setText("was unsuccessful, for reason:")
                                      .setBoldText(reason);

      send_notification(person, notification);
  }

  // Hotovo
  public static void unsuccessful_compilation_error(Person person, Version_Object version_object, String result){

    Notification notification = new Notification(Notification_importance.normal, Notification_level.error, person)
                                      .setText( "Compilation on Version")
                                      .setObject(Swagger_B_Program_Version.class, version_object.id, version_object.version_name, version_object.c_program.project_id() )
                                      .setText("with critical Error:")
                                      .setBoldText(result);

    send_notification(person, notification);
  }

  // Hotovo
  public static void upload_Instance_start(Person person, Homer_Instance instance){

    Notification notification = new Notification(Notification_importance.low,  Notification_level.info, person)
                                  .setText("Server start creating new Blocko Instance on Blocko Version  <b>" + instance.actual_instance.version_object.b_program.name + "</b>")
                                  .setObject(Swagger_B_Program_Version.class, instance.actual_instance.version_object.id, instance.actual_instance.version_object.version_name, instance.actual_instance.version_object.b_program.project_id() )
                                  .setText("from Blocko program")
                                  .setObject(B_Program.class, instance.actual_instance.version_object.b_program.id, instance.actual_instance.version_object.b_program.name + ".", instance.actual_instance.version_object.b_program.project_id());

    send_notification(person, notification);
  }

  // Hotovo
  public static void upload_Instance_was_successful(Person person, Homer_Instance instance){

    Notification notification = new Notification(Notification_importance.low, Notification_level.success, person)
                                    .setText("Server created successfully instance in cloud on Blocko Version")
                                    .setObject(Swagger_B_Program_Version.class, instance.actual_instance.version_object.id, instance.actual_instance.version_object.version_name, instance.actual_instance.version_object.b_program.project_id() )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, instance.actual_instance.version_object.b_program.id, instance.actual_instance.version_object.b_program.name + ".", instance.actual_instance.version_object.b_program.project_id());

    send_notification(person, notification);
  }

  // Hotovo
  public static void upload_Instance_was_unsuccessful(Person person, Homer_Instance instance, String reason){


    Notification notification = new Notification(Notification_importance.normal, Notification_level.warning, person)
                                    .setText("Server not upload instance to cloud on Blocko Version <b>" + instance.actual_instance.version_object.version_name + "</b> from Blocko program <b>" + instance.b_program.name + "</b> for <b> reason:\"" +  reason + "\" </b> ")
                                    .setObject(Swagger_B_Program_Version.class, instance.actual_instance.version_object.id, instance.actual_instance.version_object.version_name, instance.b_program.project_id() )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, instance.b_program.id, instance.b_program.name, instance.b_program.project_id() )
                                    .setText("Server will try to do that as soon as possible.");

    send_notification(person, notification);

  }

  // TODO zařadit pod objekt
  public static void upload_of_Instance_was_unsuccessful_with_error(Person person, Version_Object version_object){

    Notification notification = new Notification(Notification_importance.normal, Notification_level.error, person)
                                    .setText("Server not upload instance to cloud on Blocko Version")
                                    .setObject(Swagger_B_Program_Version_New.class, version_object.id, version_object.version_name, version_object.b_program.project_id() )
                                    .setText("from Blocko program")
                                    .setObject(B_Program.class, version_object.b_program.id, version_object.b_program.name, version_object.b_program.project_id() )
                                    .setText("with Critical unknown Error, Probably some bug.");

    send_notification(person, notification);
  }

  // Hotovo
  public static void new_actualization_request_with_file(Person person, Board board){

    Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
            .setText("New actualization task was added to Task Queue on")
            .setObject(Board.class, board.id, "board", board.project_id())
            .setText("with user File ");


    send_notification(person, notification);

  }

  // Hotovo
  public static void new_actualization_request_on_version(Person person, Version_Object version_object){

    Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
            .setText("New actualization task was added to Task Queue on ")
            .setObject(Swagger_C_Program_Version.class, version_object.id, "Version " + version_object.version_name, version_object.c_program.project_id() )
            .setText("from Program ")
            .setObject(C_Program.class, version_object.c_program.id, "Program " + version_object.c_program.name, version_object.c_program.project_id() );


    send_notification(person, notification);

  }

  // Hotovo
  public static void new_actualization_request_homer_instance(Project project, Homer_Instance homer_instance){

    for(Person person : project.ownersOfProject) {
      Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
              .setText("New actualization task was added to Task Queue on ")
              .setObject(Swagger_B_Program_Version_New.class, homer_instance.actual_instance.version_object.id, "Version " + homer_instance.actual_instance.version_object.version_name, homer_instance.actual_instance.version_object.b_program.project_id());


      send_notification(person, notification);
    }

  }

  // Hotovo
  public static void board_connect(Person person, Board board){

      Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
              .setText("One of your Board " + (board.personal_description != null ? board.personal_description : null ))
              .setObject(Board.class, board.id, board.id, board.project_id())
              .setText("is connected.");

      send_notification(person, notification);

  }

  // Hotovo
  public static void board_disconnect(Person person, Board board){

    Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
            .setText("One of your Board " + (board.personal_description != null ? board.personal_description : null ))
            .setObject(Board.class, board.id, board.id, board.project_id())
            .setText("is disconnected.");

    send_notification(person, notification);

  }

  //Hotovo
  public static void project_invitation(Person owner, Person receiver, Project project, Invitation invitation){

    Notification notification = new Notification(Notification_importance.normal, Notification_level.info, receiver)
            .setText("User ")
            .setObject(Person.class, owner.id, owner.full_name, null)
            .setText("wants to invite you into the project ")
            .setObject(Project.class, project.id, project.name, project.id)
            .setText(".")
            .setText("Do you agree?")
            .setLink("Yes", Server.tyrion_serverAddress + "/project/project/addParticipant/" + invitation.id + "/true")
            .setText(" / ")
            .setLink("No", Server.tyrion_serverAddress + "/project/project/addParticipant/" + invitation.id + "/false")
            .setText(".");

    invitation.notification_id = notification.id;
    invitation.update();

    send_notification(receiver, notification);

  }

  //Hotovo
  public static void project_accepted_by_invited_person(Person owner, Person person, Project project){

    Notification notification = new Notification(Notification_importance.normal, Notification_level.info, owner)
            .setText("User ")
            .setObject(Person.class, person.id, person.full_name, "")
            .setText("did not accept your invitation to the project ")
            .setObject(Project.class, project.id, project.name, project.id)
            .setText(".");

    send_notification(owner,notification);

  }

  //Hotovo
  public static void project_rejected_by_invited_person(Person owner, Person person, Project project){

    Notification notification = new Notification(Notification_importance.normal, Notification_level.info, owner)
            .setText("User ")
            .setObject(Person.class, person.id, person.full_name, "")
            .setText("accepted your invitation to the project ")
            .setObject(Project.class, project.id, project.name, project.id)
            .setText(".");

    send_notification(owner,notification);

  }

  public static void test_notification(Person person, String level, String importance, String type, String buttons){

    Notification_level lvl;

    Notification_importance imp;

    switch (importance){
      case "low": imp = Notification_importance.low; break;
      case "normal": imp = Notification_importance.normal; break;
      case "high": imp = Notification_importance.high; break;
      default: imp = Notification_importance.normal; break;
    }

    switch (level){
      case "info": lvl = Notification_level.info;break;
      case "success": lvl = Notification_level.success;break;
      case "warning": lvl = Notification_level.warning;break;
      case "error": lvl = Notification_level.error;break;
      default: lvl = Notification_level.info;break;
    }

    Notification notification;

    switch (type){
      case "1":{
        notification = new Notification(imp, lvl, person)
              .setText("Test object: ")
              .setObject(Person.class, person.id, person.full_name, null)
              .setText(" test bold text: ")
              .setBoldText("bold text ")
              .setText("test link: ")
              .setLink("TestLink","#");
        break;}
      case "2":{
        notification = new Notification(imp, lvl, person)
                .setText("Test object and long text: ")
                .setObject(Person.class, person.id, person.full_name, null)
                .setText(" test text: Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. ");
        break;}
      case "3":{
        notification = new Notification(imp, lvl, person)
                .setText("Test short text with link: ")
                .setLink("TestLink","#");
        break;}
      case "4": {
        notification = new Notification(imp, lvl, person)
                .setText("Test object and link: ")
                .setObject(Person.class, person.id, person.full_name, null)
                .setText(" test link: ")
                .setLink("Yes","#");
        break;}
      default:{
        notification = new Notification(imp, lvl, person)
                .setText("Test object: ")
                .setObject(Person.class, person.id, person.full_name, null)
                .setText(" test bold text: ")
                .setBoldText("bold text ")
                .setText("test link: ")
                .setLink("TestLink","#");
        break;}
    }
    switch (buttons){
      case "0": break;
      case "1":{
        notification.setButton(Notification_action.confirm_notification, "test", "blue", "OK", false, false, false);
        break;}
      case "2":{
        notification.setButton(Notification_action.accept_project_invitation, "test", "green", "Yes", false, false, true);
        notification.setButton(Notification_action.reject_project_invitation, "test", "red", "No", false, false, true);
        break;}
      case "3":{
        notification.setButton(Notification_action.accept_project_invitation, "test", "green", "Yes", true, false, false);
        notification.setButton(Notification_action.reject_project_invitation, "test", "red", "No", true, false, false);
        notification.setButton(Notification_action.confirm_notification, "test", "white", "Close", false, true, false);
        break;}
      default:{
        notification.setButton(Notification_action.accept_project_invitation, "test", "green", "Yes", false, false, false);
        break;}
    }

    notification.send(person);
  }










    // Public REST-API (Zdokumentované ve SWAGGER)  #######################################################################

  @ApiOperation(value = "get latest notification",
          tags = {"Notifications"},
          notes = "Get list of latest user notifications. Server return maximum 25 latest objects. \n\n " +
                  "For get another page (next 25 notifications) call this api with \"page_number\" path parameter. \n\n " +
                  "May missing or you can insert Integer values from page[1,2...,n] in Json" +
                  "Notification body cannot by documented through swagger. Visit wiki.byzance.cz",
          produces = "application/json",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Notification_List.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured_API.class)
  public Result get_notification_page(@ApiParam(value = "page_number is Integer. Contain  1,2... " + " For first call, use 1", required = false) Integer page_number){
     try {

        Query<Notification> query =  Notification.find.where().eq("person.id", SecurityController.getPerson().id).order().desc("created");

        Swagger_Notification_List result = new Swagger_Notification_List(query, page_number);

        return GlobalResult.result_ok(Json.toJson(result));

     } catch (Exception e) {
       e.printStackTrace();
       return Loggy.result_internalServerError(e, request());
     }
  }



  @ApiOperation(value = "delete notification",
          tags = {"Notifications"},
          notes = "remove notification by id",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Delete Successful",       response = Result_ok.class),
          @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured_API.class)
  public Result delete_notification(@ApiParam(value = "notification_id String path", required = true) String notification_id){
    try {

      Notification notification = Notification.find.byId(notification_id);
      if (notification == null) return GlobalResult.notFoundObject("Notification does not exist");

      if( !notification.delete_permission()) return GlobalResult.forbidden_Permission();

      notification.delete();
      return GlobalResult.result_ok();

    } catch (Exception e) {
      return Loggy.result_internalServerError(e, request());
    }
  }

  @ApiOperation(value = "mark notifications as read",
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
                          dataType = "utilities.swagger.documentationClass.Swagger_Notification_Read",
                          required = true,
                          paramType = "body",
                          value = "Contains Json with values"
                  )
          }
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successfully marked as read", response = Result_ok.class),
          @ApiResponse(code = 400, message = "Some Json value Missing",     response = Result_JsonValueMissing.class),
          @ApiResponse(code = 401, message = "Unauthorized request",        response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured_API.class)
  public Result mark_as_read_notification(){
    try {

      final Form<Swagger_Notification_Read> form = Form.form(Swagger_Notification_Read.class).bindFromRequest();
      if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
      Swagger_Notification_Read help = form.get();

      List<Notification> notifications = Notification.find.where().idIn(help.notification_id).findList();

      for(Notification notification : notifications) {

        notification.set_read();
      }

      return GlobalResult.result_ok();

    } catch (Exception e) {
      return Loggy.result_internalServerError(e, request());
    }
  }

  @ApiOperation(value = "get unconfirmed notifications",
          tags = {"Notifications"},
          notes = "This API should by called right after user logs in. Sends notifications which require confirmation via websocket.",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured_API.class)
  public Result get_unconfirmed_notifications(){
    try{
      List<Notification> notifications = Notification.find.where().eq("person.id", SecurityController.getPerson().id).eq("notification_importance", Notification_importance.high).eq("confirmed", false).findList();
      if(notifications.isEmpty()) return GlobalResult.result_ok("No new notifications");

      for (Notification notification : notifications){
          notification.send(SecurityController.getPerson());
      }

      return GlobalResult.result_ok("Notifications were sent again");

    }catch (Exception e){
      return Loggy.result_internalServerError(e, request());
    }
  }

  public Result test_notifications(String mail){
    try {

      final Form<Swagger_Notification_Test> form = Form.form(Swagger_Notification_Test.class).bindFromRequest();
      if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
      Swagger_Notification_Test help = form.get();

      Person person = Person.find.where().eq("mail", mail).findUnique();
      if (person == null) return GlobalResult.notFoundObject("Person not found");

      NotificationController.test_notification(person, help.level, help.importance, help.type, help.buttons);
      return GlobalResult.result_ok();

    }catch (Exception e){
      return Loggy.result_internalServerError(e, request());
    }
  }

  @ApiOperation(value = "confirm notification",
          tags = {"Notifications"},
          notes = "Confirms notification",
          produces = "application/json",
          consumes = "text/html",
          protocols = "https",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
          @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
          @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
          @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
          @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured_API.class)
  public Result notification_confirm(@ApiParam(value = "notification_id String path", required = true) String notification_id){

    final Form<Swagger_Notification_Confirm> form = Form.form(Swagger_Notification_Confirm.class).bindFromRequest();
    if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
    Swagger_Notification_Confirm help = form.get();

    try{
      Notification notification = Notification.find.byId(notification_id);
      if(notification == null) return GlobalResult.notFoundObject("Notification does not exist");

      if (!notification.confirm_permission()) return GlobalResult.forbidden_Permission();

      switch (help.action){
        case "confirm_notification"       : {
          notification.confirm();
          return GlobalResult.result_ok("Notification confirmed");
        }
        case "accept_project_invitation"  : {
          return programingPackageController.addParticipantToProject(help.payload, true);
        }
        case "reject_project_invitation"  : {
          return programingPackageController.addParticipantToProject(help.payload, false);
        }
        default: return GlobalResult.result_BadRequest("Unknown action");
      }

    }catch (Exception e){
      return Loggy.result_internalServerError(e, request());
    }
  }

}
