package models.project.b_program.instnace;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.global.Project;
import play.libs.Json;
import utilities.enums.Firmware_type;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.enums.Type_of_command;
import utilities.swagger.outboundClass.Swagger_B_Program_Instance;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_Instance_HW_Group;
import utilities.webSocket.WebSCType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Entity
public class Homer_Instance extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                  @Id               public String blocko_instance_name;
                             @JsonIgnore @ManyToOne()               public Cloud_Homer_Server cloud_homer_server;
        @JsonIgnore @OneToOne(fetch = FetchType.LAZY)               public Private_Homer_Server private_server; // Nevyužívané



    @JsonIgnore @OneToOne(mappedBy="instance", fetch = FetchType.LAZY)                                  public B_Program b_program;                     //LAZY!! - přes Getter!! // BLocko program ke kterému se Homer Instance váže

    @JsonIgnore @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)                  public Homer_Instance_Record actual_instance; // Aktuálně běžící instnace na Serveru

                @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL) @OrderBy("id ASC") public List<Homer_Instance_Record> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu


    @JsonIgnore                                                                                                         public boolean virtual_instance;
    @JsonIgnore @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)            public Project project;


    @JsonIgnore @OneToMany(mappedBy="virtual_instance_under_project", cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Board> boards_in_virtual_instance = new ArrayList<>();

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_instance_start_upload(Person person){

        new Notification(Notification_importance.low,  Notification_level.info)
                .setText("Server started creating new Blocko Instance of Blocko Version ")
                .setText(this.actual_instance.version_object.b_program.name + " ", "black", true, false, false)
                .setObject(Swagger_B_Program_Version.class, this.actual_instance.version_object.id, this.actual_instance.version_object.version_name, this.actual_instance.version_object.b_program.project_id() )
                .setText(" from Blocko program ")
                .setObject(B_Program.class, this.actual_instance.version_object.b_program.id, this.actual_instance.version_object.b_program.name + ".", this.actual_instance.version_object.b_program.project_id());
    }

    @JsonIgnore @Transient
    public void notification_instance_successful_upload(Person person, Homer_Instance instance){

        new Notification(Notification_importance.low, Notification_level.success)
                .setText("Server created successfully instance in cloud on Blocko Version")
                .setObject(Swagger_B_Program_Version.class, instance.actual_instance.version_object.id, instance.actual_instance.version_object.version_name, instance.actual_instance.version_object.b_program.project_id() )
                .setText("from Blocko program")
                .setObject(B_Program.class, instance.actual_instance.version_object.b_program.id, instance.actual_instance.version_object.b_program.name + ".", instance.actual_instance.version_object.b_program.project_id());
    }

    @JsonIgnore @Transient
    public void notification_instance_unsuccessful_upload(Person person, Homer_Instance instance, String reason){


        new Notification(Notification_importance.normal, Notification_level.warning)
                .setText("Server did not upload instance to cloud on Blocko Version <b>" + instance.actual_instance.version_object.version_name + "</b> from Blocko program <b>" + instance.b_program.name + "</b> for <b> reason:\"" +  reason + "\" </b> ")
                .setObject(Swagger_B_Program_Version.class, instance.actual_instance.version_object.id, instance.actual_instance.version_object.version_name, instance.b_program.project_id() )
                .setText("from Blocko program")
                .setObject(B_Program.class, instance.b_program.id, instance.b_program.name, instance.b_program.project_id() )
                .setText("Server will try to do that as soon as possible.");
    }
/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_id()             {  return this.getB_program().id;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_name()           {  return this.getB_program().name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_description()    {  return this.getB_program().description;}

    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_name()             {  return cloud_homer_server.server_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_id()               {  return cloud_homer_server.id;}

    @Transient @JsonProperty @ApiModelProperty(required = false, value = "Only if instance is upload in Homer - can be null") public Swagger_B_Program_Instance actual_summary() {
        try {

            Swagger_B_Program_Instance instance = new Swagger_B_Program_Instance();

            if(actual_instance != null) {
                instance.instance_record_id = actual_instance.id;
                instance.date_of_created = actual_instance.date_of_created;
                instance.running_from = actual_instance.running_from;
                instance.running_to = actual_instance.running_to;
                instance.planed_when = actual_instance.planed_when;

                instance.b_program_id = actual_instance.version_object.b_program.id;
                instance.b_program_name = actual_instance.version_object.b_program.name;
                instance.b_program_version_name = actual_instance.b_program_version_name();
                instance.b_program_version_id = actual_instance.b_program_version_id();

                instance.hardware_group = actual_instance.version_object.b_program_hw_groups;
                instance.m_project_program_snapshots = actual_instance.version_object.b_program_version_snapshots;
            }

            instance.server_is_online = cloud_homer_server.server_is_online();
            instance.server_name = cloud_homer_server.server_name;
            instance.server_id = cloud_homer_server.id;

            return instance;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


/* JSON IGNORE Standart Method -----------------------------------------------------------------------------------------*/

    @JsonIgnore             public B_Program    getB_program()   { return b_program;}
    @JsonIgnore @Transient  public boolean      instance_online(){ return this.online_state();}
    @JsonIgnore @Transient  public List<Board>  getBoards_in_virtual_instance() { return boards_in_virtual_instance; }

    @JsonIgnore @Transient
    private void setUnique_blocko_instance_name() {
            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (Homer_Instance.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }
    }

    @Override
    public void save(){
        this.setUnique_blocko_instance_name();
        super.save();
    }

    @Override
    public void delete(){
        try {

            this.cloud_homer_server.remove_instance(blocko_instance_name);

        }catch (Exception e){}

        super.delete();
    }





/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER---------------------------------------------------------------------------------*/

    public static String CHANNEL = "tyrion";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient public WebSCType sendToInstance(){return WebSocketController.blocko_servers.get(this.cloud_homer_server.server_name);}

    @JsonIgnore @Transient public  JsonNode getState(){
        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "getState");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);

            return sendToInstance().write_with_confirmation(request, 1000*3, 0, 2);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public void remove_board_from_virtual_instance(Board board){
        try{

            this.boards_in_virtual_instance.remove(board);
            board.virtual_instance_under_project = null;
            board.update();

            if(this.boards_in_virtual_instance.isEmpty()){
                this.remove_instance_to_server();
            }

            update();;

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JsonIgnore @Transient public  JsonNode ping() {

        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "ping_instance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);

            return sendToInstance().write_with_confirmation(request, 1000*3, 0, 2);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode devices_commands(String targetId, Type_of_command command) {
        try{

            ObjectNode request = Json.newObject();
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("messageType", "basicCommand");
            request.put("commandType", command.get_command());
            request.put("targetId", targetId);

            return  sendToInstance().write_with_confirmation(request, 1000*10, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode add_Yoda_to_instance(String yoda_id){
        try{

            if(project != null ){
                // Přidávám Yodu do virutální instance! - skontroluji tedy jestli instance někde běží - virtuální instance
                // nemusí mít žádného Yod, třeba na začátku. Pak nemá smysl aby byla zapnutá.

                if(!instance_online()) {
                    this.add_instance_to_server();
                }
            }


            ObjectNode request = Json.newObject();
            request.put("messageType", "addYodaToInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("yodaId", yoda_id);

            return sendToInstance().write_with_confirmation(request, 1000*3, 0, 4);



        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode remove_Yoda_from_instance(String yoda_id) {
        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "removeYodaFromInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("yodaId", yoda_id);

            return sendToInstance().write_with_confirmation(request, 1000*3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode add_Device_to_instance(String yoda_id, List<String> devices_id){
        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "addDeviceToInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("yodaId", yoda_id);
            request.set("devicesId", Json.toJson(devices_id) );

            return  sendToInstance().write_with_confirmation(request, 1000*3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }

    }

    @JsonIgnore @Transient public  JsonNode remove_Device_from_instance(String yoda_id, List<String> devices_id){
        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "removeDeviceFromInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("yodaId", yoda_id);
            request.set("devicesId", Json.toJson(devices_id));

            return  sendToInstance().write_with_confirmation(request, 1000*3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode add_instance_to_server() {
        try{

            // Vytvořím Instanci
            JsonNode result_instance        = this.cloud_homer_server.add_instance(this);
             //System.err.println("Result instance:: " + result_instance);
            if(!result_instance.get("status").asText().equals("success")) return result_instance;

            // Doplním do ní HW
            JsonNode result_device          = this.update_device_summary_collection();
             //System.err.println("Result device:: " + result_device);
            if(!result_device.get("status").asText().equals("success")) return result_device;

            if(virtual_instance) return result_device; // Virutální instance nemá blocko!

            // Nahraju Blocko Program
            JsonNode result_blocko_program  = this.upload_blocko_program();


            return   result_blocko_program;


        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode remove_instance_to_server() {
        try{

            // Vytvořím Instanci
            JsonNode result_instance        = this.cloud_homer_server.remove_instance(this.blocko_instance_name);
            return result_instance;

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode update_instance_to_actual_instance_record() {
        try{

            // Doplním do ní HW
            JsonNode result_device          = this.update_device_summary_collection();
            if(!result_device.get("status").asText().equals("success")) return result_device;

            // Nahraju Blocko Program
            JsonNode result_blocko_program  = this.upload_blocko_program();
            return   result_blocko_program;

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode upload_blocko_program(){
        try {
            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", actual_instance.version_object.id).eq("file_name", "program.js").findUnique();

            if (fileRecord == null) {
                return RESULT_file_record_not_found();
            }

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageChannel", CHANNEL);
            result.put("instanceId", this.blocko_instance_name);
            result.put("programId", actual_instance.version_object.id);
            result.put("program", fileRecord.get_fileRecord_from_Azure_inString());

            return this.sendToInstance().write_with_confirmation(result, 1000 * 3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode upload_blocko_program(String program_name, String program){
        try {
            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", actual_instance.version_object.id).eq("file_name", "program.js").findUnique();

            if (fileRecord == null) {
                return RESULT_file_record_not_found();
            }

            ObjectNode request = Json.newObject();
            request.put("messageType", "loadProgram");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);
            request.put("programId", program_name);
            request.put("program", program);

            return this.sendToInstance().write_with_confirmation(request, 1000 * 3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode update_device_summary_collection(){
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "addDevices");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", this.blocko_instance_name);

            List<Swagger_Instance_HW_Group> hw_groups = new ArrayList<>();

            if(actual_instance != null) {
                List<B_Program_Hw_Group> hw_group_for_checking = B_Program_Hw_Group.find.where().eq("b_program_version_groups.id", actual_instance.version_object.id).findList();
                if (hw_group_for_checking != null) {
                    for (B_Program_Hw_Group b_program_hw_group : hw_group_for_checking) {

                        if (b_program_hw_group.main_board_pair != null) {

                            Swagger_Instance_HW_Group group = new Swagger_Instance_HW_Group();
                            group.yodaId = b_program_hw_group.main_board_pair.board.id;

                            for (B_Pair pair : b_program_hw_group.device_board_pairs) {
                                group.devicesId.add(pair.board.id);
                            }
                            hw_groups.add(group);
                        }
                    }
                }
            }else if(virtual_instance){

                for(Board board : boards_in_virtual_instance){
                    Swagger_Instance_HW_Group group = new Swagger_Instance_HW_Group();
                    group.yodaId = board.id;
                    hw_groups.add(group);
                }

            }

            request.set("devices", Json.toJson(hw_groups));

            return this.sendToInstance().write_with_confirmation(request, 1000 * 3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  boolean online_state(){
        return this.cloud_homer_server.isInstanceExist(this.blocko_instance_name);
    }

    @JsonIgnore @Transient public  JsonNode get_device_list(){
        try {

            ObjectNode result = Json.newObject();
            result.put("messageType", "getDeviceList");
            result.put("messageChannel", CHANNEL);
            result.put("instanceId", this.blocko_instance_name);

            return sendToInstance().write_with_confirmation(result, 1000*3, 0, 4);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode update_devices_firmware(String actualization_procedure_id, List<String> targetIds, Firmware_type firmware_type, FileRecord record){

        try {
            logger.debug("Homer: " + sendToInstance().identifikator + ", will update Yodas or Devices");

            ObjectNode result = Json.newObject();
            result.put("messageChannel", CHANNEL);
            result.put("instanceId", this.blocko_instance_name);
            result.put("messageType", "updateDevice");
            result.put("actualization_procedure_id", actualization_procedure_id);

            result.put("firmware_type", firmware_type.get_firmwareType());
            result.set("targetIds", Json.toJson(targetIds));

            // Nahrávám Bootloader
            if (record.boot_loader != null) result.put("build_id", record.boot_loader.version_identificator);

            // Nahrávám klasický Firmware
            else result.put("build_id", record.c_compilations_binary_file.firmware_build_id);

            result.put("program", record.get_fileRecord_from_Azure_inString());

            return sendToInstance().write_with_confirmation(result, 1000 * 30, 0, 3);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode add_grid_token(String token){
        try {

            ObjectNode result = Json.newObject();
            result.put("messageType", "add_gridToken");
            result.put("messageChannel", CHANNEL);
            result.put("instanceId", this.blocko_instance_name);
            result.put("gridToken", token);
            result.put("expiration_time", 25 * 1000); // Počet sekund platnosti tokenu na Homer serveru - po této době se token sám smaže a nebude platný pro připojení

            return sendToInstance().write_with_confirmation(result, 1000 * 3, 0, 4);

        } catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public  JsonNode remove_grid_token(String token) throws  ExecutionException, TimeoutException, InterruptedException {
        try {

            ObjectNode result = Json.newObject();
            result.put("messageType", "remove_gridToken");
            result.put("messageChannel", CHANNEL);
            result.put("instanceId", this.blocko_instance_name);
            result.put("gridToken", token);

            return sendToInstance().write_with_confirmation(result, 1000*3, 0, 4);

        } catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }


    // Pomocné objekty - Defaultní zprávy

    @JsonIgnore @Transient  public static JsonNode RESULT_file_record_not_found(){
        try {

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("code", 803);
            result.put("error", "Server is offline");
            return result;

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance> find = new Finder<>(Homer_Instance.class);

}
