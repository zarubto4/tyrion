package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.beans.Transient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity("ThingsMobile_CDR")
public class ModelMongo_ThingsMobile_CRD extends _Abstract_MongoModel {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public Long msisdn;

    @ApiModelProperty(required = true)      public Long cdr_imsi;

    @ApiModelProperty(required = true, dataType = "integer", example = "1536424319")      public LocalDateTime cdr_date_start;   // Time in Millis
    @ApiModelProperty(required = true, dataType = "integer", example = "1536424319")      public LocalDateTime cdr_date_stop;    // Time in Millis

    @ApiModelProperty(required = true)      public String cdr_network;
    @ApiModelProperty(required = true)      public String cdr_country;   // Where sim consumt data

    @ApiModelProperty(required = true)  public Long cdr_traffic;    // Consumption in Bites


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = "Total Cost in €")
    @JsonProperty()
    public Double cost() {

        //1MB - 0,40€
        //1000Kb - 0,40€
        //1000000Kb - 0,40€
        return cdr_traffic * (0.4 / 1024 / 1024 ) ;
    }


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

    @InjectStore @InjectCache(value = ModelMongo_ThingsMobile_CRD.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_ThingsMobile_CRD> find = new CacheMongoFinder<>(ModelMongo_ThingsMobile_CRD.class);

}
