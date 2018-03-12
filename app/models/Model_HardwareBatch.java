package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import controllers._BaseFormFactory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.Document;
import play.data.validation.Constraints;
import play.libs.Json;
import responses.Result_NotFound;
import utilities.errors.Exceptions.Result_Error_Bad_request;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import javax.persistence.*;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@ApiModel(description = "Model of Production Batch  ", value = "HardwareBatch")
public class Model_HardwareBatch {

    /**
     * _BaseFormFactory
     */
    public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

    /**
     *  Static Constant - Here qe are not used classic Database Model, but External Mongo DB
     */
    public static final String COLLECTION_NAME = "batch-registration-authority";
    private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
    private static MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
    private static MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareBatch.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true) @Constraints.Required public String batch_id;
    @ApiModelProperty(required = true) @Constraints.Required public String revision;                     // Kod HW revize
    @ApiModelProperty(required = true) @Constraints.Required public String production_batch;             // Kod HW revizedate_of_assembly
    @ApiModelProperty(required = true) @Constraints.Required public String date_of_assembly;             // Den kdy došlo k sestavení
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
    @ApiModelProperty(required = true) @Constraints.Required public boolean deleted;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/
    

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public String get_nextMacAddress_just_for_check() throws IllegalCharsetNameException{

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

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    public void save() throws _Base_Result_Exception {
        try {

            // Set ID
            this.batch_id = UUID.randomUUID().toString();

            // Set latest latest_used_mac_address if its empty
            if (latest_used_mac_address == null) latest_used_mac_address = mac_address_start;

            // Try To make a Json and check validation properties of object  baseFormFactory.formFromJsonWithValidation
            String string_json = Json.toJson(this).toString();
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
            baseFormFactory.formFromJsonWithValidation(Model_HardwareBatch.class, json);

            // Create Document - Save Document
            Document document = Document.parse(Json.toJson(this).toString());
            collection.insertOne(document);



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
            baseFormFactory.formFromJsonWithValidation(Model_HardwareBatch.class, json);

            Document document = Document.parse(Json.toJson(this).toString());
            collection.updateOne( eq("batch_id", batch_id), new Document("$set", document));


        } catch (Exception e){
            logger.internalServerError(e);
            throw new Result_Error_Bad_request("Save To Mongo DB faild");
        }
    }


    public void delete() {
        collection.deleteOne(eq("batch_id", batch_id));
    }

/* HELP Methods --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public void check_create_permission() throws _Base_Result_Exception {
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", compiler_target_name).findOne();
        if(hardwareType == null){
            logger.error("check_update_permission - Model_HardwareType not found!");
            throw new Result_Error_NotFound(Model_HardwareType.class);
        }

        hardwareType.check_update_permission();
    }

    @JsonIgnore @Transient public void check_read_permission()   throws _Base_Result_Exception {
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", compiler_target_name).findOne();
        if(hardwareType == null){
            logger.error("check_update_permission - Model_HardwareType not found!");
            throw new Result_Error_NotFound(Model_HardwareType.class);
        }

        hardwareType.check_read_permission();
    }

    @JsonIgnore @Transient public void check_update_permission() throws _Base_Result_Exception {
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", compiler_target_name).findOne();
        if(hardwareType == null){
            logger.error("check_update_permission - Model_HardwareType not found!");
            throw new Result_Error_NotFound(Model_HardwareType.class);
        }

        hardwareType.check_update_permission();
    }

    @JsonIgnore @Transient public void check_delete_permission() throws _Base_Result_Exception {
        Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", compiler_target_name).findOne();
        if(hardwareType == null){
            logger.error("check_update_permission - Model_HardwareType not found!");
            throw new Result_Error_NotFound(Model_HardwareType.class);
        }

        hardwareType.check_update_permission();
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_HardwareBatch getById(UUID id) throws _Base_Result_Exception, IOException {
        return getById(id.toString());
    }

    public static Model_HardwareBatch getById(String id) throws _Base_Result_Exception, IOException {

        BasicDBObject query = new BasicDBObject();
        query.put("batch_id", id);
        query.put("deleted", false);

        Document document = collection.find(query).first();

        if(document == null) {
            throw new Result_Error_NotFound(Model_HardwareBatch.class);
        }

        String string_json = document.toJson();
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);

        Model_HardwareBatch batch = baseFormFactory.formFromJsonWithValidation(Model_HardwareBatch.class, json);

        return batch;

    }

    public static List<Model_HardwareBatch> getByTypeOfBoardId(String compiler_target_name) {
        try {

            BasicDBObject query = new BasicDBObject();
            query.put("compiler_target_name", compiler_target_name);
            query.put("deleted", false);

            MongoCursor<Document> cursor = collection.find(query).iterator();

            List<Model_HardwareBatch> batches = new ArrayList<>();
            while (cursor.hasNext()) {

                String string_json = cursor.next().toJson();
                ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
                Model_HardwareBatch batch = baseFormFactory.formFromJsonWithValidation(Model_HardwareBatch.class, json);
                batches.add(batch);
            }

            return batches;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


}
