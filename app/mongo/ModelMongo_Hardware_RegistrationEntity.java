package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jdk.nashorn.internal.ir.annotations.Ignore;
import models.Model_Hardware;
import models.Model_HardwareType;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.cache.CacheMongoFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.hardware_registration_auhtority.Enum_Hardware_Registration_DB_Key;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model._Abstract_MongoModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel( // Swagger annotation
        value = "Hardware_RegistrationEntity",
        description = "Registration entity of Byzance HArdware"
)
@Entity("Hardware_RegistrationEntity")
@Indexes({
        @Index(
                fields = {
                        @Field("full_id"),
                        @Field("mac_address"),
                        @Field("hash_for_adding")
                }
        )
})
public class ModelMongo_Hardware_RegistrationEntity extends _Abstract_MongoModel implements Permissible {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ModelMongo_Hardware_RegistrationEntity.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String full_id;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mac_address;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hash_for_adding;
    @ApiModelProperty(required = false, readOnly = true)                       public String personal_name; // Latest know name

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hardware_type_compiler_target_name;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public ObjectId production_batch_id;    // Kod HW revizedate_of_assembly

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mqtt_password;        // Kod firmy co osazovala DPS
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mqtt_username;        // Kod firmy co osazovala DPS

    /** Optional ! - Not supported now
     CAN_REGISTER,
     NOT_EXIST,
     ALREADY_REGISTERED_IN_YOUR_ACCOUNT,
     ALREADY_REGISTERED,
     PERMANENTLY_DISABLED,
     BROKEN_DEVICE;
     */
    public String state;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Ignore
    public static String generate_hash() {
        String hash = "HW" + UUID.randomUUID().toString().replaceAll("[-]","").substring(0, 24);
        if (getbyFull_hash(hash) != null) {
            return generate_hash();
        } else {
            return hash;
        }
    }

    @JsonIgnore
    public static Model_Hardware make_copy_of_hardware_to_local_database(String registration_hash) {


        ModelMongo_Hardware_RegistrationEntity help = getbyFull_hash(registration_hash);

        // Nejdříve Najdeme jestli existuje typ desky - Ten se porovnává podle Target Name
        // a revision name. Ty musí!!! být naprosto shodné!!!
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", help.hardware_type_compiler_target_name).findOne();

        if (hardwareType == null) {
            String error_description ="Synchronize_hardware - Something is wrong! System try to register Byzance-hardware to local database, but " +
                    ". \"compiler_target_name:\" " + help.hardware_type_compiler_target_name +
                    " not find in Database - Please Create it! Please Contact Technical Support!";
            logger.error(error_description);
            logger.error("synchronize_hardware - synchronization is canceled!");
            throw new RuntimeException(error_description);
        }

        Model_Hardware hardware = new Model_Hardware();
        hardware.full_id = help.full_id;
        hardware.mac_address = help.mac_address;
        hardware.name = help.personal_name;
        hardware.mqtt_username = help.mqtt_username;
        hardware.mqtt_password = help.mqtt_password;
        hardware.is_active = false;
        hardware.created = new Date(help.created);
        hardware.hardware_type = hardwareType;
        hardware.batch_id = help.production_batch_id.toString();
        hardware.save();

        return hardware;
    }


    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    public void save() {
        try {

            if(hash_for_adding == null) {
                this.hash_for_adding = generate_hash();
            }
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

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HARDWARE_ENTITY;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE);
    }



    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static boolean check_if_value_is_registered(String value, Enum_Hardware_Registration_DB_Key type) {

        ModelMongo_Hardware_RegistrationEntity entity = find.query().field(type.name()).equal(value).get();
        return entity != null;

    }

    public static ModelMongo_Hardware_RegistrationEntity getbyFull_id(String full_id) {
        try {

             return find.query().field("full_id").equal(full_id).get();

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    public static ModelMongo_Hardware_RegistrationEntity getbyFull_hash(String hash) {
        try {

            return find.query().field("hash_for_adding").equal(hash).get();

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    public static ModelMongo_Hardware_RegistrationEntity getbyFull_macAddress(String mac_address) {
        try {

            return find.query().field("mac_address").equal(mac_address).get();

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @Override
    @JsonIgnore
    public CacheMongoFinder<?> getFinder() {
        return find;
    }

    @JsonIgnore
    @InjectCache(value = ModelMongo_Hardware_RegistrationEntity.class, keyType = ObjectId.class)
    public static CacheMongoFinder<ModelMongo_Hardware_RegistrationEntity> find = new CacheMongoFinder<>(ModelMongo_Hardware_RegistrationEntity.class);

}
