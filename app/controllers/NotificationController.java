package controllers;

import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.notification.Notification;
import models.person.FloatingPersonToken;
import models.person.Person;
import play.Logger;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.notification.Notification_level;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.outboundClass.Filter_List.Swagger_Notification_List;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {

  //######################################################################################################################
  static play.Logger.ALogger logger = play.Logger.of("Loggy");
  private static Map<String, Map<String, EventSource>> connected_accounts = new HashMap<>(); // < person_id , < token , EventSource >
  //######################################################################################################################

  public static void send_notification(Person person, Notification_level level, String message) {

      Notification notification = new Notification();
      notification.person = person;
      notification.message = message;
      notification.level = level;
      notification.created = new Date();
      notification.confirmation_required = false;
      notification.save();

      if( connected_accounts.containsKey(person.id) &&  !connected_accounts.get(person.id).isEmpty() ) {

          for (FloatingPersonToken token : FloatingPersonToken.find.where().eq("person.id", person.id).where().eq("notification_subscriber", true).findList()) {

            CoreResponse.cors_EventSource();
            try {
              connected_accounts.get(person.id).get(token.authToken).send(EventSource.Event.event(Json.toJson(notification)));

            } catch (Exception e) {
              logger.error("Došlo k systémové chybě při odesílání EventSource");

              try {
                  if (connected_accounts.containsKey(person.id)) {
                    logger.error("connected_accounts stále obsahuje person.id");
                    if (connected_accounts.get(person.id).containsKey(token.authToken)) {
                      logger.error("connected_accounts nad person.id stále obsahuje token.authToken");
                      connected_accounts.get(person.id).get(token.authToken).close();
                      connected_accounts.get(person.id).remove(token.authToken);
                    }else{
                      logger.error("connected_accounts stále obsahuje person.id ale už neobsahuje token");
                    }
                  } else {
                    logger.error("connected_accounts neobsahuje person.id");
                  }
              } catch (Exception b) {

                logger.error("Došlo k systémové chybě nad systémovou chybou");
                b.printStackTrace();
              }
            }
          }
       }
  }

  public static void send_notification(List<Person> persons, Notification_level level, String message){
          for(Person person : persons) send_notification(person, level, message);
  }

  //######################################################################################################################

  @ApiOperation(value = "Subscribe notifications",
          tags = {"Notifications"},
          notes = "get EventSource for subscribing all notification. Its not possible document everything about this connection in Swagger, " +
                  "so you have to read more on our wiki https://wiki.byzance.cz/wiki/doku.php?id=notifikacni_centrum \n\n\n " +
                  "All incoming data cloud_blocko_server send in Json",
          produces = "text/event-stream",
          protocols = "https - Server-Sent Events",
          code = 200
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successful connected",    response = Result_ok.class),
          @ApiResponse(code = 401, message = "Unauthorized request - TOKEN IS NOT VALID",    response = Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  public Result subscribe_notification(@ApiParam(value = "token_value", required = true) @PathParam("token_value") String token_value ) {

    logger.debug("Incoming request for subscribing of notifications on token:" + token_value);


    FloatingPersonToken token = FloatingPersonToken.find.where().eq("authToken",token_value).findUnique();
    if(token == null) return GlobalResult.result_Unauthorized();

    // Token je používán, pravděpodobně došlo k obnovení okna prohlížeče a proto je nutné zahodit předchozí spojení,
    // které se bohužel umí samo odpojit až ve chvíli kdy mu cloud_blocko_server chce něco odeslat.
    if(connected_accounts.containsKey(token.person.id) && connected_accounts.get(token.person.id).containsKey(token_value) ) {
      connected_accounts.get(token.person.id).get(token_value).close();
      connected_accounts.get(token.person.id).remove(token_value);
    }

    token.notification_subscriber = true;
    token.update();

    CoreResponse.cors_EventSource();
      return ok(new EventSource() {

            @Override
            public void onConnected() {

              EventSource currentSocket = this;

              // Ze záhadného důvodu to nefuguje
              this.onDisconnected( () -> {
                if( connected_accounts.containsKey(token_value)) connected_accounts.remove(token_value);
                token.notification_subscriber = false;
                token.update();
                connected_accounts.remove(token_value);
              });

              // Na začátku musím vytvořit herarchii MAP
              if(connected_accounts.containsKey(token.person.id)){
                connected_accounts.get(token.person.id).put(token_value, currentSocket);
              } else {
               // pokud uživatel neodebírá zahládám první stupen vázaný na id user a podé druhý stupen na název tokenu a jeho hodnoty
                Map<String, EventSource> notification_sse_thread = new HashMap<>();
                notification_sse_thread.put(token_value, currentSocket);
                connected_accounts.put(token.person.id, notification_sse_thread );
              }
            }
      });
  }

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


  @ApiOperation(value = "remove Notification",
          tags = {"Notifications"},
          notes = "remove notification",
          produces = "application/json",
          protocols = "https",
          code = 200,
          authorizations = {
                  @Authorization(
                          value="permission",
                          scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                     @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                  )
          }
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response =  Result_ok.class),
          @ApiResponse(code = 400, message = "Objects not found ",      response =  Result_NotFound.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response =  Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured.class)
  public Result remove_notification(String notification_id){
    try{

      Notification notification = Notification.find.where().eq("id",notification_id).where().eq("person.id", SecurityController.getPerson().id).findUnique();

      if(notification == null && Notification.find.where().eq("id",notification_id).findUnique() != null ) return GlobalResult.forbidden_Permission();
      if(notification == null)                                                                             return GlobalResult.notFoundObject("Notification notification_id not found");

      notification.delete();
      return GlobalResult.result_ok();

    } catch (Exception e) {
      Logger.error("Error", e);
      return GlobalResult.internalServerError();
    }

  }

  @ApiOperation(value = "confirm Notification",
          tags = {"Notifications"},
          notes = "confirm notification",
          produces = "application/json",
          protocols = "https",
          code = 200,
          authorizations = {
                  @Authorization(
                          value="permission",
                          scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                     @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                  )
          }
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok Result",               response =  Result_ok.class),
          @ApiResponse(code = 400, message = "Objects not found ",      response =  Result_NotFound.class),
          @ApiResponse(code = 401, message = "Unauthorized request",    response =  Result_Unauthorized.class),
          @ApiResponse(code = 500, message = "Server side Error")
  })
  @Security.Authenticated(Secured.class)
  public Result confirm_Notification(String notification_id){
    try{

      Notification notification = Notification.find.where().eq("id",notification_id).where().eq("person.id", SecurityController.getPerson().id).findUnique();

      if(notification == null && Notification.find.where().eq("id",notification_id).findUnique() != null ) return GlobalResult.forbidden_Permission();
      if(notification == null)                                                                             return GlobalResult.notFoundObject("Notification notification_id not found");


      notification.confirmed = true;
      notification.update();

      return GlobalResult.result_ok();

    } catch (Exception e) {
      Logger.error("Error", e);
      return GlobalResult.internalServerError();
    }
  }





  //######################################################################################################################
  // TESTOVACÍ !!!!!!!!!!!!!!!!!!!
  //######################################################################################################################

  // Testovací notifikační metoda // TODO do budoucna určena ke smazání
  @ApiOperation(value = "Testovací metoda, která po zavolání odešle všem připojeným terminálům novou notifikaci", tags = {"Notifications"}, code = 200)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "successfully sent",  response = Result_ok.class)})
  public Result sendSomething(String level, String message) {

    send_notification(Person.find.where().eq("mail", "admin@byzance.cz").findUnique(), Notification_level.error, message);
    return ok();
  }

}
