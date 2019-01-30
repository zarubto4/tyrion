package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import mongo.mongo_services.InjectStore;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import utilities.cache.InjectCache;
import utilities.cache.CacheMongoFinder;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "FacebookLoginRelation",
        description = "Facebook login relation for current user"
)
@Entity("Facebook_LoginRelation")
public class ModelMongo_FacebookLoginRelation extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookLoginRelation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public UUID hash;
    public UUID person_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @JsonIgnore
    @InjectStore
    @InjectCache(value = ModelMongo_FacebookLoginRelation.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_FacebookLoginRelation> find = new CacheMongoFinder<>(ModelMongo_FacebookLoginRelation.class);

}
