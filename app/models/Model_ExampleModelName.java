package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * Toto je schéma nového Modelu
 */

@Entity
@ApiModel(description = "Model of ExampleModelName",
        value = "ExampleModelName")
public class Model_ExampleModelName extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public String id;

    @JsonIgnore public Date created;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        created = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ExampleModelName.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String,Model_ExampleModelName> find = new Model.Finder<>(Model_ExampleModelName.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_ExampleModelName get_byId(String id) {
        return find.byId(id);
    }
}
