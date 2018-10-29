package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import models.Model_CompilationServer;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import utilities.Server;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "CompilationServerOnlineStatus",
        description = "Compilation Server profile collection"
)
@Entity("CompilationServer_OnlineStatus")
@Indexes({
        @Index(
                fields = {
                        @Field("compilation_server_id")
                }
        )
})
public class ModelMongo_CompilationServer_OnlineStatus extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_CompilationServer_OnlineStatus.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String server_version;
    public UUID compilation_server_id;
    public boolean online_status;

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
    public static ModelMongo_CompilationServer_OnlineStatus create_record(Model_CompilationServer server, boolean online) {

        ModelMongo_CompilationServer_OnlineStatus status = new ModelMongo_CompilationServer_OnlineStatus();
        status.compilation_server_id = server.id;
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

    @InjectCache(value = ModelMongo_CompilationServer_OnlineStatus.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_CompilationServer_OnlineStatus> find = new CacheMongoFinder<>(ModelMongo_CompilationServer_OnlineStatus.class);
}
