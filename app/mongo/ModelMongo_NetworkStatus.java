package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import utilities.Server;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import utilities.network.Networkable;

@ApiModel( // Swagger annotation
        value = "NetworkStatus",
        description = "NetworkStatus collection"
)
@Entity("NetworkStatus")
public class ModelMongo_NetworkStatus extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_NetworkStatus.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    public String networkable_id;
    public NetworkStatus status;
    public EntityType entity_type;

    // Common
    public String server_version;
    public ServerMode server_type;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        server_version = Server.version;
        server_type = Server.mode;
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static ModelMongo_NetworkStatus create_record(Networkable networkable, NetworkStatus status) {

        ModelMongo_NetworkStatus networkStatus = new ModelMongo_NetworkStatus();
        networkStatus.networkable_id = networkable.getId().toString();
        networkStatus.entity_type = networkable.getEntityType();
        networkStatus.status = status;
        networkStatus.save();

        return networkStatus;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @InjectCache(value = ModelMongo_NetworkStatus.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_NetworkStatus> find = new CacheMongoFinder<>(ModelMongo_NetworkStatus.class);
}
