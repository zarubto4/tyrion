package models.person;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.grid.Model_TypeOfWidget;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Model_ChangePropertyToken extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

            @Id public String change_property_token;
    @OneToOne   public Model_Person person;
                public Date   time_of_creation;
                public String property;
                public String value;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.change_property_token = UUID.randomUUID().toString();
            if (find.byId(this.change_property_token) == null) break;
        }
        super.save();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ChangePropertyToken> find = new Finder<>(Model_ChangePropertyToken.class);

}
