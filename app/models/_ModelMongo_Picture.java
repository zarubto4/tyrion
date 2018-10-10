package models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.annotations.Entity;
import utilities.cache.CacheField;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.cache.CacheMongoFinder;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
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

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /**
     * Definition of Static permission key, with static keys, we can buid groups of permisions and so on the Roles.
     * Admin with all permissions
     * Editors with some permissions
     * Feminism with no permission for anything! <3
     */
    public enum Permission { Picture_create, Picture_read, Picture_update, Picture_delete }

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void check_read_permission()   throws _Base_Result_Exception {
        // For everyone
    }

    @JsonIgnore @Override
    public void check_create_permission() throws _Base_Result_Exception {
        // For everyone
    }

    @JsonIgnore @Override
    public void check_update_permission() throws _Base_Result_Exception {
        logger.error("check_update_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore @Override
    public void check_delete_permission() throws _Base_Result_Exception {
        logger.error("check_delete_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/
    @CacheField(value = File.class, keyType = String.class, duration = 72000, maxElements = 20000, name = "Picture_Cache")
    @JsonIgnore
    public static Cache<String, File> picture_cache;


    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @JsonIgnore @CacheFinderField(value = _ModelMongo_Picture.class, keyType = ObjectId.class)
    public static CacheMongoFinder<_ModelMongo_Picture> find = new CacheMongoFinder<>(_ModelMongo_Picture.class);


}