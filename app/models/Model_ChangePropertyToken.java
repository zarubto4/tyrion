package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Model_ChangePropertyToken extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_ChangePropertyToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

            @Id public String change_property_token;
    @OneToOne   public Model_Person person;
                public Date   time_of_creation;
                public String property;
                public String value;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/
/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.change_property_token = UUID.randomUUID().toString();
            if (find.byId(this.change_property_token) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {


        super.update();
    }


    @JsonIgnore @Override public void delete() {

        terminal_logger.trace("delete :: operation");

        super.delete();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/
/* PERMISSION Description ----------------------------------------------------------------------------------------------*/
/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_ChangePropertyToken get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ChangePropertyToken> find = new Finder<>(Model_ChangePropertyToken.class);

}
