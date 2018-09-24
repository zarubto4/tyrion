package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.PlatformAccess;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.Personal;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity
@ApiModel(value = "AuthorizationToken", description = "Model of AuthorizationToken")
@Table(name="AuthorizationToken")
public class Model_AuthorizationToken extends BaseModel implements Permissible, Personal {

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

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_Person getPerson() {
        return Model_Person.find.query().where().eq("authorization_tokens.id", id).select("id").findOne();
    }

    @JsonIgnore @Transient
    public UUID get_person_id() {
        return this.getPerson().id;
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


    @Override
    public EntityType getEntityType() {
        return EntityType.AUTHORIZATION_TOKEN;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     * For this case, we have Model_AuthorizationToken objects in storage, but ID is not a TOKEN name!
     * So, thets why we have two cache storage one for Model, one for connection from Token to Model (M:N)!
     */
    @CacheField(value = UUID.class, duration = CacheField.TwoDayCacheConstant, maxElements = 100000, name = "Model_AuthorizationToken_Token<->UUID")
    public static Cache<UUID, UUID> cache_token_name; // < TOKEN in UUID; UUID id of Model_AuthorizationToken>

    public static Model_AuthorizationToken getByToken(UUID token) {

        UUID tokenValue = cache_token_name.get(token);
        if (tokenValue == null) {

            Model_AuthorizationToken model = find.query().where().eq("token", token).select("id").findOne();
            if (model == null) throw new Result_Error_NotFound(Model_AuthorizationToken.class);

            cache_token_name.put(token, model.id);
        }
        return find.byId(cache_token_name.get(token));
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_AuthorizationToken.class)
    public static final CacheFinder<Model_AuthorizationToken> find = new CacheFinder<>(Model_AuthorizationToken.class);
}
