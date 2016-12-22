package controllers;

import com.avaje.ebean.Query;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.compiler.Model_VersionObject;
import models.notification.Model_Notification;
import models.person.Model_Person;
import models.project.b_program.Model_BProgram;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Notification_action;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.enums.Notification_state;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_B_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_Notification_Confirm;
import utilities.swagger.documentationClass.Swagger_Notification_Read;
import utilities.swagger.documentationClass.Swagger_Notification_Test;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Notification extends Controller {

  @Inject
  Controller_ProgramingPackage controllerProgramingPackage;

  //####################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");

  // Tvroba objektů jednotlivých notifikací ############################################################################

  public static void upload_firmware_progress(Model_Person person, String version_object){
    // TODO a taky zařadit pod objekt
  }

  // TODO zařadit pod objekt
  public static void upload_of_Instance_was_unsuccessful_with_error(Model_Person person, Model_VersionObject version_object){

    Model_Notification notification = new Model_Notification(Notification_importance.normal, Notification_level.error, person)
                                    .setText("Server not upload instance to cloud on Blocko Version")
                                    .setObject(Swagger_B_Program_Version_New.class, version_object.id, version_object.version_name, version_object.b_program.project_id() )
                                    .setText("from Blocko program")
                                    .setObject(Model_BProgram.class, version_object.b_program.id, version_object.b_program.name, version_object.b_program.project_id() )
                                    .setText("with Critical unknown Error, Probably some bug.");

    //send_notification(person, notification);
  }

  public static void test_notification(Model_Person person, String level, String importance, String type, String buttons){

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

    Model_Notification notification;

    switch (type){
      case "1":{
        notification = new Model_Notification(imp, lvl)
              .setText("Test object: ")
              .setObject(Model_Person.class, person.id, person.full_name, null)
              .setText(" test bold text: ")
              .setBoldText("bold text ")
              .setText("test link: ")
              .setLink("TestLink","#");
        break;}
      case "2":{
        notification = new Model_Notification(imp, lvl)
                .setText("Test object and long text: ")
                .setObject(Model_Person.class, person.id, person.full_name, null)
                .setText(" test text: Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. ");
        break;}
      case "3":{
        notification = new Model_Notification(imp, lvl)
                .setText("Test short text with link: ")
                .setLink("TestLink","#");
        break;}
      case "4": {
        notification = new Model_Notification(imp, lvl)
                .setText("Test object and link: ")
                .setObject(Model_Person.class, person.id, person.full_name, null)
                .setText(" test link: ")
                .setLink("Yes","#");
        break;}
      default:{
        notification = new Model_Notification(imp, lvl)
                .setText("Test object: ")
                .setObject(Model_Person.class, person.id, person.full_name, null)
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
        notification.setButton(Notification_action.confirm_notification, "test", "green", "Yes", false, false, true);
        notification.setButton(Notification_action.confirm_notification, "test", "red", "No", false, false, true);
        break;}
      case "3":{
        notification.setButton(Notification_action.confirm_notification, "test", "green", "Yes", true, false, false);
        notification.setButton(Notification_action.confirm_notification, "test", "red", "No", true, false, false);
        notification.setButton(Notification_action.confirm_notification, "test", "white", "Close", false, true, false);
        break;}
      default:{
        notification.setButton(Notification_action.confirm_notification, "test", "green", "Yes", false, false, false);
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

        Query<Model_Notification> query =  Model_Notification.find.where().eq("person.id", Controller_Security.getPerson().id).order().desc("created");

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

      Model_Notification notification = Model_Notification.find.byId(notification_id);
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

      List<Model_Notification> notifications = Model_Notification.find.where().idIn(help.notification_id).findList();

      for(Model_Notification notification : notifications) {

        notification.set_read();
        notification.state = Notification_state.updated;
        notification.send();
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
      List<Model_Notification> notifications = Model_Notification.find.where().eq("person.id", Controller_Security.getPerson().id).eq("notification_importance", Notification_importance.high).eq("confirmed", false).findList();
      if(notifications.isEmpty()) return GlobalResult.result_ok("No new notifications");

      for (Model_Notification notification : notifications){
          notification.state = Notification_state.unconfirmed;
          notification.send();
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

      Model_Person person = Model_Person.find.where().eq("mail", mail).findUnique();
      if (person == null) return GlobalResult.notFoundObject("Person not found");

      Controller_Notification.test_notification(person, help.level, help.importance, help.type, help.buttons);
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
  @ApiImplicitParams(
          {
                  @ApiImplicitParam(
                          name = "body",
                          dataType = "utilities.swagger.documentationClass.Swagger_Notification_Confirm",
                          required = true,
                          paramType = "body",
                          value = "Contains Json with values"
                  )
          }
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
      Model_Notification notification = Model_Notification.find.byId(notification_id);
      if(notification == null) return GlobalResult.notFoundObject("Notification no longer exists");

      if (!notification.confirm_permission()) return GlobalResult.forbidden_Permission();

      if (notification.confirmed) return GlobalResult.result_BadRequest("Notification is already confirmed");

      switch (help.action){
        case "confirm_notification"       : {
          notification.confirm();
          return GlobalResult.result_ok("Notification confirmed");
        }
        case "accept_project_invitation"  : {
          return controllerProgramingPackage.project_addParticipant(help.payload, true);
        }
        case "reject_project_invitation"  : {
          return controllerProgramingPackage.project_addParticipant(help.payload, false);
        }
        default: return GlobalResult.result_BadRequest("Unknown action");
      }

    }catch (Exception e){
      return Loggy.result_internalServerError(e, request());
    }
  }

}
