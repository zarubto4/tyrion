package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class PasswordRecoveryToken extends Model{
    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
    @OneToOne @JoinColumn(name = "person_id")                   public Person person;
                                                                public String password_recovery_token;
                                                                public Date time_of_creation;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Transient
    public void  setPasswordRecoveryToken(){
        while(true){ // I need Unique Value
            password_recovery_token = UUID.randomUUID().toString();
            if (PasswordRecoveryToken.find.where().eq("password_recovery_token",password_recovery_token).findUnique() == null) break;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,PasswordRecoveryToken> find = new Finder<>(PasswordRecoveryToken.class);

}
