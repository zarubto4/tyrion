package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.PlatformAccess;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_Unauthorized;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Entity
@ApiModel(value = "AuthorizationToken", description = "Model of AuthorizationToken")
@Table(name="AuthorizationToken")
public class Model_AuthorizationToken extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_AuthorizationToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                              @JsonIgnore                   public UUID token;
       @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)  public Model_Person person;     //TODO Lazy loading

    @ApiModelProperty(required = true, value = "Record, where user make login")  public PlatformAccess where_logged; // Záznam, kde došlo k přihlášení (Becki, Tyrion, Homer, Compilator

    @ApiModelProperty(required = true,
    dataType = "integer", readOnly = true,
    value = "UNIX time in ms",
    example = "1466163478925")                              public Date   access_age;
    @ApiModelProperty(required = true)                      public String user_agent;


    @ApiModelProperty(required = true)  public String provider_user_id;             // user_id ze sociální služby (facebook, git atd)
    @Column(columnDefinition = "TEXT")
    @ApiModelProperty(required = true)  public String provider_key;                 // provider key - slouží k identifikaci pro oauth2
    @ApiModelProperty(required = true)  public String type_of_connection;           // Typ Spojení
    @ApiModelProperty(required = true)  public String return_url;                   // Url na které uživatele nakonci přesměrujeme

    @ApiModelProperty(required = true)  public boolean social_token_verified;       // Pro ověření, že token byl sociální sítí ověřen

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public boolean isValid() {
        try {
            if (this.access_age.getTime() < new Date().getTime()) {

                this.delete();
                return false;

            } else {

                this.access_age = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(24*3) + TimeUnit.MINUTES.toMillis(30));
                this.update();
                return true;

            }
        } catch (Exception e) {
            logger.error("isValid() :: Error:: ", e);
            return false;
        }
    }

    @JsonIgnore
    public void setDate() {
        this.created = new Date();
        this.access_age = new Date(created.getTime() + TimeUnit.HOURS.toMillis(72));
    }

    @JsonIgnore
    public static Model_AuthorizationToken setProviderKey(String typeOfConnection) {

        Model_AuthorizationToken token = new Model_AuthorizationToken();

        while(true) { // I need Unique Value
            String key = UUID.randomUUID().toString();
            if (Model_AuthorizationToken.find.query().where().eq("provider_key",key).findOne() == null) {
                token.provider_key = key;
                break;
            }
        }

        while(true) { // I need Unique Value
            UUID authToken = UUID.randomUUID();
            if (Model_AuthorizationToken.find.query().where().eq("token",authToken).findOne() == null) {
                token.token = authToken;
                break;
            }
        }

        token.type_of_connection = typeOfConnection;
        token.save();

        return token;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        this.token = UUID.randomUUID();
        this.setDate();
        super.save();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException(); }
    @Override public void check_read_permission()   throws _Base_Result_Exception {
       if(BaseController.person().has_permission(Permission.AuthorizationToken_read.name())) return;
       if(person.id.equals( BaseController.personId())) throw new Result_Error_Unauthorized();
    }
    @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException(); }
    @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.AuthorizationToken_delete.name())) return;
        if(person.id.equals( BaseController.personId())) throw new Result_Error_Unauthorized();
    }

    public enum Permission { AuthorizationToken_read, AuthorizationToken_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_AuthorizationToken getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_AuthorizationToken getById(UUID id) {
        return find.byId(id);
    }

    public static Model_AuthorizationToken getByToken(UUID token) {
        return find.query().where().eq("token", token).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<UUID, Model_AuthorizationToken> find = new Finder<>(Model_AuthorizationToken.class);
}
