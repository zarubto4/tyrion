package utilities.hardware_registration_auhtority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import controllers._BaseFormFactory;
import io.swagger.annotations.Api;
import models.Model_HardwareType;
import models.Model_HardwareBatch;
import org.bson.Document;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Security;
import utilities.authentication.Authentication;
import utilities.hardware_registration_auhtority.document_objects.DM_Batch_Registration_Central_Authority;
import utilities.hardware_registration_auhtority.document_objects.DM_Board_Registration_Central_Authority;
import utilities.logger.Logger;
import websocket.messages.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Batch_Registration_Authority extends Controller {

    @Inject public static _BaseFormFactory baseFormFactory;

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Batch_Registration_Authority.class);

    /**
     * Tohle rozhodně nemazat!!!!!! A ani neměnit - naprosto klíčová konfigurace záměrně zahrabaná v kodu!
     */
    private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
    private static MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
    private static MongoCollection<Document> collection = database.getCollection(DM_Batch_Registration_Central_Authority.COLLECTION_NAME);

    public static boolean check_if_value_is_registered(String value, String type) {

        // type == "full_id" or "mac_address"
        // Kontroluji Device ID
        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put( type ,value);
        Document device_id_already_registered = collection.find(whereQuery_board_id).first();

        if (device_id_already_registered != null) {
            return true;
        }

        return false;
    }

    // Před uložením desky - je nejprve proveden dotaz zda může být uložena!
    public static boolean register_batch(Model_HardwareType hardwareType, Model_HardwareBatch batch) {

        logger.info("Batch_Registration_Authority:: New Registration of batch {} for Type of Board {}  ", batch.production_batch, hardwareType.compiler_target_name);

        // Kontroluji Device ID
        if ( check_if_value_is_registered(batch.id.toString(),"id")) {
            logger.error("Batch_Registration_Authority:: check_if_value_is_registered:: Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            logger.error("Batch_Registration_Authority:: check_if_value_is_registered:: In Database is registered batch with Same production_batch name!");
            synchronize();
            return false;
        }

        DM_Batch_Registration_Central_Authority batch_registration_central_authority = new DM_Batch_Registration_Central_Authority();
        batch_registration_central_authority.id = batch.id;
        batch_registration_central_authority.revision = batch.revision;
        batch_registration_central_authority.production_batch = batch.production_batch;
        batch_registration_central_authority.date_of_assembly = batch.assembled;
        batch_registration_central_authority.pcb_manufacture_name = batch.pcb_manufacture_name;
        batch_registration_central_authority.pcb_manufacture_id =  batch.pcb_manufacture_id;
        batch_registration_central_authority.assembly_manufacture_name =  batch.assembly_manufacture_name;
        batch_registration_central_authority.assembly_manufacture_id = batch.assembly_manufacture_id;
        batch_registration_central_authority.customer_product_name = batch.customer_product_name;
        batch_registration_central_authority.customer_company_name = batch.customer_company_name;
        batch_registration_central_authority.date_of_assembly = batch.assembled;
        batch_registration_central_authority.customer_company_made_description = batch.customer_company_made_description;
        batch_registration_central_authority.pcb_manufacture_id = batch.pcb_manufacture_id;
        batch_registration_central_authority.mac_address_start = batch.mac_address_start.toString();
        batch_registration_central_authority.mac_address_end = batch.mac_address_end.toString();
        batch_registration_central_authority.latest_used_mac_address = batch.latest_used_mac_address.toString();
        batch_registration_central_authority.ean_number = batch.ean_number.toString();
        batch_registration_central_authority.description = batch.description;
        batch_registration_central_authority.hardware_type_compiler_target_name = hardwareType.compiler_target_name;


        try {

            String string_json = Json.toJson(batch_registration_central_authority).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

            baseFormFactory.formFromJsonWithValidation(DM_Batch_Registration_Central_Authority.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }


        Document document = Document.parse(Json.toJson(batch_registration_central_authority).toString());
        collection.insertOne(document);

        return true;
    }


    public static void synchronize() {

        logger.info("Batch_Registration_Authority:: synchronize_mac");

        List<Model_HardwareBatch> batches = Model_HardwareBatch.find.query().where().eq("deleted", false).findList();

        logger.info("Batch_Registration_Authority:: Batches for Check: " + batches.size());

        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                try {

                    String string_json = cursor.next().toJson();
                    ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

                    DM_Batch_Registration_Central_Authority help =  baseFormFactory.formFromJsonWithValidation(DM_Batch_Registration_Central_Authority.class, json);

                    System.out.println("Co přišlo za HW Type??");
                    System.out.println(Json.toJson(help));
                    System.out.println();

                    Model_HardwareBatch batch_database = Model_HardwareBatch.find.query().where().eq("revision", help.revision).eq("production_batch", help.production_batch).findOne();
                    if (batch_database != null) {
                        logger.info("Batch_Registration_Authority:: Batch id {} revision is already registered in database", help.id, help.revision );
                        // Already Registred
                        continue;
                    } else {
                        logger.info("Batch_Registration_Authority:: Batch id {} revision is not registered in database ", help.id, help.revision );
                    }

                    Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", help.hardware_type_compiler_target_name).findOne();
                    if (hardwareType == null) {
                        logger.error("Batch_Registration_Authority:: Required Hardware type Read {} is missing!", help.hardware_type_compiler_target_name);
                        continue;
                    }

                    Model_HardwareBatch batch = new Model_HardwareBatch();
                    batch.hardware_type = hardwareType;

                    batch.id = help.id;

                    batch.revision = help.revision;
                    batch.production_batch = help.production_batch;

                    batch.assembled = help.date_of_assembly;

                    batch.pcb_manufacture_name = help.pcb_manufacture_name;
                    batch.pcb_manufacture_id = help.pcb_manufacture_id;

                    batch.assembly_manufacture_name = help.assembly_manufacture_name;
                    batch.assembly_manufacture_id = help.assembly_manufacture_id;

                    batch.customer_product_name = help.customer_product_name;

                    batch.customer_company_name = help.customer_company_name;
                    batch.customer_company_made_description = help.customer_company_made_description;

                    batch.mac_address_start = Long.parseLong(help.mac_address_start, 10);
                    batch.mac_address_end = Long.parseLong(help.mac_address_end, 10);
                    batch.latest_used_mac_address = Long.parseLong(help.latest_used_mac_address, 10);

                    if (batch.mac_address_start == null || batch.mac_address_end == null || batch.latest_used_mac_address == null) {
                        logger.error("Batch_Registration_Authority:: incompatible Mac address ");
                        return;
                    }

                    batch.ean_number = Long.parseLong(help.ean_number, 10);
                    batch.description = help.description;

                    // Uložení objektu do DB
                    batch.save_from_central_authority();

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
