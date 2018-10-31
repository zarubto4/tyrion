package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

@Entity("THINGSMOBILE_CRD")
public class ModelMongo_ThingsMobile_CRD extends _Abstract_MongoModel {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_FacebookProfile.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public Long msisdn;
    public Long cdrImsi;

    public Long cdrDateStart;   // Time in Millis
    public Long cdrDateStop;    // Time in Millis

    public String cdrNetwork;
    public String cdrCountry;   // Where sim consumt data

    @JsonIgnore
    public Long cdrTraffic;    // Consumption in Bites


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @Transient
    @JsonProperty()
    public Long data_in_bites(){
        return cdrTraffic;
    }

    @Transient
    @JsonProperty()
    public Long data_in_kb(){
        return cdrTraffic / 1024;
    }

    @Transient
    @JsonProperty()
    public Long data_in_mb(){
        return cdrTraffic / 1024 / 1024;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


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


    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        System.out.println("setMsisdn Long");
        this.msisdn = msisdn;
    }

    public void setMsisdn(String msisdn) {
        System.out.println("setMsisdn String");
    }


    public Long getCdrImsi() {
        return cdrImsi;
    }

    public void setCdrImsi(Long cdrImsi) {
        this.cdrImsi = cdrImsi;
    }

    public Long getCdrDateStart() {
        return cdrDateStart;
    }

    public void setCdrDateStart(Long cdrDateStart) {
        this.cdrDateStart = cdrDateStart;
    }

    public Long getCdrDateStop() {
        return cdrDateStop;
    }

    public void setCdrDateStop(Long cdrDateStop) {
        this.cdrDateStop = cdrDateStop;
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

    @InjectCache(value = ModelMongo_ThingsMobile_CRD.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_ThingsMobile_CRD> find = new CacheMongoFinder<>(ModelMongo_ThingsMobile_CRD.class);

}
