package utilities.demo_data;

import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;

import java.util.List;

public class Basic_Data {


    public static void set_Developer_objects(){

        // For Developing
        if(SecurityRole.findByName("SuperAdmin") == null){
            SecurityRole role = new SecurityRole();
            role.person_permissions.addAll(PersonPermission.find.all());
            role.name = "SuperAdmin";
            role.save();
        }

        if (Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null)
        {
            System.err.println("Creating first admin account: admin@byzance.cz, password: 123456789, token: token");
            Person person = new Person();
            person.full_name = "Admin Byzance";
            person.mailValidated = true;
            person.nick_name = "Syndibád";
            person.mail = "admin@byzance.cz";
            person.setSha("123456789");
            person.roles.add(SecurityRole.findByName("SuperAdmin"));

            person.save();

            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

        }else{
            // updatuji oprávnění
            Person person = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            List<PersonPermission> personPermissions = PersonPermission.find.all();

            for(PersonPermission personPermission :  personPermissions) if(!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();
        }

    }





}
