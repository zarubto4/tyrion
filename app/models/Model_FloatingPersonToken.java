package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Where_logged_tag;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Entity
@ApiModel(value = "FloatingPersonToken", description = "Model of FloatingPersonToken")
public class Model_FloatingPersonToken extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_FloatingPersonToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @Id @ApiModelProperty(required = true)                  public String connection_id;
                              @JsonIgnore                   public String authToken;
       @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)  public Model_Person person;

    @ApiModelProperty(required = true,
    dataType = "integer", readOnly = true,
    value = "UNIX time in ms",
    example = "1466163478925")                              public Date   created;

    @ApiModelProperty(required = true, value = "Record, where user make login")  public Enum_Where_logged_tag where_logged; // Záznam, kde došlo k přihlášení (Becki, Tyrion, Homer, Compilator

    @ApiModelProperty(required = true,
    dataType = "integer", readOnly = true,
    value = "UNIX time in ms",
    example = "1466163478925")                              public Date   access_age;
    @ApiModelProperty(required = true)                      public String user_agent;


    @ApiModelProperty(required = true)                      public String provider_user_id;             // user_id ze sociální služby (facebook, git atd)
    @Column(columnDefinition = "TEXT")
    @ApiModelProperty(required = true)                      public String provider_key;                 // provider key - slouží k identifikaci pro oauth2
    @ApiModelProperty(required = true)                      public String type_of_connection;           // Typ Spojení
    @ApiModelProperty(required = true)                      public String return_url;                   // Url na které uživatele nakonci přesměrujeme

    @ApiModelProperty(required = true)                      public boolean social_token_verified;       // Pro ověření, že token byl sociální sítí ověřen

    @ApiModelProperty(required = true)                      public boolean notification_subscriber;     // Pokud se s tímto tokenem frontend přihlásí k odebírání notifikací nastaví se mu hodnota true
                                                                                                        // a to z důvodů rychlého filtrování, protože uživatel může být přihlášen na 50 zařízeních a na 15 odebírá notifikace
                                                                                                        // v případě uzavření notifikačního kanálu se musí token přenastavit na false!

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/
@JsonIgnore @Transient

    public boolean isValid(){
        try {
            if(this.access_age.getTime() < new Date().getTime()){
                this.delete();
                return false;
            }else {
                this.access_age = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(72));
                this.update();
                return true;
            }
        } catch (Exception e){
            return false;
        }
    }

    // If userDB/system make log out
    public void deleteAuthToken() {
        this.delete();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

@JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        if (authToken == null || authToken.isEmpty()) this.setToken( createToken() );
        this.setDate();

        while (true) { // I need Unique Value
            this.connection_id = UUID.randomUUID().toString();
            if (Model_FloatingPersonToken.find.byId(this.connection_id) == null) break;
        }
        super.save();
    }


    @JsonIgnore
    private void setToken(String token){
        authToken = token;
    }

    @JsonIgnore
    public void setDate(){
       this.created = new Date();
       this.access_age = new Date(created.getTime() + TimeUnit.HOURS.toMillis(72));
    }

    @JsonIgnore @Transient
    private String createToken(){

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if ( Model_FloatingPersonToken.find.where().eq("authToken",authToken).findUnique() == null) break;
        }
        return authToken;
    }

    @JsonIgnore @Transient
    public static Model_FloatingPersonToken setProviderKey(String typeOfConnection ){

        Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();

        while(true){ // I need Unique Value
            String key = UUID.randomUUID().toString();
            if (Model_FloatingPersonToken.find.where().eq("provider_key",key).findUnique() == null) {
                floatingPersonToken.provider_key = key;
                break;
            }
        }

        while(true){ // I need Unique Value
            String authToken = UUID.randomUUID().toString();
            if (Model_FloatingPersonToken.find.where().eq("authToken",authToken).findUnique() == null) {
                floatingPersonToken.authToken = authToken;
                break;
            }
        }

        floatingPersonToken.type_of_connection = typeOfConnection;
        floatingPersonToken.created = new Date();
        floatingPersonToken.save();

        return floatingPersonToken;
    }


    @Override
    public void update(){
        terminal_logger.trace("update :: Delete object Id: {} ", this.authToken);

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.trace("delete :: Delete object Id: {} ", this.authToken);

        //Database Update
        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()  {  return ( person.id.equals( Controller_Security.get_person().id) ) || Controller_Security.get_person().has_permission("FloatingPersonToken_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return ( person.id.equals( Controller_Security.get_person().id) ) || Controller_Security.get_person().has_permission("FloatingPersonToken_delete"); }

    public enum permissions{ FloatingPersonToken_read, FloatingPersonToken_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    // Override in Model_Person

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static final Finder<String, Model_FloatingPersonToken> find = new Finder<>(Model_FloatingPersonToken.class);
}
