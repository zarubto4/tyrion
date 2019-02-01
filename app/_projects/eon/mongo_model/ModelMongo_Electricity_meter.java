package _projects.eon.mongo_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import mongo.mongo_services.InjectStore;
import mongo.mongo_services._MongoCollectionConfig;
import org.bson.types.ObjectId;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.NetworkStatus;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import utilities.network.JsonLastOnline;
import utilities.network.JsonNetworkStatus;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Transient;

@ApiModel( // Swagger annotation
        value = "ElectroMeters",
        description = "Object represent Electricity meter"
)
@Entity("Electricity_meter")
@_MongoCollectionConfig(database_name = "EON_LOCAL_TEST")
public class ModelMongo_Electricity_meter extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_Electricity_meter.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String identification_id;
    public String name;
    public String description;

    public String owner_id;

    public String gateway_id; // Byzance Hardware ID (not Full ID)

    public Double latitude;
    public Double longitude;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonNetworkStatus @Transient
    @ApiModelProperty(required = true, value = "Value is cached with asynchronous refresh")
    public NetworkStatus online_state;

    @JsonLastOnline
    @Transient @ApiModelProperty(required = true, value = "Value is cached with asynchronous refresh")
    public Long latest_online;

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @InjectStore
    @InjectCache(value = ModelMongo_Electricity_meter.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Electricity_meter> find = new CacheMongoFinder<>(ModelMongo_Electricity_meter.class);
}
