package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import models.Model_Hardware;
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

@ApiModel( // Swagger annotation
        value = "HardwareOnlineStatus",
        description = "Hardware profile collection"
)
@Entity("Hardware_ActivationStatus")
@Indexes({
        @Index(
                fields = {
                        @Field("hardware_id")
                }
        )
})
public class ModelMongo_Hardware_ActivationStatus extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_Hardware_ActivationStatus.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String hardware_id;
    public boolean activation;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public static ModelMongo_Hardware_ActivationStatus create_record(Model_Hardware hardware, boolean activation) {

        ModelMongo_Hardware_ActivationStatus status = new ModelMongo_Hardware_ActivationStatus();
        status.activation = activation;
        status.hardware_id = hardware.id.toString();

        status.save();

        return status;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @InjectStore
    @InjectCache(value = ModelMongo_Hardware_ActivationStatus.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Hardware_ActivationStatus> find = new CacheMongoFinder<>(ModelMongo_Hardware_ActivationStatus.class);
}
