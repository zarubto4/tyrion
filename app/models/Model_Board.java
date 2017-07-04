package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.Server;
import utilities.document_db.document_objects.DM_Board_BackupIncident;
import utilities.document_db.document_objects.DM_Board_Connect;
import utilities.document_db.document_objects.DM_Board_Disconnected;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.documentationClass.Swagger_Board_for_fast_upload_detail;
import utilities.swagger.outboundClass.Swagger_Board_Short_Detail;
import utilities.swagger.outboundClass.Swagger_C_Program_Update_plan_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.homer_hardware_with_tyrion.*;
import web_socket.message_objects.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Status;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;
import web_socket.services.WS_HomerServer;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.*;


@Entity
@ApiModel(value = "Board", description = "Model of Board")
public class Model_Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Board.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                   @Id @ApiModelProperty(required = true)   public String id;                   // Full_Id procesoru přiřazené Garfieldem
                                       @ApiModelProperty(required = true)   public String hash_for_adding;      // Vygenerovaný Hash pro přidávání a párování s Platformou.

                                       @ApiModelProperty(required = true)   public String wifi_mac_address;     // Mac addressa wifi čipu
                                       @ApiModelProperty(required = true)   public String mac_address;          // Přiřazená MacAdresa z rozsahu Adres

                                       @ApiModelProperty(required = true)   public String name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String description;
                                       @JsonIgnore  @ManyToOne              public Model_TypeOfBoard type_of_board;
                                       @JsonIgnore                          public boolean is_active;
                                       @JsonIgnore                          public boolean backup_mode;
                                       @JsonIgnore                          public boolean database_synchronize;
                                                                            public Date date_of_create;
                                       @JsonIgnore                          public Date date_of_user_registration;

                      @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)   public Model_Project project;

                                                   @JsonIgnore @ManyToOne   public Model_VersionObject actual_c_program_version;
                                                   @JsonIgnore @ManyToOne   public Model_VersionObject actual_backup_c_program_version;
                                                   @JsonIgnore @ManyToOne   public Model_BootLoader    actual_boot_loader;

    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_BPair> b_pair = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();

    @JsonIgnore public String connected_server_id; // Latest know Server ID
    @JsonIgnore public String connected_instance_id; // Latest know Instance ID

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient @ApiModelProperty(required = true) public Enum_Board_BackUpMode backup_mode(){ return backup_mode ? Enum_Board_BackUpMode.AUTO_BACKUP : Enum_Board_BackUpMode.STATIC_BACKUP;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_id()   { return type_of_board.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_name() { return type_of_board.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_name()       { return       project == null ? null : project.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_bootloader_version_name()     { return  actual_boot_loader == null ? null : actual_boot_loader.name; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_bootloader_id()               { return  actual_boot_loader == null ? null : actual_boot_loader.id.toString();}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String available_bootloader_version_name()  { return  type_of_board.main_boot_loader  == null ? null :  type_of_board.main_boot_loader.name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String available_bootloader_id()            { return  type_of_board.main_boot_loader  == null ? null :  type_of_board.main_boot_loader.id.toString(); }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public List<Enum_Board_Alert> alert_list(){
        try {
            List<Enum_Board_Alert> list = new ArrayList<>();

            if(update_boot_loader_required()) list.add(Enum_Board_Alert.BOOTLOADER_REQUIRED);

            return list;
        }catch (Exception e){
            terminal_logger.internalServerError("alert_list:", e);
            return null;
        }
    }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public List<Swagger_C_Program_Update_plan_Short_Detail> updates(){

        try {
            List<Swagger_C_Program_Update_plan_Short_Detail> plans = new ArrayList<>();

            for (Model_CProgramUpdatePlan plan : Model_CProgramUpdatePlan.find.where().eq("board.id", this.id).order().desc("date_of_create").findList()) {
                try {
                    plans.add(plan.get_short_version_for_board());

                }catch (Exception e){
                    terminal_logger.internalServerError("updates:", e);
                }
            }

            return plans;
        }catch (Exception e){
            terminal_logger.internalServerError("updates:", e);
            return null;
        }
    }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public Model_HomerServer server(){ return Model_HomerServer.get_byId(connected_server_id); }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Instance_Short_Detail actual_instance(){ return get_instance().get_instance_short_detail();}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_id(){return actual_c_program_version.c_program.name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_name(){return actual_c_program_version.c_program.name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_description(){return actual_c_program_version.c_program.description;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_version_id(){return actual_c_program_version.id;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_version_name(){return actual_c_program_version.version_name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_version_description(){return actual_c_program_version.version_description;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_id(){return actual_backup_c_program_version.c_program.name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_name(){return actual_backup_c_program_version.c_program.name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_description(){return actual_backup_c_program_version.c_program.description;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_version_id(){return actual_backup_c_program_version.id;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_version_name(){return actual_backup_c_program_version.version_name;}
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_c_program_backup_version_description(){return actual_backup_c_program_version.version_description;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public List<Swagger_C_Program_Update_plan_Short_Detail> required_updates(){

        try {
            List<Model_CProgramUpdatePlan> c_program_plans = Model_CProgramUpdatePlan.find.where()
                    .disjunction()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                    .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                    .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .endJunction()
                    .eq("board.id", id).order().asc("actualization_procedure.date_of_create").findList();

            List<Swagger_C_Program_Update_plan_Short_Detail> required_c_programs = new ArrayList<>();

            for (Model_CProgramUpdatePlan plan : c_program_plans) required_c_programs.add(plan.get_short_version_for_board());

            return required_c_programs;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, name = "Null value, if device is online") public Date latest_online(){
        if(is_online()) return null;
        return last_online();
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Board_Short_Detail get_short_board(){

        try {

            Swagger_Board_Short_Detail swagger_board_short_detail = new Swagger_Board_Short_Detail();
            swagger_board_short_detail.id = id;
            swagger_board_short_detail.name = name;
            swagger_board_short_detail.description = description;
            swagger_board_short_detail.type_of_board_id = type_of_board_id();
            swagger_board_short_detail.type_of_board_name = type_of_board_name();

            swagger_board_short_detail.edit_permission = edit_permission();
            swagger_board_short_detail.delete_permission = delete_permission();
            swagger_board_short_detail.update_permission = update_permission();

            if(update_boot_loader_required()) swagger_board_short_detail.alert_list.add(Enum_Board_Alert.BOOTLOADER_REQUIRED);

            try {
                swagger_board_short_detail.board_online_status = is_online();

            }catch (Exception e){
                terminal_logger.internalServerError("get_short_board:", e);
                swagger_board_short_detail.board_online_status = false;
            }

            swagger_board_short_detail.last_online = last_online();

            return swagger_board_short_detail;

        }catch (Exception e){
            terminal_logger.internalServerError("get_short_board:", e);
            return null;
        }
    }

    @Transient @JsonIgnore public Swagger_Board_for_fast_upload_detail get_short_board_for_fast_upload(){
        try {

            Swagger_Board_for_fast_upload_detail board_for_fast_upload_detail = new Swagger_Board_for_fast_upload_detail();
            board_for_fast_upload_detail.id = id;
            board_for_fast_upload_detail.name = name;
            board_for_fast_upload_detail.description = description;

            if (this.get_instance() == null) {
                 board_for_fast_upload_detail.collision = Enum_Board_update_collision.NO_COLLISION;
            } else {
                 board_for_fast_upload_detail.collision = Enum_Board_update_collision.ALREADY_IN_INSTANCE;
            }

            board_for_fast_upload_detail.type_of_board_id = type_of_board_id();
            board_for_fast_upload_detail.type_of_board_name = type_of_board_name();

            return board_for_fast_upload_detail;

         }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }

    }

    @Transient @JsonIgnore public Model_HomerServer get_connected_server(){
        return Model_HomerServer.get_byId(this.connected_server_id);
    }

    @JsonIgnore @Transient public boolean update_boot_loader_required(){

        if(type_of_board.main_boot_loader == null || actual_boot_loader == null) return true;
        return (!this.type_of_board.main_boot_loader.id.equals(this.actual_boot_loader.id));

    }

    @JsonIgnore @Transient public Date last_online(){
        try {

            if (this.is_online()) return null;

            List<Document> documents = Server.documentClient.queryDocuments(Server.online_status_collection.getSelfLink(),"SELECT * FROM root r  WHERE r.device_id='" + this.id + "' AND r.document_type_sub_type='DEVICE_DISCONNECT'", null).getQueryIterable().toList();

            terminal_logger.debug("last_online: number of retrieved documents = {}", documents.size());

            if (documents.size() > 0) {

                DM_Board_Disconnected record;

                if (documents.size() > 1) {

                    terminal_logger.debug("last_online: more than 1 record, finding latest record");
                    record = documents.stream().max(Comparator.comparingLong(document -> document.toObject(DM_Board_Disconnected.class).time)).get().toObject(DM_Board_Disconnected.class);

                } else {

                    terminal_logger.debug("last_online: result = {}", documents.get(0).toJson());

                    record = documents.get(0).toObject(DM_Board_Disconnected.class);
                }

                terminal_logger.debug("last_online: device_id: {}", record.device_id);

                return new Date(record.time);
            }

            return null;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient public Model_HomerInstance get_instance() {

        return Model_HomerInstance.find.where().disjunction()
                .eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", this.id)
                .eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", this.id)
                .endJunction().findUnique();

    }

/* JSON IGNORE  --------------------------------------------------------------------------------------------------------*/

/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "hardware";
    
    // Messenger
    @JsonIgnore @Transient
    public static void Messages(WS_HomerServer homer, ObjectNode json){
        new Thread(() -> {
            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Hardware_connected.message_type: {

                        final Form<WS_Message_Hardware_connected> form = Form.form(WS_Message_Hardware_connected.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_connected: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_Connected(form.get());
                        return;
                    }

                    case WS_Message_Hardware_disconnected.message_type: {

                        final Form<WS_Message_Hardware_disconnected> form = Form.form(WS_Message_Hardware_disconnected.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_disconnected: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_Disconnected(form.get());
                        return;
                    }

                    case WS_Message_Hardware_autobackup_maked.message_type: {

                        final Form<WS_Message_Hardware_autobackup_maked> form = Form.form(WS_Message_Hardware_autobackup_maked.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_AutoBackUp_progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_autoBackUp_echo(form.get());
                        return;
                    }

                    case WS_Message_Hardware_UpdateProcedure_Progress.messageType: {

                        final Form<WS_Message_Hardware_UpdateProcedure_Progress> form = Form.form(WS_Message_Hardware_UpdateProcedure_Progress.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_UpdateProcedure_Progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_CProgramUpdatePlan.update_procedure_progress(form.get());
                        return;
                    }


                    case WS_Message_Hardware_UpdateProcedure_Status.messageType: {

                        final Form<WS_Message_Hardware_UpdateProcedure_Status> form = Form.form(WS_Message_Hardware_UpdateProcedure_Status.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_UpdateProcedure_Status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_CProgramUpdatePlan.update_procedure_state(form.get());
                        return;
                    }


                    default: throw new Exception("Incoming message, chanel tyrion: message_type not recognized -> " + json.get("message_type").asText());
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("Messages:", e);
            }
        }).start();
    }
    
    // Kontrola připojení
    @JsonIgnore @Transient public static void device_Connected(WS_Message_Hardware_connected help){
        try {

            terminal_logger.debug("master_device_Connected:: Updating device ID:: {} is online ", help.device_id);

            Model_Board device = Model_Board.get_byId(help.device_id);

            if(device == null) throw new Exception("Hardware not found. Message from Homer server: ID = " + help.websocket_identificator + ". Unregistered Hardware Id: " + help.device_id);

            if(cache_status.get(help.device_id) == null || !cache_status.get(help.device_id) ){
                cache_status.put(help.device_id, true);
                device.notification_board_connect();
            }

            if(!device.connected_server_id.equals(help.websocket_identificator)){
                device.connected_server_id = help.websocket_identificator;
                device.update();
            }

            device.make_log_connect();

            device.hardware_firmware_state_check();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient public static void device_Disconnected(WS_Message_Hardware_disconnected help){
        try {

            terminal_logger.debug("master_device_Disconnected:: Updating device status " +  help.device_id + " on offline ");
            cache_status.put(help.device_id, false);

            Model_Board master_device =  Model_Board.get_byId(help.device_id);

            if(master_device == null) throw new NullPointerException("Hardware not found: ID = " + help.device_id);

            master_device.notification_board_disconnect();
            master_device.make_log_disconnect();

            Model_Board.cache_status.put(help.device_id, false);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient public static void device_autoBackUp_echo(WS_Message_Hardware_autobackup_maked report){
        try {

            terminal_logger.debug("device_autoBackUp_echo:: Deive send Echo about backup device ID:: {} ", report.device_id);

            Model_Board device = Model_Board.get_byId(report.device_id);

            if(device == null) throw new Exception("Unregistered Hardware. Id = " + report.device_id);

            Model_VersionObject c_program_version = Model_VersionObject.find.where().eq("c_compilation.firmware_build_id", report.build_id).findUnique();
            if(c_program_version == null) throw new Exception("Firmware with build ID = " + report.build_id + " was not found in the database!");

            device.actual_backup_c_program_version = c_program_version;
            device.update();

            return;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    
    /* Servers Parallel tasks  ----------------------------------------------------------------------------------------------*/

    /**
     * Jelikož je nutné vrstvou zakrýt dotazování na devices, které jsou na různých server
     * a jelikož dotazování má fungovat paralelně, slouží následující metoda k tomu, že jednoltivým serverům paralelně
     * rozešle příkaz a čeká na splnění všech maximální možnou dobu. Poté odpovědi sbalí a dá do pole, které vrátí.
     *
     * Každé vlákno se nakonci podívá, kolik je hotovo, když je hotovo vše - vrací vlákno které má shodné číslo kolekci.
     *
     * @param get_result_from_servers
     * @return JsonNode
     */
    @JsonIgnore @Transient private static List<JsonNode> server_parallel(HashMap<String, ObjectNode> get_result_from_servers, Integer time, Integer delay, Integer number_of_retries){
        try {

            Collection<Callable<JsonNode>> tasks = new ArrayList<Callable<JsonNode>>();
            for (String server_id : get_result_from_servers.keySet()) {
                tasks.add(new Callable<JsonNode>() {
                    public JsonNode call() throws Exception {
                        return Model_HomerServer.get_byId(server_id).sender().write_with_confirmation(get_result_from_servers.get(server_id), time, delay, number_of_retries);
                    }
                });
            }

            List<Future<JsonNode>> future_results = Executors.newFixedThreadPool(get_result_from_servers.size()).invokeAll(tasks, time * number_of_retries + 1000, TimeUnit.MILLISECONDS);
            List<JsonNode> results = new ArrayList<>();

            for (Future<JsonNode> future : future_results) {
                results.add(future.get());
            }

            return results;

        }catch (Exception e){
            return null;
        }

    }

    @JsonIgnore @Transient private static HashMap<String, List<String>> server_Separate(List<Model_Board> devices){

        HashMap<String, List<String>> serverHashMap = new HashMap<>(); // < Model_HomerServer.id, List<Model_Board.id>

        // Separate Board Acording servers
        for(Model_Board device : devices){

            // If not collection exist -> create that
            if(!serverHashMap.containsKey(device.connected_server_id)){serverHashMap.put(device.connected_server_id, new ArrayList<String>());}

            // Add to collection
            serverHashMap.get(device.connected_server_id).add(device.id);
        }

        return serverHashMap;
    }

    @JsonIgnore @Transient private static List<Model_Board> make_array(Model_Board device){
        List<Model_Board> devices = new ArrayList<>();
        devices.add(device);
        return  devices;
    }

    
    /* Commands ----------------------------------------------------------------------------------------------*/
    
    //-- Online State Hardware  --//
    @JsonIgnore @Transient public WS_Message_Hardware_online_status get_devices_online_state(){
        return get_devices_online_state(make_array(this));
    }

    @JsonIgnore @Transient public static WS_Message_Hardware_online_status get_devices_online_state(List<Model_Board> devices){


        // Sepparate Device by servers
        HashMap<String, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parralell command
        HashMap<String, ObjectNode> request_collection = new HashMap<>();
        for(String key : serverHashMap.keySet()){
            request_collection.put(key, new WS_Message_Hardware_online_status().make_request(serverHashMap.get(key)));
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );


        // Common Result for fill
        WS_Message_Hardware_online_status result = new WS_Message_Hardware_online_status();


        for(JsonNode json_result : results ) {
            try {

                final Form<WS_Message_Hardware_online_status> form = Form.form(WS_Message_Hardware_online_status.class).bind(json_result);
                if (form.hasErrors()) throw new Exception("WS_Message_Hardware_online_status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                result.device_list.addAll(form.get().device_list);

            }catch (Exception e){
                terminal_logger.internalServerError("get_devices_online_state:", e);
                return new WS_Message_Hardware_online_status();
            }
        }

        return result;

    }

    //-- Over View Hardware  --//
    @JsonIgnore @Transient public WS_Message_Hardware_overview get_devices_overview(){
        return get_devices_overview(make_array(this));
    }

    @JsonIgnore @Transient public static WS_Message_Hardware_overview get_devices_overview(Model_Board device){
        return get_devices_overview(make_array(device));
    }

    @JsonIgnore @Transient public static WS_Message_Hardware_overview get_devices_overview(List<Model_Board> devices){

        // Sepparate Device by servers
        HashMap<String, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parralell command
        HashMap<String, ObjectNode> request_collection = new HashMap<>();
        for(String key : serverHashMap.keySet()){
            request_collection.put(key, new WS_Message_Hardware_overview().make_request(serverHashMap.get(key)) );
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );


        // Common Result for fill
        WS_Message_Hardware_overview result = new WS_Message_Hardware_overview();


        for(JsonNode json_result : results ) {
            try {

                final Form<WS_Message_Hardware_overview> form = Form.form(WS_Message_Hardware_overview.class).bind(json_result);
                if (form.hasErrors()) throw new Exception("WS_Message_Hardware_overview: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                result.device_list.addAll(form.get().device_list);


            }catch (Exception e){
                terminal_logger.internalServerError("get_devices_online_state:", e);
                return new WS_Message_Hardware_overview();
            }
        }

        return result;

    }

    // Change Hardware Alias  --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_alias set_alias(){
        try {

            JsonNode node = Model_HomerServer.get_byId(this.connected_server_id).sender().write_with_confirmation(new WS_Message_Hardware_set_alias().make_request(this), 1000 * 3, 0, 2);

            final Form<WS_Message_Hardware_set_alias> form = Form.form(WS_Message_Hardware_set_alias.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Hardware_set_alias: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (TimeoutException e){
            terminal_logger.warn("set_auto_backup: Timeout");
            return new WS_Message_Hardware_set_alias();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_set_alias();
        }
    }


    //-- ADD or Remove // Change Server --//
    @JsonIgnore @Transient public void device_relocate_server(Model_HomerServer future_server){
        List<Model_Board> devices = new ArrayList<>();
        devices.add(this);
        device_relocate_server(devices, future_server);
    }

    @JsonIgnore @Transient public void device_relocate_server(List<Model_Board> devices, Model_HomerServer future_server){
        try {

            // Sepparate Device by servers
            HashMap<String, List<String>> serverHashMap = server_Separate(devices);

            // Create Server Parralell command
            HashMap<String, ObjectNode> request_collection = new HashMap<>();
            for(String key : serverHashMap.keySet()){
                request_collection.put(key, new WS_Message_Hardware_change_server().make_request(future_server, serverHashMap.get(key)) );
            }

            // Make Parallel Operation
            List<JsonNode> results = this.server_parallel(request_collection,1000 * 10, 0, 1);

        }catch (Exception e){
            terminal_logger.internalServerError(e);

        }
    }


    //-- Set AutoBackup  --//
    @JsonIgnore @Transient public static WS_Message_Hardware_set_autobackup set_auto_backup(Model_Board board_for_update){
        try{

            JsonNode node = Model_HomerServer.get_byId(board_for_update.connected_server_id).sender().write_with_confirmation(new WS_Message_Hardware_set_autobackup().make_request(board_for_update), 1000*3, 0, 4);

            final Form<WS_Message_Hardware_set_autobackup> form = Form.form(WS_Message_Hardware_set_autobackup.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Hardware_set_autobackup: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (TimeoutException e){
            terminal_logger.warn("set_auto_backup: Timeout");
            return new WS_Message_Hardware_set_autobackup();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_set_autobackup();
        }
    }


    ///-- Send Update Procedure to All Hardware's  --//
    @JsonIgnore @Transient public static void execute_update_procedure(Model_ActualizationProcedure procedure){

        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} Start Execution. ", procedure.id );
        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} . Actual state {} ", procedure.id , procedure.state.name());

        if(procedure.state == Enum_Update_group_procedure_state.complete || procedure.state == Enum_Update_group_procedure_state.successful_complete ){
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} is done  (successful_complete or complete) -> Return.", procedure.id);
            return;
        }

        if(procedure.updates.isEmpty()){
            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} is empty and not set to any updates! -> Return." , procedure.id);
            return;
        }

        procedure.state = Enum_Update_group_procedure_state.in_progress;
        procedure.update();


        List<Model_CProgramUpdatePlan> plans = Model_CProgramUpdatePlan.find.where().eq("actualization_procedure.id", procedure.id)
                .disjunction()
                .eq("state", Enum_CProgram_updater_state.not_start_yet)
                .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                .eq("state", Enum_CProgram_updater_state.bin_file_not_found)
                .endJunction()
                .findList();

        if(plans.isEmpty()){
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} all updates is done or in progress. -> Return.", procedure.id );
            return;
        }

        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} . Number of C_Procedures By database for execution:: {}" , procedure.id , plans.size());


        HashMap<String, List<Model_CProgramUpdatePlan> > server_device_sort = new HashMap<>();


        for (Model_CProgramUpdatePlan plan : plans) {
            try {

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: ID:: {} - New Cycle" , procedure.id , plan.id);
                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Board ID:: {}" , procedure.id , plan.id,  plan.board.id);
                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Status:: {} ", procedure.id , plan.id,  plan.state);

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Number of tries  ", procedure.id , plan.id,  plan.count_of_tries);

                if( plan.count_of_tries > 5 ){
                    plan.state = Enum_CProgram_updater_state.critical_error;
                    plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                    plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                    plan.update();
                    terminal_logger.warn("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                    continue;
                }


                if(!server_device_sort.containsKey(plan.board.connected_server_id)){
                    server_device_sort.put(plan.board.connected_server_id, new ArrayList<Model_CProgramUpdatePlan>());
                }

                if(!Model_HomerServer.get_byId(plan.board.connected_server_id).server_is_online()){
                    terminal_logger.warn("actualization_update_procedure:: Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", procedure.id , plan.id, Model_HomerServer.get_byId(plan.board.connected_server_id).personal_server_name);
                    plan.state = Enum_CProgram_updater_state.homer_server_is_offline;
                    plan.update();
                    continue;
                }

                server_device_sort.get(plan.board.connected_server_id).add(plan);

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {}  plan {} of blocko program is online and connected with Tyrion", procedure.id, plan.id);

                plan.state = Enum_CProgram_updater_state.in_progress;
                plan.update();

            } catch(Exception e) {
                terminal_logger.internalServerError("actualization_update_procedure:", e);
                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.update();
                break;
            }
        }

        terminal_logger.debug("Summary for actualizations");

        //  new Thread(procedure::notification_update_procedure_start).start();

        for(String server_id : server_device_sort.keySet()){

            List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

            for(Model_CProgramUpdatePlan plan : server_device_sort.get(server_id)){
                tasks.add(plan.get_brief_for_update_homer_server());
            }

            Model_HomerServer.get_byId(server_id).update_devices_firmware(tasks);
        }

    }

    
/* CHECK BOARD RIGHT FIRMWARE || BACKUP || BOOTLOADER STATUS -----------------------------------------------------------*/

    // Kontrola up_to_date harwaru
    @JsonIgnore @Transient  public void hardware_firmware_state_check() {
    try {

        WS_Message_Hardware_overview report = get_devices_overview();

        if(report.error != null){

            terminal_logger.debug("hardware_firmware_state_check: Report Device ID: {} contains ErrorCode:: {} ErrorMessage:: {} " , this.id, report.error_code, report.error);

            if(report.error_code == ErrorCode.YODA_IS_OFFLINE.error_code() || report.error_code == ErrorCode.DEVICE_IS_NOT_ONLINE.error_code()){
                terminal_logger.debug("hardware_firmware_state_check:: Report Device ID: {} is offline" , this.id);
                return;
            }

        }

        WS_Message_Hardware_overview.WS_Help_Hardware_board_overview overview = report.get_device_from_list(this.id);

        terminal_logger.debug("hardware_firmware_state_check: Summary information of connected master board: ID = " + this.id);
        terminal_logger.debug("hardware_firmware_state_check: Actual firmware_id from HW = " + overview.firmware_build_id);


        if (overview.firmware_build_id == null){

            terminal_logger.debug("hardware_firmware_state_check: Actual firmware_id is not recognized by the DB - Device has not firmware from Tyrion");
            terminal_logger.debug("hardware_firmware_state_check: Tyrion will try to find Default C Program Main Version for this type of hardware. If is it set, Tyrion will update device to starting state");

            // Nastavím default firmware
            if(type_of_board.version_scheme.default_main_version != null){

                terminal_logger.debug("hardware_firmware_state_check: Yes, Default Version for Type Of Device {} is set", type_of_board.name);

                List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.board = this;
                b_pair.c_program_version = this.type_of_board.version_scheme.default_main_version;

                b_pairs.add(b_pair);

                this.notification_board_not_databased_version();

                Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.FIRMWARE, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                procedure.execute_update_procedure();

                return;

            }else {
                terminal_logger.internalServerError(new Exception("Default main code version is not set for Type Of Board " + this.type_of_board.name));
            }

        } else {
            terminal_logger.debug("hardware_firmware_state_check: Actual firmware_id by DB:: " + this.actual_c_program_version.c_compilation.firmware_build_id);
        }

        if (this.actual_boot_loader == null)       terminal_logger.debug("hardware_firmware_state_check:: Actual bootloader_id by DB not recognized :: " + overview.bootloader_build_id);
        else terminal_logger.debug("hardware_firmware_state_check: Actual bootloader_id by DB: " + this.actual_boot_loader.version_identificator);

        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy

        List<Model_CProgramUpdatePlan> firmware_plans = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id)
                .disjunction()
                .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                .endJunction()
                .disjunction()
                .add(Expr.eq("firmware_type", Enum_Firmware_type.FIRMWARE.name()))
                .add(Expr.eq("firmware_type", Enum_Firmware_type.BACKUP.name()))
                .add(Expr.eq("firmware_type", Enum_Firmware_type.BOOTLOADER.name()))
                .findList();

        terminal_logger.debug("hardware_firmware_state_check: Total CProgramUpdatePlans for this device (not complete): " + firmware_plans.size());
        terminal_logger.debug("hardware_firmware_state_check: Message form Homer: " + Json.toJson(report));

        if(firmware_plans.size() == 0){

            terminal_logger.debug("hardware_firmware_state_check: There is no Active Updates for Firmware, Backup or Bootloader");
            terminal_logger.debug("hardware_firmware_state_check: But its time ti check actual versions for database collisions ");

            // Firmware
            terminal_logger.debug("hardware_firmware_state_check: First Check Firmware! ");
            if(overview.firmware_build_id != null) // Hází to null pointer exception při náběhu instancí na homerovi a touhle stupidní kaskádou se hledalo na čem
                if(this.actual_c_program_version != null)
                    if(this.actual_c_program_version.c_compilation != null)
                        if(!overview.firmware_build_id.equals( this.actual_c_program_version.c_compilation.firmware_build_id)){

                            terminal_logger.debug("hardware_firmware_state_check: Different firmware versions versus database");

                            // Nejpravděpodobnější varianta proč tam není správná verze je nasazený Backup
                            // Pokud je aktuální backup podle DB shodný firmwarem na hardwaru - měl bych oznámit kritickou chybu uživatelovi
                            // A zároven označit firmware verzi za nestabilní
                            // Nastavit aktuální verzi firmwaru na backup který naběhnul

                            if(this.actual_backup_c_program_version.id.equals(overview.bootloader_build_id)){

                                terminal_logger.warn("hardware_firmware_state_check: We have problem with firmware version. Backup is now running");

                                // Notifikace uživatelovi
                                this.notification_board_unstable_actual_firmware_version(this.actual_c_program_version);

                                // Označit firmare za nestabilní
                                this.actual_c_program_version.c_compilation.status = Enum_Compile_status.hardware_unstable;
                                this.actual_c_program_version.c_compilation.update();

                                // Přemapovat hardware
                                this.actual_c_program_version = this.actual_backup_c_program_version;
                                this.update();

                                return;
                            }

                            // Backup to není
                            else {

                                terminal_logger.warn("hardware_firmware_state_check::     Wrong version on hardware - or null version on hardware");
                                terminal_logger.warn("hardware_firmware_state_check::     Now System set Default Firmware or Firmware by Database!!!");

                                // Nastavuji nový systémový update
                                List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                                b_pair.board = this;
                                b_pair.c_program_version = this.actual_c_program_version;

                                b_pairs.add(b_pair);

                                Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.FIRMWARE, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                                procedure.execute_update_procedure();

                                return;
                            }
                        }

            // Bootloader
            terminal_logger.debug("hardware_firmware_state_check::     Second Check Bootloader! ");
            if(overview.bootloader_build_id != null)
                if(this.actual_boot_loader != null)
                    if(this.actual_boot_loader != null)
                        if(this.actual_boot_loader.main_type_of_board != null)
                            if(!overview.bootloader_build_id.equals(this.actual_boot_loader.id.toString())){

                                terminal_logger.debug("hardware_firmware_state_check::     Different bootloader on hardware versus database");

                                List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                                b_pair.board = this;
                                b_pair.bootLoader = this.actual_boot_loader;

                                b_pairs.add(b_pair);

                                terminal_logger.debug("hardware_firmware_state_check::     Creating update procedure");
                                Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.BOOTLOADER, Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);
                                procedure.execute_update_procedure();
                            }

            terminal_logger.debug("hardware_firmware_state_check::     Third Check Backup! ");
            if(overview.backup_build_id != null) // Hází to null pointer exception při náběhu instancí na homerovi a touhle stupidní kaskádou se hledalo na čem
                if(this.actual_backup_c_program_version != null)
                    if(this.actual_backup_c_program_version.c_compilation != null)
                        if(!overview.backup_build_id.equals( this.actual_backup_c_program_version.c_compilation.firmware_build_id)){

                            terminal_logger.debug("hardware_firmware_state_check::     Inconsistent backup state on hardware with Database - Start new Update Procedure");

                            // Nastavuji nový systémový update
                            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                            WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                            b_pair.board = this;
                            b_pair.c_program_version = this.actual_c_program_version;

                            b_pairs.add(b_pair);

                            Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.BACKUP, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                            procedure.execute_update_procedure();
                            return;
                        }
            return;
        }


            /*
             * Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
             * To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
             * Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
             * Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
             */

        if (firmware_plans.size() > 1) {

            terminal_logger.debug("hardware_firmware_state_check::  there is still some not finished firmware_plans");

            // Kontrola Firmwaru
            int pointer = -1;

            for (int i = 0; i < firmware_plans.size(); i++) {

                if(pointer == -1 && firmware_plans.get(i).firmware_type == Enum_Firmware_type.FIRMWARE){
                    pointer = i;
                    continue;
                }

                if(firmware_plans.get(i).firmware_type  == Enum_Firmware_type.FIRMWARE){
                    firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                    firmware_plans.get(i).update();
                }
            }

            pointer = -1;

            // Kontrola Bootloader
            for (int i = 0; i < firmware_plans.size(); i++) {

                if(pointer == -1 && firmware_plans.get(i).firmware_type == Enum_Firmware_type.BOOTLOADER){
                    pointer = i;
                    continue;
                }

                if(firmware_plans.get(i).firmware_type  == Enum_Firmware_type.BOOTLOADER){
                    firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                    firmware_plans.get(i).update();
                }
            }

            pointer = -1;

            // Kontrola Backupu
            for (int i = 0; i < firmware_plans.size(); i++) {

                if(pointer == -1 && firmware_plans.get(i).firmware_type == Enum_Firmware_type.BACKUP){
                    pointer = i;
                    continue;
                }

                if(firmware_plans.get(i).firmware_type  == Enum_Firmware_type.BACKUP){
                    firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                    firmware_plans.get(i).update();
                }
            }

            // Projedu seznam a do nového seznamu vložím jen ty se kterými chci nadále pracovat
            List<Model_CProgramUpdatePlan> firmware_separated_plans = new ArrayList<>();
            for(int i = 0;  i < firmware_plans.size(); i++){
                if(firmware_plans.get(0).state != Enum_CProgram_updater_state.overwritten) firmware_separated_plans.add(firmware_plans.get(0));
            }

            firmware_plans = firmware_separated_plans;
        }

        if (firmware_plans.size() > 0) {

            terminal_logger.trace("hardware_firmware_state_check:: Firmware");

            for(Model_CProgramUpdatePlan plan : firmware_plans) {

                // Mám shodu firmwaru oproti očekávánemů
                if(plan.firmware_type == Enum_Firmware_type.FIRMWARE) {

                    if (plan.board.actual_c_program_version != null) {

                        // Verze se rovnají
                        if (plan.board.actual_c_program_version.c_compilation.firmware_build_id.equals(plan.c_program_version_for_update.c_compilation.firmware_build_id)) {

                            terminal_logger.debug("hardware_firmware_state_check:: Firmware versions are equal. Procedure done");
                            plan.state = Enum_CProgram_updater_state.complete;
                            plan.update();

                        } else {

                            terminal_logger.debug("hardware_firmware_state_check:: Firmware versions are not equal, System start with and try the new update");
                            plan.state = Enum_CProgram_updater_state.not_start_yet;
                            terminal_logger.debug("hardware_firmware_state_check:: Firmware versions are not equal, System start with and try the new update. Number of Tries:: {} ", plan.count_of_tries);
                            plan.count_of_tries++;
                            plan.update();
                            execute_update_procedure(plan.actualization_procedure);

                        }

                    } else {

                        terminal_logger.debug("hardware_firmware_state_check:: Firmware versions are not equal because there is no on the hardware at all. System start with a new update");
                        plan.state = Enum_CProgram_updater_state.not_start_yet;
                        plan.count_of_tries++;
                        plan.update();

                        execute_update_procedure(plan.actualization_procedure);
                    }

                    continue;
                }

                if(plan.firmware_type == Enum_Firmware_type.BOOTLOADER){

                    if (plan.board.actual_boot_loader != null) {

                        // Verze se rovnají
                        if (plan.board.actual_boot_loader.version_identificator.equals(plan.bootloader.version_identificator)) {

                            terminal_logger.debug("hardware_firmware_state_check:: Bootloader versions are equal. Procedure done");
                            plan.state = Enum_CProgram_updater_state.complete;
                            plan.update();

                        } else {

                            terminal_logger.debug("hardware_firmware_state_check:: Bootloader versions are not equal, System start with and try the new update");
                            plan.state = Enum_CProgram_updater_state.not_start_yet;
                            terminal_logger.debug("hardware_firmware_state_check:: Bootloader versions are not equal, System start with and try the new update. Number of Tries:: {} ", plan.count_of_tries);
                            plan.count_of_tries++;
                            plan.update();
                            execute_update_procedure(plan.actualization_procedure);
                        }

                    } else {

                        terminal_logger.debug("hardware_firmware_state_check:: Bootloader versions are not equal because there is no on the hardware at all. System start with a new update");
                        plan.state = Enum_CProgram_updater_state.not_start_yet;
                        plan.count_of_tries++;
                        plan.update();

                        execute_update_procedure(plan.actualization_procedure);
                    }

                    continue;
                }

                if(plan.firmware_type == Enum_Firmware_type.BACKUP) {

                    if (plan.board.actual_backup_c_program_version != null) {

                        // Verze se rovnají
                        if (plan.board.actual_backup_c_program_version.c_compilation.firmware_build_id.equals(plan.c_program_version_for_update.c_compilation.firmware_build_id)) {

                            terminal_logger.debug("hardware_firmware_state_check:: Backup versions are equal. Procedure done");
                            plan.state = Enum_CProgram_updater_state.complete;
                            plan.update();

                        } else {

                            terminal_logger.debug("hardware_firmware_state_check:: Backup versions are not equal, System start with and try the new update");
                            plan.state = Enum_CProgram_updater_state.not_start_yet;
                            terminal_logger.debug("hardware_firmware_state_check:: Backup versions are not equal, System start with and try the new update. Number of Tries:: {} ", plan.count_of_tries);
                            plan.count_of_tries++;
                            plan.update();
                            execute_update_procedure(plan.actualization_procedure);

                        }

                    } else {

                        terminal_logger.debug("hardware_firmware_state_check:: Backup versions are not equal because there is no on the hardware at all. System start with a new update");
                        plan.state = Enum_CProgram_updater_state.not_start_yet;
                        plan.count_of_tries++;
                        plan.update();

                        execute_update_procedure(plan.actualization_procedure);
                    }
                }
            }
        }


    }catch (Exception e){
        terminal_logger.internalServerError(e);
    }
}

    @JsonIgnore @Transient private void check_firmware(){
        // TODO
    }

    @JsonIgnore @Transient private void check_backup(){
        // TODO
    }

    @JsonIgnore @Transient private void check_bootloader(){
        // TODO
    }


/* UPDATE --------------------------------------------------------------------------------------------------------------*/

    public static Model_ActualizationProcedure create_update_procedure(Enum_Firmware_type firmware_type, Enum_Update_type_of_update type_of_update, List<WS_Help_Hardware_Pair> board_for_update){

        Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
        procedure.project_id = board_for_update.get(0).board.project_id();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;

        procedure.save();

        for (WS_Help_Hardware_Pair b_pair : board_for_update) {

            List<Model_CProgramUpdatePlan> procedures_for_overriding = Model_CProgramUpdatePlan
                    .find
                    .where()
                    .eq("firmware_type", firmware_type)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .endJunction()
                    .eq("board.id", b_pair.board.id).findList();

            for (Model_CProgramUpdatePlan cProgramUpdatePlan : procedures_for_overriding) {
                cProgramUpdatePlan.state = Enum_CProgram_updater_state.overwritten;
                cProgramUpdatePlan.date_of_finish = new Date();
                cProgramUpdatePlan.update();
            }

            Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
            plan.board = b_pair.board;
            plan.firmware_type = firmware_type;
            plan.actualization_procedure = procedure;


            // Firmware
            if(firmware_type == Enum_Firmware_type.FIRMWARE){
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }

            // Backup
            else if(firmware_type == Enum_Firmware_type.BACKUP){
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }

            // Bootloader
            else if(firmware_type == Enum_Firmware_type.BOOTLOADER){
                plan.bootloader = b_pair.bootLoader;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }


            plan.save();
        }

        procedure.refresh();


        return procedure;

    }


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Online Offline Notification
    @JsonIgnore @Transient
    public void notification_board_connect(){
        new Thread( () -> {
            if (project == null) return;

            try {
                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.success)
                        .setText(new Notification_Text().setText("Device " + this.id))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has just connected"))
                        .send_under_project(project_id());

            } catch (Exception e) {
                terminal_logger.internalServerError("notification_board_connect:", e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_disconnect(){
        new Thread( () -> {
            if(project == null) return;
            // Pokud to není yoda ale device tak neupozorňovat v notifikaci, že je deska offline - zbytečné zatížení
            try{

                new Model_Notification()
                    .setImportance( Enum_Notification_importance.low )
                    .setLevel( Enum_Notification_level.warning)
                    .setText(  new Notification_Text().setText("Device" + this.id ))
                    .setObject(this)
                    .setText( new Notification_Text().setText(" has disconnected."))
                    .send_under_project(project_id());

            }catch (Exception e){
                terminal_logger.internalServerError("notification_board_disconnect:", e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_unstable_actual_firmware_version(Model_VersionObject firmware_version){
        new Thread( () -> {

            // Pokud to není yoda ale device tak neupozorňovat v notifikaci, že je deska offline - zbytečné zatížení
            if(project == null) return;

            try{

                new Model_Notification()
                        .setImportance( Enum_Notification_importance.high)
                        .setLevel(Enum_Notification_level.error)
                        .setText(new Notification_Text().setText("Attention! We note the highest critical error on your device "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" There was a collapse of the running firmware "))
                        .setObject(firmware_version.c_program)
                        .setText(new Notification_Text().setText(" version "))
                        .setObject(firmware_version)
                        .setText(new Notification_Text().setText(". But stay calm. The hardware has successfully restarted and uploaded a backup version. " +
                                "This can cause a data collision in your Blocko Program, but you have the chance to fix the firmware. " +
                                "Incorrect version of Firmware has been flagged as unreliable."))
                        .send_under_project(project_id());

            }catch (Exception e){
                terminal_logger.internalServerError("notification_board_unstable_actual_firmware_version:", e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_not_databased_version(){
        new Thread( () -> {

            // Pokud to není yoda ale device tak neupozorňovat v notifikaci, že je deska offline - zbytečné zatížení
            if(project == null) return;

            try{

                new Model_Notification()
                        .setImportance( Enum_Notification_importance.normal)
                        .setLevel(Enum_Notification_level.info)
                        .setText(new Notification_Text().setText("Attention! Device "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has logged in. Unfortunately, we do not have a synchronized knowledge of the " +
                                "device status of what device firmware is running on. Perhaps this is the factory setting. " +
                                "We are now updating to the default firmware on device."))
                        .setnewLine()
                        .setText(new Notification_Text().setText("You do not have to do anything. Have a nice day."))
                        .setnewLine()
                        .setText(new Notification_Text().setText("Byzance"))
                        .send_under_project(project_id());



            }catch (Exception e){
                terminal_logger.internalServerError("notification_board_unstable_actual_firmware_version:", e);
            }
        }).start();
    }


    // Backup Notification
    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_single(Model_BPair board_for_update){
        try {

            new Thread( () -> {

                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.warning)
                        .setText(new Notification_Text().setText("You set Static Backup program: "))
                        .setObject(board_for_update.c_program_version.c_program)
                        .setText(new Notification_Text().setText(", in Version "))
                        .setObject(board_for_update.c_program_version)
                        .setText(new Notification_Text().setText(" for device "))
                        .setObject(board_for_update.board)
                        .setText(new Notification_Text().setText(". "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.board.project_id());

            }).start();

        } catch (Exception e) {
            terminal_logger.internalServerError("notification_set_static_backup_procedure_first_information_single:", e);
        }
    }

    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_list( List<Model_BPair> board_for_update){
        try {

            new Thread( () -> {

                if( board_for_update.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if( board_for_update.size() == 1 ){
                    notification_set_static_backup_procedure_first_information_single(board_for_update.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.warning)
                        .setText(new Notification_Text().setText("You set Static Backup program for Hardware Collection (" + board_for_update.size() + "). "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.get(0).board.project_id());

            }).start();

        } catch (Exception e) {
            terminal_logger.internalServerError("notification_set_static_backup_procedure_first_information_list:", e);
        }
    }


/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Connect.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_connect:", e);
            }
        }).start();
    }

    public void make_log_disconnect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Disconnected.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_disconnect:", e);
            }
        }).start();
    }

    public void make_log_backup_arrise_change(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_BackupIncident.make_request_success_backup(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_backup_arrise_change:", e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      " - Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public boolean create_permission(){  return  Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  Controller_Security.has_token() && ((project != null && project.update_permission()) || Controller_Security.get_person().has_permission("Board_edit")) ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()  {  return Controller_Security.has_token() && ((project != null && project.read_permission())   || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_read"));
    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  Controller_Security.has_token() && ((project != null && project.update_permission()) || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_delete"));}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  Controller_Security.has_token() && ((project != null && project.update_permission()) || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_update"));}


    public enum permissions {Board_read, Board_Create, Board_edit, Board_delete, Board_update}


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean first_connect_permission(){  return project == null;}

    @Override
    public void save(){

        terminal_logger.debug("save :: Creating new Object");

        //Default starting state
        database_synchronize = true;

        while(true){ // I need Unique Value

            String UUDID = UUID.randomUUID().toString().substring(0,14);
            this.hash_for_adding = UUDID.substring(0, 4) + "-" + UUDID.substring(4, 8) + "-" + UUDID.substring(9, 13);
            if (Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique() == null) break;
        }

        super.save();

        //Cache Update
        cache.put(this.id, this);
    }

    @Override
    public void update(){

        terminal_logger.debug("update :: Update object Id: " + this.id);

        //Cache Update
        cache.put(this.id, this);

        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Board.class, project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override
    public void delete() {
        try {

            if (cache.containsKey(this.id))
                cache.remove((this.id));

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        super.delete();
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_Board.class.getSimpleName();
    public static final String CACHE_STATUS = Model_Board.class.getSimpleName() + "_STATUS";

    public static Cache<String, Model_Board> cache;         // Server_cache Override during server initialization
    public static Cache<String, Boolean> cache_status;      // Server_cache Override during server initialization

    public static Model_Board get_byId(String id){

        Model_Board board_model = cache.get(id);

        if(board_model == null){
            board_model = Model_Board.find.byId(id);

            if (board_model == null) return null;

            cache.put(id, board_model);
        }

        return board_model;
    }

    @JsonIgnore
    public boolean is_online() {

        Boolean status = cache_status.get(id);

        if (status == null){

            terminal_logger.debug("is_online:: Check online status - its not in cache:: " + id);

            try {

                WS_Message_Hardware_online_status result = get_devices_online_state();


                if( result.status.equals("error")){

                    terminal_logger.debug("is_online:: device_id:: "+  id + " Checking online state! Device is offline");
                    cache_status.put(id, false);
                    return false;

                } else if( result.status.equals("success") ){

                    cache_status.put(id, result.is_device_online(id));
                    return false;

                }

                cache_status.put(id, false );
                return false;


            }catch (NullPointerException e){
                cache_status.put(id, false );
                return false;
            }catch (Exception e){
                terminal_logger.internalServerError("is_online:", e);
                return false;
            }
        }else {
            return status;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Board> find = new Finder<>(Model_Board.class);

}
