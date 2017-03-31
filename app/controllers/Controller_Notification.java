package controllers;

import com.avaje.ebean.Query;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.Model_Board;
import models.Model_VersionObject;
import models.Model_Notification;
import models.Model_Person;
import models.Model_BProgram;
import models.Model_CProgram;
import models.Model_Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.enums.Enum_Notification_level;
import utilities.enums.Enum_Notification_state;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Link;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_Notification_Confirm;
import utilities.swagger.documentationClass.Swagger_Notification_Read;
import utilities.swagger.documentationClass.Swagger_Notification_Test;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;

import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Notification extends Controller {

  @Inject Controller_Project controllerProgramingPackage;

  //####################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");


  // Tvroba objektů jednotlivých notifikací ############################################################################

  public static void test_notification(Model_Person person, String level, String importance, String type, String buttons){

    Enum_Notification_level lvl;

    Enum_Notification_importance imp;

    switch (importance){
      case "low": imp = Enum_Notification_importance.low; break;
      case "normal": imp = Enum_Notification_importance.normal; break;
      case "high": imp = Enum_Notification_importance.high; break;
      default: imp = Enum_Notification_importance.normal; break;
    }

    switch (level){
      case "info": lvl = Enum_Notification_level.info;break;
      case "success": lvl = Enum_Notification_level.success;break;
      case "warning": lvl = Enum_Notification_level.warning;break;
      case "error": lvl = Enum_Notification_level.error;break;
      default: lvl = Enum_Notification_level.info;break;
    }

    Model_Notification notification;

    switch (type){
      case "1":{
        notification = new Model_Notification()
                .setImportance(imp)
                .setLevel(lvl)
                .setText( new Notification_Text().setText("Test object: "))
                .setObject(person)
                .setText(new Notification_Text().setText(" test text, "))
                .setText(new Notification_Text().setText(" test bold text, ").setBoltText())
                .setText(new Notification_Text().setText(" test italic text, ").setItalicText())
                .setText(new Notification_Text().setText(" test underline text, ").setUnderlineText())
                .setText(new Notification_Text().setText(" test red color text, ").setColor(Becki_color.byzance_red))
                .setLink(new Notification_Link().setUrl("Text linku na google ", "http://google.com"));

        Model_Project project = Model_Project.find.where().eq("participants.person.id", person.id).eq("name", "První velkolepý projekt").findUnique();
        if (project != null) {
          notification.setObject(project);

          if (!project.boards.isEmpty()){
            Model_Board board = project.boards.get(0);
            notification.setObject(board);
          }

          Model_CProgram cProgram;
          if (!project.c_programs.isEmpty()){

            cProgram = project.c_programs.get(0);

          } else {

            cProgram = new Model_CProgram();
            cProgram.name                  = "Test notification c program";
            cProgram.description           = "random text sd asds dasda ";
            cProgram.date_of_create        = new Date();
            cProgram.project               = project;
            cProgram.save();
            cProgram.refresh();

            logger.info("Setting new C Program");
          }

          notification.setObject(cProgram);

          Model_VersionObject version_object;
          if (cProgram.getVersion_objects().isEmpty()){

            version_object = new Model_VersionObject();
            version_object.version_name        = "Test notification c version";
            version_object.version_description = "random text sd asds dasda";
            version_object.author              = person;
            version_object.date_of_create      = new Date();
            version_object.c_program           = cProgram;
            version_object.public_version      = false;
            version_object.save();
            version_object.refresh();

            logger.info("Setting new C Program Version");

          } else {
            version_object = cProgram.getVersion_objects().get(0);
          }

          notification.setObject(version_object);

          Model_BProgram bProgram;
          if (!project.b_programs.isEmpty()){
            bProgram = project.b_programs.get(0);
          } else {

            bProgram = new Model_BProgram();
            bProgram.name                  = "Test notification b program";
            bProgram.description           = "random text sd asds dasda ";
            bProgram.date_of_create        = new Date();
            bProgram.project = project;
            bProgram.save();
            bProgram.refresh();

            logger.info("Setting new B Program");
          }

          notification.setObject(bProgram);

          Model_VersionObject b_version_object;
          if (bProgram.getVersion_objects().isEmpty()){

            b_version_object = new Model_VersionObject();
            b_version_object.version_name        = "Test notification b version";
            b_version_object.version_description = "random text sd asds dasda";
            b_version_object.author              = person;
            b_version_object.date_of_create      = new Date();
            b_version_object.b_program           = bProgram;
            b_version_object.save();
            b_version_object.refresh();

            logger.info("Setting new B Program Version");

          } else {
            b_version_object = bProgram.getVersion_objects().get(0);
          }

          notification.setObject(b_version_object);
        }



        break;}
      case "2":{
        notification = new Model_Notification()
                .setImportance(imp)
                .setLevel(lvl)
                .setText(new Notification_Text().setText("Test object and long text: "))
                .setObject(person)
                .setText(new Notification_Text().setText(" test text: Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "));
        break;}
      case "3":{
        notification = new Model_Notification()
                .setImportance(imp)
                .setLevel(lvl)
                .setText(new Notification_Text().setText("Test short text with link: "))
                .setLink(new Notification_Link().setUrl("Text linku na google ", "http://google.com"));
        break;}
      case "4": {
        notification = new Model_Notification()
                .setImportance(imp)
                .setLevel(lvl)
                .setText( new Notification_Text().setText("Test object and link: "))
                .setObject(person)
                .setText( new Notification_Text().setText(" test link: "))
                .setLink(new Notification_Link().setUrl("Yes", "http://google.com"));
        break;}
      default:{
        notification = new Model_Notification()
                .setImportance(imp)
                .setLevel(lvl)
                .setText( new Notification_Text().setText("Test object: "))
                .setObject(person)
                .setText( new Notification_Text().setText(" test bold text: ").setBoltText())
                .setText( new Notification_Text().setText( "test link: "))
                .setLink( new Notification_Link().setUrl("Yes", "http://google.com"));
        break;}
    }
    switch (buttons){
      case "0": break;
      case "1":{
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("OK") );
        break;}
      case "2":{
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_green).setText("YES") );
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_red).setText("NO").setUnderLine() );
        break;}
      case "3":{
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.white).setText("NO").setBold() );
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_green).setText("NO").setItalic() );
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
        break;}
      default:{
        notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
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
        notification.state = Enum_Notification_state.updated;
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
      List<Model_Notification> notifications = Model_Notification.find.where().eq("person.id", Controller_Security.getPerson().id).eq("notification_importance", Enum_Notification_importance.high).eq("confirmed", false).findList();
      if(notifications.isEmpty()) return GlobalResult.result_ok("No new notifications");

      for (Model_Notification notification : notifications){
          notification.state = Enum_Notification_state.unconfirmed;
          notification.send();
      }

      return GlobalResult.result_ok("Notifications were sent again");

    }catch (Exception e){
      return Loggy.result_internalServerError(e, request());
    }
  }

  public Result test_notifications(){
    try {

      final Form<Swagger_Notification_Test> form = Form.form(Swagger_Notification_Test.class).bindFromRequest();
      if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
      Swagger_Notification_Test help = form.get();

      Model_Person person = Model_Person.find.where().eq("mail", help.mail).findUnique();
      if (person == null) return GlobalResult.notFoundObject("Person not found");

      test_notification(person, help.level, help.importance, help.type, help.buttons);
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
