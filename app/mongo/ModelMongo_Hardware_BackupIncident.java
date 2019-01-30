package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_Hardware;
import mongo.mongo_services.InjectStore;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.*;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;


@Entity("Hardware_BackupIncident")
@Indexes({
        @Index(
                fields = {
                        @Field("hardware_id")
                }
        )
})
public class ModelMongo_Hardware_BackupIncident extends _Abstract_MongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_Hardware_BackupIncident.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Indexed
    public String hardware_id;


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
    public static ModelMongo_Hardware_BackupIncident create_record(Model_Hardware hardware) {

        ModelMongo_Hardware_BackupIncident status = new ModelMongo_Hardware_BackupIncident();
        status.hardware_id = hardware.id.toString();

        status.save();
        return status;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/



/* FINDER --------------------------------------------------------------------------------------------------------------*/


    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @JsonIgnore @InjectStore
    @InjectCache(value = ModelMongo_Hardware_BackupIncident.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Hardware_BackupIncident> find = new CacheMongoFinder<>(ModelMongo_Hardware_BackupIncident.class);

}
