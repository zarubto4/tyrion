package utilities.hardware_registration_auhtority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.*;
import models.Model_Hardware;
import models.Model_HardwareType;
import models.Model_HardwareBatch;
import org.bson.Document;
import play.libs.Json;
import play.mvc.Security;
import utilities.authentication.Authentication;
import utilities.enums.Enum_Terminal_Color;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions.Result_Error_Registration_Fail;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Hardware_Registration_Authority extends _BaseController {

    public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

/* LOGGER --------------------------------------------------------------------------------------------------------------*/
    private static final Logger logger = new Logger(Hardware_Registration_Authority.class);

/* COMMON VALUES -------------------------------------------------------------------------------------------------------*/

    /**
     * Tohle rozhodně nemazat!!!!!! A ani neměnit - naprosto klíčová konfigurace záměrně zahrabaná v kodu!
     */
    private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
    private static MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
    private static MongoCollection<Document> collection = database.getCollection(DM_Board_Registration_Central_Authority.COLLECTION_NAME);

/* CONTENT -------------------------------------------------------------------------------------------------------------*/

    public static DM_Board_Registration_Central_Authority get_registration_hardware_from_central_authority(String full_id) throws _Base_Result_Exception, IOException {

        // If its person operation
        if(_BaseController.isAuthenticated()) {
            if (!person().is_admin()) {
                throw new Result_Error_PermissionDenied();
            }
        }

        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put(Enum_Hardware_Registration_DB_Key.full_id.name(), full_id);
        Document device = collection.find(whereQuery_board_id).first();

        String string_json = device.toJson();
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

        return baseFormFactory.formFromJsonWithValidation(DM_Board_Registration_Central_Authority.class, json);
    }



    public static boolean check_if_value_is_registered(String value, Enum_Hardware_Registration_DB_Key type) {

        // Kontroluji Device ID
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(type.name() ,value);
        Document device_id_already_registered = collection.find(whereQuery).first();

        if (device_id_already_registered != null) {
            return true;
        }

        return false;
    }

    // Před uložením desky - je nejprve proveden dotaz zda může být uložena!
    public static boolean register_device(Model_Hardware hardware, Model_HardwareType hardwareType, Model_HardwareBatch batch) {

        logger.info("Registration new Device " + hardware.id);

        // Kontroluji Device ID
        if (check_if_value_is_registered(hardware.full_id, Enum_Hardware_Registration_DB_Key.full_id)) {
            logger.error("Hardware_Registration_Authority:: check_if_value_is_registered:: Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            logger.error("Hardware_Registration_Authority:: check_if_value_is_registered:: In Database is registered device with Same device ID!");
            return false;
        }

        // Kontroluji Mac Addresu
        BasicDBObject whereQuery_mac = new BasicDBObject();
        whereQuery_mac.put(Enum_Hardware_Registration_DB_Key.mac_address.name(), hardware.id);
        Document mac_address_already_registered = collection.find(whereQuery_mac).first();

        if (mac_address_already_registered != null) {
            logger.error("Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            logger.error("Hardware_Registration_Authority:: register_device:: ");
            return false;
        }

        DM_Board_Registration_Central_Authority board_registration_central_authority = new DM_Board_Registration_Central_Authority();
        board_registration_central_authority.full_id = hardware.full_id;
        board_registration_central_authority.mac_address = hardware.mac_address;
        board_registration_central_authority.hash_for_adding = Model_Hardware.generate_hash();
        board_registration_central_authority.personal_name = hardware.name;
        board_registration_central_authority.hardware_type_compiler_target_name =  hardwareType.compiler_target_name;
        board_registration_central_authority.created = ((Long)hardware.created.getTime()).toString();
        board_registration_central_authority.revision = batch.revision;
        board_registration_central_authority.production_batch_id = batch.batch_id;
        board_registration_central_authority.date_of_assembly = batch.date_of_assembly;
        board_registration_central_authority.pcb_manufacture_name = batch.pcb_manufacture_name;
        board_registration_central_authority.pcb_manufacture_id = batch.pcb_manufacture_id;
        board_registration_central_authority.assembly_manufacture_name = batch.assembly_manufacture_name;
        board_registration_central_authority.assembly_manufacture_id = batch.assembly_manufacture_id;
        board_registration_central_authority.mqtt_username = hardware.mqtt_username;
        board_registration_central_authority.mqtt_password = hardware.mqtt_password;

        // Validation test - simulation of save and get from

        try {
            String string_json = Json.toJson(board_registration_central_authority).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

            baseFormFactory.formFromJsonWithValidation(DM_Board_Registration_Central_Authority.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }

        Document document = Document.parse(Json.toJson(board_registration_central_authority).toString());
        collection.insertOne(document);

        return true;
    }

    /*
    public static void synchronize_mac() {

        logger.info("Hardware_Registration_Authority:: synchronize_mac");

        List<Model_HardwareBatch> batches = Model_HardwareBatch.find.all();

        logger.info("Hardware_Registration_Authority:: Batches for Check: " + batches.size());

        for (Model_HardwareBatch batch : batches) {
            try {

                BasicDBObject whereQuery_mac = new BasicDBObject();
                whereQuery_mac.put("revision", batch.revision);
                whereQuery_mac.put("production_batch", batch.production_batch);


                if (batch.latest_used_mac_address == null) {
                    batch.latest_used_mac_address = batch.mac_address_start;
                    batch.update();
                }

                Document mac_address_already_registered = collection.find(whereQuery_mac).sort(descending("mac_address")).first();

                if (mac_address_already_registered != null) {

                    String latest_used_mac_address = (String) mac_address_already_registered.get("mac_address");
                    Long latest_from_mongo = Long.parseLong(latest_used_mac_address.replace(":",""),16);

                    logger.info("Hardware_Registration_Authority::  Latest Used Mac Address Mongo: " + mac_address_already_registered.get("mac_address"));
                    logger.info("Hardware_Registration_Authority::  Latest Used Mac Address Mongo: in Long:  " + latest_from_mongo);

                    logger.info("Hardware_Registration_Authority::  Latest Used Mac Address Local: " + Model_HardwareBatch.convert_to_MAC_ISO(batch.latest_used_mac_address));
                    logger.info("Hardware_Registration_Authority::  Latest Used Mac Address Local Database in Long:  " + batch.latest_used_mac_address);


                     if (!batch.latest_used_mac_address.equals( latest_from_mongo)) {
                         logger.warn("Hardware_Registration_Authority::  Its Required shift Mac Address UP ");
                         batch.latest_used_mac_address = latest_from_mongo;
                         batch.update();
                     }

                } else {
                    logger.error("Hardware_Registration_Authority:: mac_address_already_registered not find by Filter parameters from local database!");
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }
    }
    */
    public static Model_Hardware make_copy_of_hardware_to_local_database(String registration_hash) throws java.io.IOException {

        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put("registration_hash", registration_hash);
        Document device = collection.find(whereQuery_board_id).first();

        String string_json = device.toJson();
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

        DM_Board_Registration_Central_Authority help = baseFormFactory.formFromJsonWithValidation(DM_Board_Registration_Central_Authority.class, json);

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

}
