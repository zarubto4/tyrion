package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="PasswordRecoveryToken")
public class Model_PasswordRecoveryToken extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_PasswordRecoveryToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @OneToOne public Model_Person person;
              public String password_recovery_token;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void  setPasswordRecoveryToken() {
        while(true) { // I need Unique Value
            this.password_recovery_token = UUID.randomUUID().toString();
            if (Model_PasswordRecoveryToken.find.query().where().eq("password_recovery_token",this.password_recovery_token).findOne() == null) break;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_PasswordRecoveryToken> find = new Finder<>(Model_PasswordRecoveryToken.class);
}
