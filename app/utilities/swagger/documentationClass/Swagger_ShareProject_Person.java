package utilities.swagger.documentationClass;

import models.persons.Person;

import java.util.ArrayList;
import java.util.List;

public class Swagger_ShareProject_Person {
    public List<String> persons;


    public List<Person> get_person(){
        List<Person> list = new ArrayList<>();

        // NEJDŘÍVE KONTROLA VŠECH UŽIVATELŮ ZDA EXISTUJÍ
        for (String value : persons) {
            Person person = Person.find.byId(value);
            if(person != null) list.add(person);
        }
        return list;

    }

}
