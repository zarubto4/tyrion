import controllers.WebSocketController_Incoming;
import controllers.WebSocketController_OutComing;
import models.persons.Person;
import models.persons.PersonPermission;
import models.persons.SecurityRole;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.webSocket.WebSocketClientNotPlay;

import java.lang.reflect.Method;
import java.util.Map;


public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
       try {

           //1
           Logger.warn("Setting global values");
           Server.set_Server();

           //2
           Logger.warn("Setting main servers connections");
           Server.set_Blocko_Server_Connection();

           //3
           Logger.warn("Setting system Permission");
           Server.setPermission();



    //****************************************************************************************************************************

           // For Developing
           if(SecurityRole.findByName("SuperAdmin") == null){
               Logger.warn("Creating first system superAdmin Role");
                SecurityRole role = new SecurityRole();
                role.permissions.addAll(PersonPermission.find.all());
                role.name = "SuperAdmin";
                role.save();
           }

           if (Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null)
           {
               Logger.warn("Creating first admin account: admin@byzance.cz, password: 123456789");
               Person person = new Person();
               person.first_name = "Admin";
               person.last_name = "Byzance";
               person.mailValidated = true;
               person.mail = "admin@byzance.cz";
               person.setSha("123456789");
               person.roles.add(SecurityRole.findByName("SuperAdmin"));

               person.save();
           }


       }catch (Exception e){
         e.printStackTrace();
       }

    }


    @Override
    public void onStop(Application app){

        Logger.warn("Restartuji server!");

        Logger.warn("Odpojuji připojené Homery!");
        WebSocketController_Incoming.disconnect_all_homers();

        Logger.warn("Odpojuji připojené mobilní zařízení!");
        WebSocketController_Incoming.disconnect_all_mobiles();


        Logger.warn("Odpojuji připojené servery Blocko!");
        for (Map.Entry<String, WebSocketClientNotPlay> entry :  WebSocketController_OutComing.servers.entrySet())
        {
            entry.getValue().interrupt();
        }
    }


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);

    }


}

