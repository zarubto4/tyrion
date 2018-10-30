package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Hardware;
import models.Model_HardwareType;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.*;
import play.data.validation.Constraints;
import utilities.Server;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model._Abstract_MongoModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


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

    private static final Logger logger = new Logger(ModelMongo_Hardware_OnlineStatus.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @AlsoLoad("serverVersion") // handle old componentName field
    public String server_version;
    @Indexed
    public UUID hardware_id;


    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public static ModelMongo_Hardware_BackupIncident create_record(Model_Hardware hardware) {

        ModelMongo_Hardware_BackupIncident status = new ModelMongo_Hardware_BackupIncident();
        status.hardware_id = hardware.id;
        status.server_version = Server.version;

        status.save();

        return status;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/


    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @JsonIgnore @InjectCache(value = ModelMongo_Hardware_BackupIncident.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Hardware_BackupIncident> find = new CacheMongoFinder<>(ModelMongo_Hardware_BackupIncident.class);

}
