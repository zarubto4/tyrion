package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mongo.mongo_services.InjectStore;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

@Entity("ThingsMobile_Daily_Overview")
@Indexes({
        @Index(
                fields = {
                        @Field("msisdn")
                }
        )
})
public class ModelMongo_ThingsMobile_Daily_Overview extends _Abstract_MongoModel {


    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public Long msisdn;

    @JsonIgnore
    public Long cdr_traffic;    // Consumption in Bites
    public Long for_day;        // Consumption in Bites


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
    @InjectCache(value = ModelMongo_ThingsMobile_Daily_Overview.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_ThingsMobile_Daily_Overview> find = new CacheMongoFinder<>(ModelMongo_ThingsMobile_Daily_Overview.class);

}
