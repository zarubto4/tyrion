package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import utilities.cache.CacheField;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.cache.CacheMongoFinder;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.Date;
import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "FacebookProfile",
        description = "Facebook profile collection"
)
@Entity("FACEBOOK_PROFILE")
public class ModelMongo_FacebookProfile extends _Abstract_MongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String facebook_id;

    @JsonIgnore
    public String access_token;
    public String first_name;
    public String last_name;
    public String email;
    public Date created;
    public UUID person_id;

    @JsonIgnore
    public ObjectId user_id;        // Model User_id!

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void check_read_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_create_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_update_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_delete_permission() throws _Base_Result_Exception {

    }

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/


    @CacheField(value = String.class, keyType = UUID.class, duration = 240, maxElements = 10000, name = "Redirect_Link")
    @JsonIgnore
    public static Cache<UUID, String> redirect_link_cache;

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @JsonIgnore
    @CacheFinderField(value = ModelMongo_FacebookProfile.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_FacebookProfile> find = new CacheMongoFinder<>(ModelMongo_FacebookProfile.class);

}
