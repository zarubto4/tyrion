package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import utilities.cache.InjectCache;
import utilities.cache.CacheMongoFinder;
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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @InjectCache(value = String.class, duration = 240, maxElements = 10000, name = "Redirect_Link")
    public static Cache<UUID, String> redirect_link_cache;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @InjectCache(value = ModelMongo_FacebookProfile.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_FacebookProfile> find = new CacheMongoFinder<>(ModelMongo_FacebookProfile.class);
}
