package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Entity
public class FloatingPersonToken extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String connection_id;
                                     @JsonIgnore            public String authToken;
       @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)  public Person person;

    @ApiModelProperty(required = true,
    dataType = "integer", readOnly = true,
    value = "UNIX time stamp", example = "1460126537")      public Date   created;

    @ApiModelProperty(required = true,
    dataType = "integer", readOnly = true,
    value = "UNIX time stamp", example = "1460126537")      public Date   access_age;
                                                            public String user_agent;


                                                            public String providerUserId;          // user_id ze sociální služby (facebook, git atd)
                       @Column(columnDefinition = "TEXT")   public String providerKey;             // provider key - slouží k identifikaci pro oauth2
                                                            public String typeOfConnection;        // Typ Spojení
                                                            public String returnUrl;               // Url pna které užáivatele přesměruji

                                                            public boolean social_tokenVerified;  // Pro ověření, že token byl sociální sítí ověřen

                                                            public boolean notification_subscriber;  // Pokud se s tímto tokenem frontend přihlásí k odebírání notifikací nastaví se mu hodnota true
                                                                                                     // a to z důvodů rychlého filtrování, protože uživatel může být přihlášen na 50 zařízeních a na 15 odebírá notifikace
                                                                                                     // v případě uzavření notifikačního kanálu se musí token přenastavit na false!


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void set_basic_values(){
        this.setToken( createToken() );
        this.setDate();
    }

    @JsonIgnore
    public void set_basic_values(String token) {
        this.setToken( token );
        this.setDate();
    }

    @JsonIgnore
    private void setToken(String token){
        authToken = token;
    }

    @JsonIgnore
    public void setDate(){
       this.created = new Date(); // oldDate == current time
       this.access_age = new Date(created.getTime() + TimeUnit.DAYS.toMillis(72));
    }

    @JsonIgnore @Transient
    private String createToken(){

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if ( FloatingPersonToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }
        return authToken;
    }

    @JsonIgnore @Transient
    public static FloatingPersonToken setProviderKey(String typeOfConnection ){
        FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
        while(true){ // I need Unique Value
            String key = UUID.randomUUID().toString();
            if (FloatingPersonToken.find.where().eq("providerKey",key).findUnique() == null) {
                floatingPersonToken.providerKey = key;
                break;
            }
        }

        while(true){ // I need Unique Value
            String authToken = UUID.randomUUID().toString();
            if (FloatingPersonToken.find.where().eq("authToken",authToken).findUnique() == null) {
                floatingPersonToken.authToken = authToken;
                break;
            }
        }

        floatingPersonToken.typeOfConnection = typeOfConnection;
        floatingPersonToken.created = new Date();
        floatingPersonToken.save();
        return floatingPersonToken;
    }


    // If userDB/system make log out
    public void deleteAuthToken() {
       this.delete();
    }




/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public Boolean read_permission()  {  return ( person.id.equals( SecurityController.getPerson().id) ) || SecurityController.getPerson().has_permission("FloatingPersonToken_read");   }
    @JsonProperty @Transient public Boolean delete_permission(){  return ( person.id.equals( SecurityController.getPerson().id) ) || SecurityController.getPerson().has_permission("FloatingPersonToken_delete"); }

    public enum permissions{ FloatingPersonToken_read, FloatingPersonToken_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static final Finder<String, FloatingPersonToken> find = new Finder<>(FloatingPersonToken.class);
}
