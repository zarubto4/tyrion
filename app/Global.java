import controllers.WebSocketController_Incoming;
import models.grid.Screen_Size_Type;
import models.persons.Person;
import models.persons.PersonPermission;
import models.persons.SecurityRole;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;

import java.lang.reflect.Method;


public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
       try {

           //1
           Logger.warn("Setting global values");
           Server.set_Server_address();

           //2
           Logger.warn("Setting main servers connections");
           Server.set_Blocko_Server_Connection();

           //3
           Logger.warn("Setting system Permission");
           Server.setPermission();

           //4
           Logger.warn("Setting Directory for Files");
           Server.setDirectory();

    /****************************************************************************************************************************/

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


           if( Screen_Size_Type.find.where().eq("name","iPhone6").findUnique() == null){

               Logger.warn("Creating screen size type for developers iPhone`s");
               Screen_Size_Type screen_size_type = new Screen_Size_Type();

               screen_size_type.name = "iPhone6";

               screen_size_type.landscape_height = 375;
               screen_size_type.landscape_width = 667;
               screen_size_type.landscape_square_height = 6;
               screen_size_type.landscape_square_width = 11;
               screen_size_type.landscape_max_screens = 10;
               screen_size_type.landscape_min_screens = 1;

               screen_size_type.portrait_height = 667;
               screen_size_type.portrait_width = 375;
               screen_size_type.portrait_square_height = 11;
               screen_size_type.portrait_square_width = 6;
               screen_size_type.portrait_max_screens = 10;
               screen_size_type.portrait_min_screens = 1;

               screen_size_type.height_lock  = true;
               screen_size_type.width_lock   = true;
               screen_size_type.touch_screen = true;

               screen_size_type.save();

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
        // TODO
       /* for (Map.Entry<String, WebSocketClientNotPlay> entry :  WebSocketController_Incoming.cloud_servers.entrySet())
        {
            entry.getValue().interrupt();
        }*/
    }


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);

    }


}

