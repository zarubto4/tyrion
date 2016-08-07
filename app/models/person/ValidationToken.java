package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.UUID;

@Entity
public class ValidationToken extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public String personEmail;
        public String authToken;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public ValidationToken  setValidation(String mail){



        this.personEmail = mail;

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if (ValidationToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }
        save();
        return this;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,ValidationToken> find = new Finder<>(ValidationToken.class);

}
