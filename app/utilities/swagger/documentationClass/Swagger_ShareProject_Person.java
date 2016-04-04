package utilities.swagger.documentationClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.persons.Person;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model for sharing project with Persons",
          value = "ShareProject_Person")
public class Swagger_ShareProject_Person {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public List<String> persons;





    @JsonIgnore @ApiModelProperty(required = true, hidden = true)
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
