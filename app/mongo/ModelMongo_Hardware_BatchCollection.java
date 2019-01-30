package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HardwareType;
import mongo.mongo_services.InjectStore;
import mongo.mongo_services._MongoCollectionConfig;
import org.bson.types.ObjectId;
import xyz.morphia.annotations.*;
import play.data.validation.Constraints;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model._Abstract_MongoModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import java.nio.charset.IllegalCharsetNameException;
import java.time.LocalDateTime;
import java.util.*;


@Entity("Hardware_BatchCollection")
@Indexes({
        @Index(
                fields = {
                        @Field("production_batch"),
                        @Field("compiler_target_name")
                }
        )
})
@ApiModel("HardwareBatch")
@_MongoCollectionConfig(database_name = "Tyrion")
public class ModelMongo_Hardware_BatchCollection extends _Abstract_MongoModel implements Permissible, Publishable {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_Hardware_BatchCollection.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true) @Constraints.Required public String revision;                     // Kod HW revize
    @ApiModelProperty(required = true) @Constraints.Required public String production_batch;             // Kod HW revizedate_of_assembly
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    @Constraints.Required public LocalDateTime date_of_assembly;             // Den kdy došlo k sestavení

    @ApiModelProperty(required = true) @Constraints.Required public String pcb_manufacture_name;         // Jméno výrobce desky
    @ApiModelProperty(required = true) @Constraints.Required public String pcb_manufacture_id;           // Kod výrobce desky
    @ApiModelProperty(required = true) @Constraints.Required public String assembly_manufacture_name;    // Jméno firmy co osazovala DPS
    @ApiModelProperty(required = true) @Constraints.Required public String assembly_manufacture_id;      // Kod firmy co osazovala DPS

    @ApiModelProperty(required = true) @Constraints.Required public String customer_product_name;        // Jméno HW co bude na štítku
    @ApiModelProperty(required = true) @Constraints.Required public String customer_company_name;        // Jméno várobce co bude na štítku
    @ApiModelProperty(required = true) @Constraints.Required public String customer_company_made_description;      // Made in Czech Republic (co bude na štítku)

    @ApiModelProperty(required = true) @Constraints.Required public String mac_address_start;            // Je v HExa decimálním režimu!!!! Long.parseLong(help.ean_number,16)!!!
    @ApiModelProperty(required = true) @Constraints.Required public String mac_address_end;
    @ApiModelProperty(required = true) @Constraints.Required public String latest_used_mac_address;     // Pro přiřazení je vždy nutné zvednout novou verzi - tato hodnota se dosynchronizovává se serverem

    @ApiModelProperty(required = true) @Constraints.Required public String ean_number;
    @ApiModelProperty(required = true)                       public String description;
    @ApiModelProperty(required = true) @Constraints.Required public String compiler_target_name;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_HardwareType getHardwareType() {
        return Model_HardwareType.find.query().where().eq("compiler_target_name", compiler_target_name).findOne();
    }

    @JsonIgnore
    public String get_nextMacAddress_just_for_check() throws IllegalCharsetNameException {

        logger.trace("get_nextMacAddress_just_for_check:: revision: {} , latest used mac address now: {}", revision, latest_used_mac_address);

        // Its used only for check - if some other server dont use this mac address and if its not registred in central hardware registration authority
        if (latest_used_mac_address == null) {
            return convert_to_MAC_ISO(Long.parseLong(mac_address_start, 16));
        }


        logger.trace("get_nextMacAddress_just_for_check:: Latest allowed: {} : converted:: {}", mac_address_end, Long.parseLong(mac_address_end, 16));
        logger.trace("get_nextMacAddress_just_for_check:: Latest used:    {} : converted:: {}", latest_used_mac_address, Long.parseLong(latest_used_mac_address, 16) );

        if (Long.parseLong(latest_used_mac_address, 16) >= Long.parseLong(mac_address_end, 16)) {
            throw new IllegalCharsetNameException("All Mac Address used");
        }

        logger.trace("get_nextMacAddress_just_for_check:: Latest address:  {} is ok ", latest_used_mac_address);
        Long latest = Long.parseLong(latest_used_mac_address, 16);
        logger.trace("get_nextMacAddress_just_for_check:: Latest address in Long {} is ok ", latest);
        Long latest_plus_one = latest + 1;
        logger.trace("get_nextMacAddress_just_for_check:: Latest address in Long+1 {} is ok ", latest_plus_one);

        String latest_in_string = Long.toString(latest_plus_one, 16);
        logger.trace("get_nextMacAddress_just_for_check:: Latest address in String+1 {} is ok ", latest_in_string);
        return convert_to_MAC_ISO(latest_plus_one);
    }

    @JsonIgnore
    public String get_new_MacAddress() throws IllegalCharsetNameException{

        if (latest_used_mac_address == null) {
            latest_used_mac_address = mac_address_start;
            this.update();
            return get_new_MacAddress();
        }

        if (Long.parseLong(latest_used_mac_address, 16)>= Long.parseLong(mac_address_end, 16)) {
            throw new IllegalCharsetNameException("All Mac Address used");
        }


        Long latest_used = Long.parseLong(latest_used_mac_address, 16);
        logger.debug("get_new_MacAddress in batch revision {} - Latest used MAc Address:: {}", this.revision, latest_used);

        Long latest_used_1 = latest_used + 1;

        this.latest_used_mac_address = Long.toString(latest_used_1, 16);
        logger.debug("get_new_MacAddress in batch revision {} - new one will be in long {}", this.revision, latest_used_1);
        logger.debug("get_new_MacAddress in batch revision {} - new one will be in mac  {}", this.revision, latest_used_mac_address);
        this.update();

        logger.debug("get_new_MacAddress in batch revision {} - new one will be {} ", this.revision, Long.parseLong(latest_used_mac_address, 16));
        return convert_to_MAC_ISO(Long.parseLong(latest_used_mac_address, 16));

    }


    //Konvertor Long na ISO normu Mac addressy
    @JsonIgnore
    public static String convert_to_MAC_ISO(Long mac) {

        if (mac > 0xFFFFFFFFFFFFL || mac < 0) {
            throw new IllegalArgumentException("mac out of range");
        }

        StringBuffer m = new StringBuffer(Long.toString(mac, 16));
        while (m.length() < 12){
            logger.trace("convert_to_MAC_ISO:: while");
            m.insert(0, "0");
        }

        for (int j = m.length() - 2; j >= 2; j-=2) {
            m.insert(j, ":");
        }

        return m.toString().toUpperCase();
    }

    @JsonIgnore @Override
    public boolean isPublic() {
        return true;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    public void save() {
        try {

            // Set latest latest_used_mac_address if its empty
            if (latest_used_mac_address == null) latest_used_mac_address = mac_address_start;

            // Try To make a Json and check validation properties of object  formFactory.formFromJsonWithValidation
            super.save();

        } catch (Exception e){
            logger.internalServerError(e);
            throw new RuntimeException("Save To Mongo DB faild");
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HARDWARE_BATCH;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static List<ModelMongo_Hardware_BatchCollection> getByTypeOfBoardId(String compiler_target_name) {
        try {

            return find.query().field("compiler_target_name").equal(compiler_target_name).field("deleted").equal(false).asList();

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @JsonIgnore @InjectStore
    @InjectCache(value = ModelMongo_Hardware_BatchCollection.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Hardware_BatchCollection> find = new CacheMongoFinder<>(ModelMongo_Hardware_BatchCollection.class);

}
