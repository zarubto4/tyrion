package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import utilities.cache.InjectCache;
import utilities.cache.CacheMongoFinder;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.io.File;

@ApiModel(
        value = "Picture",
        description = "This entity collect data of our Pictures, Campany Logos etc"
)
@Entity("PICTURE")
public class _ModelMongo_Picture extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(_ModelMongo_Picture.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public File file;

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

    @InjectCache(value = File.class, keyType = String.class, duration = 72000, maxElements = 20000, name = "Picture_Cache")
    public static Cache<String, File> picture_cache;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @InjectCache(value = _ModelMongo_Picture.class, keyType = ObjectId.class)
    public static CacheMongoFinder<_ModelMongo_Picture> find = new CacheMongoFinder<>(_ModelMongo_Picture.class);


}