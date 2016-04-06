package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.persons.FloatingPersonToken;
import models.persons.Person;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.notification.Notification_level;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;

import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {
  
  private static Map<String, EventSource > connected_accounts = new HashMap<>(); // < Token , EventSource >


  // Vize používání notifikátoru
  public static void sent_notification(List<Person> persons, Notification_level level, String message){

    for(Person person : persons)
    for(FloatingPersonToken token : FloatingPersonToken.find.where().eq("person.id", person.id).where().eq("notification_subscriber", true).findList() ){
      try{

         CoreResponse.cors();
         JsonNode msg = Json.newObject()
                            .put("level", level.name() )
                            .put("text", message);

         connected_accounts.get(token.authToken).send(EventSource.Event.event(msg));

       }catch (NullPointerException e){
         token.notification_subscriber = false;
         token.update();
       }


    }

  }


  @ApiOperation(value = "Subscribe notifications",
          tags = {"Notifications"},
          notes = "get EventSource for subscribing all notification. Its not possible document everything about this connection in Swagger, " +
                  "so you have to read more on our wiki https://wiki.byzance.cz/wiki/doku.php?id=notifikacni_centrum \n\n\n " +
                  "All incoming data server send in Json",
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

    FloatingPersonToken token = FloatingPersonToken.find.where().eq("authToken",token_value).findUnique();

    if(token == null) return GlobalResult.result_Unauthorized();


    // Token je používán, pravděpodobně došlo k obnovení okna prohlížeče a proto je nutné zahodit předchozí spojení,
    // které se bohužel umí samo odpojit až ve chvíli kdy mu server chce něco odeslat.
    if(connected_accounts.containsKey(token_value)) {
      connected_accounts.get(token_value).close();
      connected_accounts.remove(token_value);
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

              connected_accounts.put(token_value, currentSocket);
            }
      });


  }

  // Testovací notifikační metoda // TODO do budoucna určena ke smazání
  @ApiOperation(value = "Testovací metoda, která po zavolání odešle všem připojeným terminálům novou notifikaci", tags = {"Notifications"}, code = 200)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "successfully sent",  response = Result_ok.class)})
  public Result sendSomething(String level, String message){

    System.out.println("Počet spojení je: " + connected_accounts.size());
    CoreResponse.cors();

    JsonNode msg = Json.newObject()
            .put("level", level  )
            .put("text",  message);

    for (String key: connected_accounts.keySet()) connected_accounts.get(key).send(EventSource.Event.event(msg)  );

    CoreResponse.cors();
    return ok();
  }






}
