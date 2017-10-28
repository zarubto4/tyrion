package utilities.hardware_registration_auhtority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.swagger.annotations.*;
import models.Model_Board;
import models.Model_TypeOfBoard;
import models.Model_TypeOfBoard_Batch;
import org.bson.Document;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Enum_Terminal_Color;
import utilities.hardware_registration_auhtority.document_objects.DM_Board_Registration_Central_Authority;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;


import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Hardware_Registration_Authority extends Controller {

    private static final Class_Logger terminal_logger_start = new Class_Logger(Server.class);
    private static final Class_Logger terminal_logger_registration = new Class_Logger(Hardware_Registration_Authority.class);


    /**
     * Tohle rozhodně nemazat!!!!!! A ani neměnit - naprosto klíčová konfigurace záměrně zahrabaná v kodu!
     */
    private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
    private static MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
    private static MongoCollection<Document> collection = database.getCollection(DM_Board_Registration_Central_Authority.COLLECTION_NAME);


    @ApiOperation(value = "synchronize Board all with central registration authority",
            tags = { "Garfield"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result synchronize_script(){
        try{


            synchronize_mac_address_with_authority();
            synchronize_device_with_authority();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    // Před uložením desky - je nejprve proveden dotaz zda může být uložena!
    public static boolean register_device(Model_Board board, Model_TypeOfBoard typeOfBoard, Model_TypeOfBoard_Batch batch){

        terminal_logger_registration.info("Registration new Device " + board.id);

        // Kontroluji Device ID
        BasicDBObject whereQuery_board_id = new BasicDBObject();
        whereQuery_board_id.put("board_id", board.id);
        Document device_id_already_registered = collection.find(whereQuery_board_id).first();

        if(device_id_already_registered != null) {
            terminal_logger_registration.error("Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            terminal_logger_registration.error("Hardware_Registration_Authority:: register_device:: In Database is registered device with Same device ID!");
            synchronize_mac_address_with_authority();
            synchronize_device_with_authority();
            return false;
        }

        // Kontroluji Mac Addresu
        BasicDBObject whereQuery_mac = new BasicDBObject();
        whereQuery_mac.put("mac_address", board.id);
        Document mac_address_already_registered = collection.find(whereQuery_mac).first();

        if(mac_address_already_registered != null) {
            terminal_logger_registration.error("Collection name:: " + DM_Board_Registration_Central_Authority.COLLECTION_NAME);
            terminal_logger_registration.error("Hardware_Registration_Authority:: register_device:: ");
            synchronize_mac_address_with_authority();
            synchronize_device_with_authority();
            return false;
        }

        DM_Board_Registration_Central_Authority board_registration_central_authority = new DM_Board_Registration_Central_Authority();
        board_registration_central_authority.board_id = board.id;
        board_registration_central_authority.mac_address = board.mac_address;
        board_registration_central_authority.hash_for_adding = board.hash_for_adding;
        board_registration_central_authority.personal_name = board.name;
        board_registration_central_authority.type_of_board_compiler_target_name =  typeOfBoard.compiler_target_name;
        board_registration_central_authority.type_of_board_revision_name =  typeOfBoard.revision;
        board_registration_central_authority.date_of_create = board.date_of_create;
        board_registration_central_authority.revision = batch.revision;
        board_registration_central_authority.production_batch = batch.production_batch;
        board_registration_central_authority.date_of_assembly = batch.date_of_assembly;
        board_registration_central_authority.pcb_manufacture_name = batch.pcb_manufacture_name;
        board_registration_central_authority.pcb_manufacture_id = batch.pcb_manufacture_id;
        board_registration_central_authority.assembly_manufacture_name = batch.assembly_manufacture_name;
        board_registration_central_authority.assembly_manufacture_id = batch.assembly_manufacture_id;

        Document document = Document.parse(Json.toJson(board_registration_central_authority).toString());
        collection.insertOne(document);

        return true;
    }



    public static void synchronize_mac_address_with_authority(){

        terminal_logger_start.info("Hardware_Registration_Authority:: synchronize_mac_address_with_authority");

        List<Model_TypeOfBoard_Batch> batches = Model_TypeOfBoard_Batch.find.where().eq("removed_by_user", false).findList();

        terminal_logger_start.info("Hardware_Registration_Authority:: Batches for Check: " + batches.size());

        for(Model_TypeOfBoard_Batch batch : batches){
            try {

                BasicDBObject whereQuery_mac = new BasicDBObject();
                whereQuery_mac.put("revision", batch.revision);
                whereQuery_mac.put("production_batch", batch.production_batch);


                if(batch.latest_used_mac_address == null){
                    batch.latest_used_mac_address = batch.mac_address_start;
                    batch.update();
                }

                Document mac_address_already_registered = collection.find(whereQuery_mac).sort(descending("mac_address")).first();

                if(mac_address_already_registered != null){

                    String latest_used_mac_address = (String) mac_address_already_registered.get("mac_address");
                    Long latest_from_mongo = Long.parseLong(latest_used_mac_address.replace(":",""),16);

                    terminal_logger_start.info("Hardware_Registration_Authority::  Latest Used Mac Address Mongo: " + mac_address_already_registered.get("mac_address"));
                    terminal_logger_start.info("Hardware_Registration_Authority::  Latest Used Mac Address Mongo: in Long:  " + latest_from_mongo);

                    terminal_logger_start.info("Hardware_Registration_Authority::  Latest Used Mac Address Local: " + Model_TypeOfBoard_Batch.convert_to_MAC_ISO(batch.latest_used_mac_address));
                    terminal_logger_start.info("Hardware_Registration_Authority::  Latest Used Mac Address Local Database in Long:  " + batch.latest_used_mac_address);


                     if(!batch.latest_used_mac_address.equals( latest_from_mongo)) {
                         terminal_logger_start.warn("Hardware_Registration_Authority::  Its Required shift Mac Address UP ");
                         batch.latest_used_mac_address = latest_from_mongo;
                         batch.update();
                     }
                }else {
                    terminal_logger_start.error("Hardware_Registration_Authority:: mac_address_already_registered not find by Filter parameters from local database!");
                }


            }catch (Exception e){
                terminal_logger_start.internalServerError(e);
            }
        }
    }


    /*
        Synchronizace s centrální autoritou je provedena vždy na začátku spuštní serveru a také ji lze aktivovat manuálně
        pomocí URL GET z routru. V rámci časových úspor byla zvolena strategie kdy v každé databázi je nutné vytvořit typ desky a výrobní kolekci, které mají shodné názvy,
        (Tím je zamezeno synchronizaci, když o ní člověk nestojí) - Demodata mohou být snadnou alternativou, jak vytvořit výchozí pozici a synchronizaci.
     */
    public static void synchronize_device_with_authority(){

        terminal_logger_start.warn(Enum_Terminal_Color.ANSI_YELLOW + "Hardware_Registration_Authority: Synchronize!" + Enum_Terminal_Color.ANSI_RESET);

        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                try {

                    String string_json = cursor.next().toJson();
                    ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

                    final Form<DM_Board_Registration_Central_Authority> form = Form.form(DM_Board_Registration_Central_Authority.class).bind(json);
                    if (form.hasErrors()) {
                        terminal_logger_start.error("Hardware_Registration_Authority:: Document Read " + string_json);
                        terminal_logger_start.error("Hardware_Registration_Authority:: synchronize_device_with_authority:: Json from Mongo DB has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                        break;
                    }

                    DM_Board_Registration_Central_Authority help = form.get();

                    Model_Board board = Model_Board.find.byId(help.board_id);
                    if(board != null) {
                        continue;
                    }

                    terminal_logger_start.info("Hardware_Registration_Authority:: There is Hardware, witch is not registered in local Database!" + string_json);


                    // Nejdříve Najdeme jestli existuje typ desky - Ten se porovnává podle Target Name
                    // a revision name. Ty musí!!! být naprosto shodné!!!
                    Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("revision", help.type_of_board_revision_name).eq("compiler_target_name", help.type_of_board_compiler_target_name).findUnique();

                    if(typeOfBoard == null) {
                        terminal_logger_start.error("Hardware_Registration_Authority: synchronize_device_with_authority:: Something is wrong! System try to register Byzance-hardware to local database, but " +
                                "\n typeOfBoard with required parameters \"revision:\" " + help.type_of_board_revision_name +
                                ". \"compiler_target_name:\" " + help.type_of_board_compiler_target_name +
                                " not find in Database - Please Create it!"
                        );

                        terminal_logger_start.error("Hardware_Registration_Authority:: synchronize_device_with_authority:: Synchronize process not continue!");
                        break;
                    }


                    Model_TypeOfBoard_Batch typeOfBoard_batch = Model_TypeOfBoard_Batch.find.where().eq("type_of_board.id", typeOfBoard.id).eq("revision", help.revision).findUnique();
                    if(typeOfBoard_batch == null) {
                        terminal_logger_start.error("Hardware_Registration_Authority: Something is wrong! System try to register Byzance-hardware to local database, but " +
                                " typeOfBoard_batch with required parameters \"revision:\" " + help.revision +
                                " \"production_batch:\"" + help.production_batch +
                                " for type of Board " + typeOfBoard.name +
                                " not find in Database - Please Create it! Before. Mac Address will be synchronize after."
                        );
                        terminal_logger_start.error("Hardware_Registration_Authority:: synchronize_device_with_authority:: Synchronize process not continue!");
                        break;
                    }


                    board = new Model_Board();
                    board.id = help.board_id;
                    board.mac_address = help.mac_address;
                    board.hash_for_adding = help.hash_for_adding;
                    board.name = help.personal_name;
                    board.is_active = false;
                    board.date_of_create = help.date_of_create;
                    board.type_of_board = typeOfBoard;
                    board.batch_id = typeOfBoard_batch.id.toString();
                    board.save();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            terminal_logger_start.warn(Enum_Terminal_Color.ANSI_YELLOW + "Hardware_Registration_Authority: Synchronize Done!" + Enum_Terminal_Color.ANSI_RESET);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }

}
