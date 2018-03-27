package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.ApiModelProperty;
import org.bson.Document;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.errors.Exceptions.Result_Error_Bad_request;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions.Result_Error_Registration_Fail;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.hardware_registration_auhtority.Enum_Hardware_Registration_DB_Key;
import utilities.logger.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static controllers._BaseController.person;

/**
 * Zástupný model pro Mongo Databázi synchronizjící a zastupující virtuální registraci hardwaru.
 * Kde jsou uložené přístupové klíče hardwaru (ty původní, prvně vygenerované), tak i mac address
 * a další číčoviny.
 *
 * Není supportováno delete!
 */
public class Model_HardwareRegistrationEntity {

    /**
     * _BaseFormFactory
     */
    public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareRegistrationEntity.class);

/* Mongo VALUE  -----------------------------------------------------------------------------------------------------*/

    public static final String COLLECTION_NAME = "hardware-registration-authority";
    /**
     *  Static Constant - Here qe are not used classic Database Model, but External Mongo DB
     */


    public static MongoCollection<Document> collection() {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
        MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        return collection;
    }
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String full_id;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mac_address;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hash_for_adding;
    @ApiModelProperty(required = false, readOnly = true)                       public String personal_name; // Latest know name

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hardware_type_compiler_target_name;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String created;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String production_batch_id;    // Kod HW revizedate_of_assembly

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



 /* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public Model_HardwareBatch get_batch() {
        return null;
    }

    @JsonIgnore
    public Model_HardwareType get_hardware_type() {
        return null;
    }

    public static String generate_hash() throws IOException {
        String hash = "HW" + UUID.randomUUID().toString().replaceAll("[-]","").substring(0, 24);
        if (getbyFull_hash(hash) != null) {
            return generate_hash();
        } else {
            return hash;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    public void save() throws _Base_Result_Exception {
        try {

            this.hash_for_adding = generate_hash();

            // Set Date but with time in millis
            Long longs = new Date().getTime();
            this.created = longs.toString();

            String string_json = Json.toJson(this).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
            baseFormFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);

            // Create Document - Save Document
            Document document = Document.parse(Json.toJson(this).toString());
            collection() .insertOne(document);


        } catch (Exception e){
            logger.internalServerError(e);
            throw new Result_Error_Bad_request("Save To Mongo DB faild");
        }
    }

    public void update() {
        try {

            // Try To make a Json and check validation properties of object  baseFormFactory.formFromJsonWithValidation
            String string_json = Json.toJson(this).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
            baseFormFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);

            Document document = Document.parse(Json.toJson(this).toString());
            collection() .updateOne( eq("full_id", full_id), new Document("$set", document));

        } catch (Exception e){
            logger.internalServerError(e);
            throw new Result_Error_Bad_request("Save To Mongo DB faild");
        }
    }

    public static Model_Hardware make_copy_of_hardware_to_local_database(String registration_hash) throws java.io.IOException {


        Model_HardwareRegistrationEntity help = getbyFull_hash(registration_hash);

        // Nejdříve Najdeme jestli existuje typ desky - Ten se porovnává podle Target Name
        // a revision name. Ty musí!!! být naprosto shodné!!!
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", help.hardware_type_compiler_target_name).findOne();

        if (hardwareType == null) {
            String error_description ="Synchronize_hardware - Something is wrong! System try to register Byzance-hardware to local database, but " +
                    ". \"compiler_target_name:\" " + help.hardware_type_compiler_target_name +
                    " not find in Database - Please Create it! Please Contact Technical Support!";
            logger.error(error_description);
            logger.error("synchronize_hardware - synchronization is canceled!");
            throw new Result_Error_Registration_Fail(error_description);
        }

        Model_Hardware hardware = new Model_Hardware();
        hardware.full_id = help.full_id;
        hardware.mac_address = help.mac_address;
        hardware.name = help.personal_name;
        hardware.mqtt_username = help.mqtt_username;
        hardware.mqtt_password = help.mqtt_password;
        hardware.is_active = false;
        hardware.created = new Date(new Long(help.created));
        hardware.hardware_type = hardwareType;
        hardware.batch_id = help.production_batch_id;
        hardware.save();

        return hardware;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static boolean check_if_value_is_registered(String value, Enum_Hardware_Registration_DB_Key type) {

        // Kontroluji Device ID
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(type.name() ,value);
        Document device_id_already_registered = collection() .find(whereQuery).first();

        if (device_id_already_registered != null) {
            return true;
        }

        return false;
    }

    public static Model_HardwareRegistrationEntity getbyFull_id(String full_id) throws _Base_Result_Exception, IOException {

        // If its person operation
        if(_BaseController.isAuthenticated()) {
            if (!person().is_admin()) {
                throw new Result_Error_PermissionDenied();
            }
        }

        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put(Enum_Hardware_Registration_DB_Key.full_id.name(), full_id);
        Document device = collection() .find(whereQuery_board_id).first();

        if(device == null) {
            return null;
        }

        String string_json = device.toJson();
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

        return baseFormFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);
    }

    public static Model_HardwareRegistrationEntity getbyFull_hash(String hash) throws _Base_Result_Exception, IOException {
        try {

            BasicDBObject whereQuery_board_id = new BasicDBObject();
            whereQuery_board_id.put("hash_for_adding", hash);
            Document device = collection() .find(whereQuery_board_id).first();

            if(device == null) {
                return null;
            }

            String string_json = device.toJson();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

            return baseFormFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    public static Model_HardwareRegistrationEntity getbyFull_macAddress(String mac_address) throws _Base_Result_Exception, IOException {
        try {

            BasicDBObject whereQuery_board_id = new BasicDBObject();
            whereQuery_board_id.put("mac_address", mac_address);
            Document device = collection() .find(whereQuery_board_id).first();

            if(device == null) {
                return null;
            }

            String string_json = device.toJson();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

            return baseFormFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
}
