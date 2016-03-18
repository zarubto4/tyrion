package models.persons;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Entity
public class FloatingPersonToken extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String connection_id;
                                     @JsonIgnore            public String authToken;
         @JsonIgnore @ManyToOne                             public Person person;
                                                            public Date created;
                                                            public Date access_age;
                                                            public String user_agent;



    public void set_basic_values(){
        this.setToken( createToken() );
        this.setDate();
    }

    public void set_basic_values(String token) {
        this.setToken( token );
        this.setDate();
    }

    private void setToken(String token){
        authToken = token;
    }

    private void setDate(){
       this.created = new Date(); // oldDate == current time
       this.access_age = new Date(created.getTime() + TimeUnit.DAYS.toMillis(72));
    }

    private String createToken(){

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if ( FloatingPersonToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }
        return authToken;
    }

    // If userDB/system make log out
    public void deleteAuthToken() {
       this.delete();
    }

    public static final Finder<String, FloatingPersonToken> find = new Finder<>(FloatingPersonToken.class);
}
