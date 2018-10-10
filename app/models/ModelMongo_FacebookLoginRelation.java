package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.cache.CacheMongoFinder;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "FacebookLoginRelation",
        description = "Facebook login relation for current user"
)
@Entity("FACEBOOK_LOGIN_RELATION")
public class ModelMongo_FacebookLoginRelation extends _Abstract_MongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookLoginRelation.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public UUID hash;
    public UUID person_id;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void check_read_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_create_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_update_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore
    @Override
    public void check_delete_permission() throws _Base_Result_Exception {

    }

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @JsonIgnore
    @CacheFinderField(value = ModelMongo_FacebookLoginRelation.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_FacebookLoginRelation> find = new CacheMongoFinder<>(ModelMongo_FacebookLoginRelation.class);

}
