package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Model_PasswordRecoveryToken extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_PasswordRecoveryToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

          @Id public String id;
    @OneToOne public Model_Person person;
              public String password_recovery_token;
              public Date time_of_creation;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void  setPasswordRecoveryToken(){
        while(true){ // I need Unique Value
            this.password_recovery_token = UUID.randomUUID().toString();
            if (Model_PasswordRecoveryToken.find.where().eq("password_recovery_token",this.password_recovery_token).findUnique() == null) break;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Invitation.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {
        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_PasswordRecoveryToken> find = new Finder<>(Model_PasswordRecoveryToken.class);

}
