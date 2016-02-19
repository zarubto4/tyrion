package models.persons;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;
import java.security.SecureRandom;

@Entity
public class ValidationToken extends Model{

    @Id public String personEmail;
        public String authToken;

    public static Finder<String,ValidationToken> find = new Finder<>(ValidationToken.class);

    // Tohle slouží k ověřování emailu - tedy funkčního účtu


    public ValidationToken  setValidation(String mail){

        this.personEmail = mail;

        while(true){ // I need Unique Value
            authToken = new BigInteger(130, new SecureRandom()).toString(32).toLowerCase();
            if (ValidationToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }

        save();

        return this;
    }



}
