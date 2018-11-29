package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
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
    public Long cdrTraffic;    // Consumption in Bites
    public Long for_day;       // Consumption in Bites


    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty()
    public Long data_in_bites(){
        return cdrTraffic;
    }

    @JsonProperty()
    public Long data_in_kb(){
        return cdrTraffic / 1024;
    }

    @JsonProperty()
    public Long data_in_mb(){
        return cdrTraffic / 1024 / 1024;
    }

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

    @InjectCache(value = ModelMongo_ThingsMobile_Daily_Overview.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_ThingsMobile_Daily_Overview> find = new CacheMongoFinder<>(ModelMongo_ThingsMobile_Daily_Overview.class);

}
