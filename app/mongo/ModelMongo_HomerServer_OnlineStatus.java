package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import models.Model_HomerServer;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import utilities.Server;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.HomerType;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "HomerServerOnlineStatus",
        description = "Homer Server profile collection"
)
@Entity("HomerServer_OnlineStatus")
@Indexes({
        @Index(
                fields = {
                        @Field("homer_id"),
                        @Field("server_type")
                }
        )
})
public class ModelMongo_HomerServer_OnlineStatus extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_HomerServer_OnlineStatus.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String server_version;
    public UUID homer_id;
    public boolean online_status;
    public HomerType server_type;


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
    public static ModelMongo_HomerServer_OnlineStatus create_record(Model_HomerServer homer, boolean online) {

        ModelMongo_HomerServer_OnlineStatus status = new ModelMongo_HomerServer_OnlineStatus();
        status.homer_id = homer.id;
        status.server_type = homer.server_type;
        status.online_status = online;
        status.server_version = Server.version;

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

    @InjectCache(value = ModelMongo_HomerServer_OnlineStatus.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_HomerServer_OnlineStatus> find = new CacheMongoFinder<>(ModelMongo_HomerServer_OnlineStatus.class);
}
