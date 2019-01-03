package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import utilities.network.Networkable;

@ApiModel( // Swagger annotation
        value = "LastOnline",
        description = "LastOnline collection"
)
@Entity("LastOnline")
public class ModelMongo_LastOnline extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_LastOnline.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String networkable_id;
    public EntityType entity_type;

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
    public static ModelMongo_LastOnline create_record(Networkable networkable) {

        ModelMongo_LastOnline networkStatus = new ModelMongo_LastOnline();
        networkStatus.networkable_id = networkable.getId().toString();
        networkStatus.entity_type = networkable.getEntityType();
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

    @InjectStore @InjectCache(value = ModelMongo_LastOnline.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_LastOnline> find = new CacheMongoFinder<>(ModelMongo_LastOnline.class);
}
