import models.persons.Person;
import models.persons.PersonPermission;
import models.persons.SecurityRole;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import utilities.a_main_utils.GlobalValue;
import utilities.webSocket.ClientThreadChecker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class Global extends GlobalSettings {

    List<ClientThreadChecker> blockoServers = new ArrayList<>();

    @Override
    public void onStart(Application app) {
       try {

           //1
           Logger.warn("Setting global values");
           GlobalValue.onStart();

           //2
           Logger.warn("Setting main servers connections");
           if (Configuration.root().getBoolean("Servers.blocko.server1.run")) {

               Logger.warn("Starting Main Thread for Blocko Server1 ");

               ClientThreadChecker clientThreadChecker = new ClientThreadChecker()
                       .setIDentificator(Configuration.root().getString("Servers.blocko.server1.name"))
                       .setPeriodReconnectionTime(Configuration.root().getInt("Servers.blocko.server1.periodicTime"))
                       .setReconnection(true)
                       .setServerAddress(Configuration.root().getString("Servers.blocko.server1.url"))
                       .connectToServer();
           }



    //****************************************************************************************************************************

           // For Developing
           // 3


           if(SecurityRole.findByName("SuperAdmin") == null){
               Logger.warn("Creating first system superAdmin Role");
                SecurityRole role = new SecurityRole();
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

           Logger.warn("Controlling of the system global permissions");

               ArrayList<String> perms = new ArrayList<>();

               perms.add("processor.read");
               perms.add("processor.edit");
               perms.add("processor.create");
               perms.add("processor.delete");

               perms.add("producer.edit");
               perms.add("producer.create");
               perms.add("producer.read");
               perms.add("producer.delete");

               perms.add("type_of_board.create");
               perms.add("type_of_board.read");
               perms.add("type_of_board.edit");
               perms.add("type_of_board.delete");

               perms.add("board.create");
               perms.add("board.read");
               perms.add("board.edit");
               perms.add("board.delete");

               perms.add("role.create");
               perms.add("role.person");
               perms.add("role.manager");
               perms.add("role.delete");

               perms.add("permission.connectWithPerson");
               perms.add("permission.disconnectWithPerson");
               perms.add("permission.edit");
               perms.add("board.delete");


               for (String name : perms)
               {
                   if( PersonPermission.findByValue(name) == null) {
                       PersonPermission permission = new PersonPermission();
                       permission.value = name;
                       permission.save();
                   }
               }


       }catch (Exception e){
         e.printStackTrace();
       }

    }


    @Override
    public void onStop(Application app){
        Logger.warn("Restartuji server!");

        for( ClientThreadChecker clientThreadChecker :blockoServers){
          // clientThreadChecker.stop();
        }
    }


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);

    }


}

