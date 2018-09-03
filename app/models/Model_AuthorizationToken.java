package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.PlatformAccess;
import utilities.errors.Exceptions.*;
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

    public UUID token;

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  public Model_Person person;

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


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_person_id;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_Person get_person() {

        if (cache_person_id == null) {
            return Model_Person.getById(get_person_id());

        }

        return Model_Person.getById(cache_person_id);
    }

    @JsonIgnore @Transient
    public UUID get_person_id() {

        if (cache_person_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("authorization_tokens.id", id).select("id").findOne();
            cache_person_id = person.id;
        }

        return cache_person_id;
    }

    @JsonIgnore @Transient
    public boolean isValid() {
        try {
            if (this.access_age != null) {
                if (this.access_age.before(new Date())) {
                    logger.trace("isValid - token is expired");
                    this.delete();
                } else {
                    this.access_age = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(72));
                    this.update();
                    return true;
                }
            } else {
                logger.trace("isValid - token is probably permanent");
                return true;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return false;
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
        token.setDate();
        token.save();

        return token;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        this.token = UUID.randomUUID();
        super.save();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.AuthorizationToken_create.name())) return;
        if(get_person_id().equals(_BaseController.personId())) return;
        throw new Result_Error_Unauthorized();
    }
    @Override public void check_read_permission()   throws _Base_Result_Exception {
       if(_BaseController.person().has_permission(Permission.AuthorizationToken_read.name())) return;
       if(get_person_id().equals( _BaseController.personId())) return;
    }
    @Override public void check_update_permission() throws _Base_Result_Exception {
        throw new Result_Error_NotSupportedException();
    }
    @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.AuthorizationToken_delete.name())) return;
        if(get_person_id().equals( _BaseController.personId())) return;
        throw new Result_Error_Unauthorized();
    }

    public enum Permission { AuthorizationToken_create, AuthorizationToken_read, AuthorizationToken_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     * For this case, we have Model_AuthorizationToken objects in storage, but ID is not a TOKEN name!
     * So, thets why we have two cache storage one for Model, one for connection from Token to Model (M:N)!
     */
    @CacheField(value = Model_AuthorizationToken.class, duration = CacheField.TwoDayCacheConstant, maxElements = 100000)
    public static Cache<UUID, Model_AuthorizationToken> cache;

    @CacheField(value = UUID.class, duration = CacheField.TwoDayCacheConstant, maxElements = 100000, name = "Model_AuthorizationToken_Token<->UUID")
    public static Cache<UUID, UUID> cache_token_name; // < TOKEN in UUID; UUID id of Model_AuthorizationToken>

    public static Model_AuthorizationToken getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_AuthorizationToken getById(UUID id) throws _Base_Result_Exception {
        Model_AuthorizationToken token = cache.get(id);
        if (token == null) {

            token = Model_AuthorizationToken.find.byId(id);
            // If token not exist its Unauthorized access
            if (token == null) throw new Result_Error_Unauthorized();

            cache.put(id, token);
        }
        // Check Permission
        if(token.its_person_operation()) {
            token.check_read_permission();
        }

        return token;
    }

    public static Model_AuthorizationToken getByToken(UUID token) {

        UUID tokenValue = cache_token_name.get(token);
        if (tokenValue == null) {

            Model_AuthorizationToken model = find.query().where().eq("token", token).select("id").findOne();
            if (model == null) throw new Result_Error_NotFound(Model_AuthorizationToken.class);

            cache_token_name.put(token, model.id);
        }
        return getById(cache_token_name.get(token));
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<UUID, Model_AuthorizationToken> find = new Finder<>(Model_AuthorizationToken.class);
}
