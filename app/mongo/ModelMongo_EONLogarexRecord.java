package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mongo.mongo_services.InjectStore;
import mongo.mongo_services._MongoCollectionConfig;
import org.bson.types.ObjectId;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;

import java.time.LocalDateTime;

@Entity("TEST9")
@Indexes({
        @Index(
                fields = {
                        @Field("msisdn")
                }
        )
})
@_MongoCollectionConfig(database_name="EON_LOCAL_TEST")
public class ModelMongo_EONLogarexRecord extends _Abstract_MongoModel {


    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String gateway_id;
    public String metter_id;
    public String obic_code;
    public String server_type;
    public LocalDateTime timestamp;
    public Long value;

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

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @InjectStore
    @InjectCache(value = ModelMongo_EONLogarexRecord.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_EONLogarexRecord> find = new CacheMongoFinder<>(ModelMongo_EONLogarexRecord.class);

}
