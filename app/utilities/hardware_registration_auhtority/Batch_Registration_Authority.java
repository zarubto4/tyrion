package utilities.hardware_registration_auhtority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.swagger.annotations.Api;
import models.Model_Board;
import models.Model_TypeOfBoard;
import models.Model_TypeOfBoard_Batch;
import org.bson.Document;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Security;
import utilities.Server;
import utilities.hardware_registration_auhtority.document_objects.DM_Batch_Registration_Central_Authority;
import utilities.hardware_registration_auhtority.document_objects.DM_Board_Registration_Central_Authority;
import utilities.logger.Class_Logger;
import utilities.login_entities.Secured_API;

import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Batch_Registration_Authority extends Controller {

    private static final Class_Logger terminal_logger_start = new Class_Logger(Server.class);
    private static final Class_Logger terminal_logger_registration = new Class_Logger(Batch_Registration_Authority.class);


    /**
     * Tohle rozhodně nemazat!!!!!! A ani neměnit - naprosto klíčová konfigurace záměrně zahrabaná v kodu!
     */
    private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
    private static MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
    private static MongoCollection<Document> collection = database.getCollection(DM_Batch_Registration_Central_Authority.COLLECTION_NAME);

    public static boolean check_if_value_is_registered(String value, String type){

        // type == "board_id" or "mac_address"
        // Kontroluji Device ID
        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put( type ,value);
        Document device_id_already_registered = collection.find(whereQuery_board_id).first();

        if(device_id_already_registered != null) {
            return true;
        }

        return false;
    }

    // Před uložením desky - je nejprve proveden dotaz zda může být uložena!
    public static boolean register_batch(Model_TypeOfBoard typeOfBoard, Model_TypeOfBoard_Batch batch){

        terminal_logger_registration.info("Batch_Registration_Authority:: New Registration of batch {} for Type of Board {}  ", batch.production_batch, typeOfBoard.compiler_target_name);

        // Kontroluji Device ID
        if( check_if_value_is_registered(batch.id.toString(),"id")) {
            terminal_logger_registration.error("Batch_Registration_Authority:: check_if_value_is_registered:: Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            terminal_logger_registration.error("Batch_Registration_Authority:: check_if_value_is_registered:: In Database is registered batch with Same production_batch name!");
            synchronize_batch_with_authority();
            return false;
        }

        DM_Batch_Registration_Central_Authority batch_registration_central_authority = new DM_Batch_Registration_Central_Authority();
        batch_registration_central_authority.id = batch.id;
        batch_registration_central_authority.revision = batch.revision;
        batch_registration_central_authority.production_batch = batch.production_batch;
        batch_registration_central_authority.date_of_assembly = batch.date_of_assembly;
        batch_registration_central_authority.pcb_manufacture_name = batch.pcb_manufacture_name;
        batch_registration_central_authority.pcb_manufacture_id =  batch.pcb_manufacture_id;
        batch_registration_central_authority.assembly_manufacture_name =  batch.assembly_manufacture_name;
        batch_registration_central_authority.assembly_manufacture_id = batch.assembly_manufacture_id;
        batch_registration_central_authority.customer_product_name = batch.customer_product_name;
        batch_registration_central_authority.customer_company_name = batch.customer_company_name;
        batch_registration_central_authority.date_of_assembly = batch.date_of_assembly;
        batch_registration_central_authority.customer_company_made_description = batch.customer_company_made_description;
        batch_registration_central_authority.pcb_manufacture_id = batch.pcb_manufacture_id;
        batch_registration_central_authority.mac_address_start = batch.mac_address_start.toString();
        batch_registration_central_authority.mac_address_end = batch.mac_address_end.toString();
        batch_registration_central_authority.latest_used_mac_address = batch.latest_used_mac_address.toString();
        batch_registration_central_authority.ean_number = batch.ean_number.toString();
        batch_registration_central_authority.description = batch.description;
        batch_registration_central_authority.type_of_board_compiler_target_name = typeOfBoard.compiler_target_name;


        try {
            String string_json = Json.toJson(batch_registration_central_authority).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

            final Form<DM_Batch_Registration_Central_Authority> form = Form.form(DM_Batch_Registration_Central_Authority.class).bind(json);
            if (form.hasErrors()) {
                terminal_logger_start.error("Batch_Registration_Authority:: Document Registration and Validation test " + string_json);
                terminal_logger_start.error("Batch_Registration_Authority:: Probably some value is missing in by Required object " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                return false;
            }

        }catch (Exception e){
            terminal_logger_registration.internalServerError(e);
            return false;
        }


        Document document = Document.parse(Json.toJson(batch_registration_central_authority).toString());
        collection.insertOne(document);

        return true;
    }


    public static void synchronize_batch_with_authority() {

        terminal_logger_start.info("Batch_Registration_Authority:: synchronize_mac_address_with_authority");

        List<Model_TypeOfBoard_Batch> batches = Model_TypeOfBoard_Batch.find.where().eq("removed_by_user", false).findList();

        terminal_logger_start.info("Batch_Registration_Authority:: Batches for Check: " + batches.size());

        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                try {

                    String string_json = cursor.next().toJson();
                    ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

                    System.out.println("Kontrolní výpis:: " + json);

                    final Form<DM_Batch_Registration_Central_Authority> form = Form.form(DM_Batch_Registration_Central_Authority.class).bind(json);
                    if (form.hasErrors()) {
                        terminal_logger_start.error("Batch_Registration_Authority:: Document Read " + string_json);
                        terminal_logger_start.error("Batch_Registration_Authority:: synchronize_device_with_authority:: Json from Mongo DB has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                        break;
                    }

                    DM_Batch_Registration_Central_Authority help = form.get();

                    Model_TypeOfBoard_Batch batch_database = Model_TypeOfBoard_Batch.find.byId(help.id.toString());
                    if(batch_database != null) {
                        // Already Registred
                        continue;
                    }

                    Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("compiler_target_name", help.type_of_board_compiler_target_name).findUnique();
                    if(typeOfBoard == null) {
                        terminal_logger_start.error("Batch_Registration_Authority:: Required Type Of Board Read {} is missing!", help.type_of_board_compiler_target_name);
                        continue;
                    }

                    Model_TypeOfBoard_Batch batch = new Model_TypeOfBoard_Batch();
                    batch.type_of_board = typeOfBoard;

                    batch.id = help.id;

                    batch.revision = help.revision;
                    batch.production_batch = help.production_batch;

                    batch.date_of_assembly = help.date_of_assembly;

                    batch.pcb_manufacture_name = help.pcb_manufacture_name;
                    batch.pcb_manufacture_id = help.pcb_manufacture_id;

                    batch.assembly_manufacture_name = help.assembly_manufacture_name;
                    batch.assembly_manufacture_id = help.assembly_manufacture_id;

                    batch.customer_product_name = help.customer_product_name;

                    batch.customer_company_name = help.customer_company_name;
                    batch.customer_company_made_description = help.customer_company_made_description;

                    batch.mac_address_start = Long.getLong(help.mac_address_start);
                    batch.mac_address_end = Long.getLong(help.mac_address_end);
                    batch.latest_used_mac_address = Long.getLong(help.mac_address_end);

                    batch.ean_number = Long.getLong(help.ean_number);
                    batch.description = help.description;

                    // Uložení objektu do DB
                    batch.save_from_central_atuhority();

                } catch (Exception e) {
                    terminal_logger_registration.internalServerError(e);
                }
            }
        } catch (Exception e) {
            terminal_logger_registration.internalServerError(e);
        }
    }


}
