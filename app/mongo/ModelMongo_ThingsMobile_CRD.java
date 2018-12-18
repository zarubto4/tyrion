package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.beans.Transient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity("ThingsMobile_CRD")
public class ModelMongo_ThingsMobile_CRD extends _Abstract_MongoModel {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public Long msisdn;
    @JsonIgnore() public Long cdrImsi;

    @JsonIgnore() public Long cdrDateStart;   // Time in Millis
    @JsonIgnore() public Long cdrDateStop;    // Time in Millis

    @JsonIgnore()  public String cdrNetwork;
    @JsonIgnore() public String cdrCountry;   // Where sim consumt data

    @JsonIgnore public Long cdrTraffic;    // Consumption in Bites


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/


    @JsonProperty()
    @ApiModelProperty(required = true,
            value = "Dates in Bites")
    public Long data_in_bites(){
        return cdrTraffic;
    }

    @JsonProperty()
    @ApiModelProperty(required = true,
            value = "UNIX time in s",
            example = "1466163471")
    public Long crd_msisdn() {
        return cdrImsi;
    }

    @JsonProperty()
    @ApiModelProperty(required = true,
            value = "UNIX time in s",
            example = "1466163471")
    public Long cdr_date_start() {
        return cdrDateStart;
    }

    @JsonProperty()
    @ApiModelProperty(required = true)
    public Long cdr_date_stop() {
        return cdrDateStop;
    }

    @JsonProperty()
    @ApiModelProperty(required = true)
    public String cdr_network() {
        return cdrNetwork;
    }

    @JsonProperty()
    @ApiModelProperty(required = true)
    public String cdr_country() {
        return cdrCountry;
    }

    @ApiModelProperty(required = true, value = "Total Cost in €")
    @JsonProperty()
    public Double cost() {

        //1MB - 0,40€
        //1000Kb - 0,40€
        //1000000Kb - 0,40€
        return cdrTraffic * (0.4 / 1024 / 1024 ) ;
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public void setMsisdn(Long msisdn) {
        System.out.println("setMsisdn Long");
        this.msisdn = msisdn;
    }

    public void setMsisdn(String msisdn) {
        System.out.println("setMsisdn String");
    }


    @Transient
    @JsonIgnore()
    public LocalDateTime date_from(){
        return  Instant.ofEpochMilli(cdrDateStart).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Transient
    @JsonIgnore()
    public LocalDateTime date_to(){
        return  Instant.ofEpochMilli(cdrDateStop).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @JsonIgnore()
    public Long getMsisdn() {
        return msisdn;
    }

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
