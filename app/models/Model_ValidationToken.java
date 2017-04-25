package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "ValidationToken", description = "Model of Validation of REST-API Token")
public class Model_ValidationToken extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

private static final Class_Logger terminal_logger = new Class_Logger(Model_ValidationToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public String personEmail;
        public String authToken;
        public Date created;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_ValidationToken setValidation(String mail){

        this.personEmail = mail;
        this.created = new Date();

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if (Model_ValidationToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }
        save();
        return this;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");
        super.save();

    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object email: {}",  this.personEmail);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object email: {} ", this.personEmail);
        super.delete();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_ValidationToken> find = new Finder<>(Model_ValidationToken.class);

}
