package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Model_PasswordRecoveryToken extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

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


    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Invitation.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_PasswordRecoveryToken> find = new Finder<>(Model_PasswordRecoveryToken.class);

}
