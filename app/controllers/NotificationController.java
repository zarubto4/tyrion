package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import models.persons.FloatingPersonToken;
import models.persons.Person;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.notification.Notification_level;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;

import java.util.HashMap;
import java.util.Map;

@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationController extends Controller {
  
  private static Map<String, EventSource > connected_accounts = new HashMap<>(); // < Token , EventSource >

  // TODO SMAZAT
  public Result sendSomething(){

    System.out.println("Počet spojení je: " + connected_accounts.size());
    CoreResponse.cors();
    JsonNode msg = Json.newObject()
            .put("level", Notification_level.Success.toString() )
            .put("text", "blabla asdf sdfgkjgsdaflkj lkjagsfkj sdf")
            .put("who", "server")
            .put("time", DateTime.now().toString() );

    for (String key: connected_accounts.keySet()) {
      System.out.println("   Zprávu odesílám na " + key);
      connected_accounts.get(key).send(EventSource.Event.event(msg)  );
    }

    return ok();
  }



  // Vize používání notifikátoru
  public void sent_notification(Person person, Notification_level level, String message){

    for(FloatingPersonToken token : FloatingPersonToken.find.where().eq("person.id", person.id).where().eq("notification_subscriber", true).findList() ){

      try{

         JsonNode msg = Json.newObject()
                  .put("level", level.name() )
                  .put("text", "blabla")
                  .put("who", "server")
                  .put("time", DateTime.now().toString() );

         connected_accounts.get(token.authToken).send(EventSource.Event.event(msg));

       }catch (NullPointerException e){
         token.notification_subscriber = false;
         token.update();
       }


    }

  }


  // TODO - Token by nemusel být součástí žádosti o odběr notifikací, pokud se otestuje, že přijmu přihlášeného uživatele!!
  public Result subscribe_notification(String token_value) {

    System.out.println("Přihlásil se mi k odběru: " + token_value);


    System.out.println("Přihlásil se mi k odběru: " + token_value);
    System.out.println("Překontroluji jestli je token platný");

    FloatingPersonToken token = FloatingPersonToken.find.where().eq("authToken",token_value).findUnique();
    if(token == null) {
      System.out.println("Token nexistuje");
      return GlobalResult.forbidden_Global();
    }
    System.out.println("Token existuje");

    System.out.println("Překontroluji jestli už s tokenem neodebíráš notifikace");
    if(connected_accounts.containsKey(token_value)) {
      System.out.println("S tokenem už odebíráš notifikace hergot!");
      return GlobalResult.badRequest("Token is used for subscribing server side notification");
    }



    token.notification_subscriber = true;
    token.update();

    System.out.println("V pořádku a vracím EventSource!");

      return ok(new EventSource() {

      @Override
      public void onConnected() {

        EventSource currentSocket = this;

        // Ze záhadného důvodu to nefuguje
        this.onDisconnected( () -> {
          System.out.println("Odběratel notifikací se odhlásil a tak měním u tokenu jeho odběr na false! ");
          if( connected_accounts.containsKey(token_value)) connected_accounts.remove(token_value);
          token.notification_subscriber = false;
          token.update();
          connected_accounts.remove(token_value);
        });

        // Add to MAP
        if(connected_accounts.containsKey(token_value)) Logger.info(token_value + " - SSE už existuje");
        else connected_accounts.put(token_value, currentSocket );

      }


      }


      );


  }
}
