package models.project.b_program.instnace;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_Board;
import models.compiler.Model_FileRecord;
import models.notification.Model_Notification;
import models.person.Model_FloatingPersonToken;
import models.person.Model_Person;
import models.project.b_program.Model_BPair;
import models.project.b_program.Model_BProgram;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import models.project.global.Model_Project;
import models.project.global.Model_ProjectParticipant;
import models.project.m_program.Model_GridTerminal;
import play.data.Form;
import play.i18n.Lang;
import utilities.enums.*;
import utilities.hardware_updater.Master_Updater;
import utilities.swagger.documentationClass.Swagger_B_Program_Version_New;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_Instance_HW_Group;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.WebSCType;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageBoard;
import utilities.web_socket.message_objects.homer_instance.Help_object.YodaOnlyHardwareIdList;
import utilities.web_socket.message_objects.homer_instance.*;
import utilities.web_socket.message_objects.homer_tyrion.WS_Destroy_instance;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Entity
@ApiModel(description = "Model of HomerInstance",
        value = "HomerInstance")
public class Model_HomerInstance extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                  @Id               public String blocko_instance_name;
                             @JsonIgnore @ManyToOne()               public Model_HomerServer cloud_homer_server;


    @JsonIgnore @OneToOne(mappedBy="instance",cascade=CascadeType.ALL, fetch = FetchType.LAZY)          public Model_BProgram b_program;                     //LAZY!! - přes Getter!! // BLocko program ke kterému se Homer Instance váže

                @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)                  public Model_HomerInstanceRecord actual_instance; // Aktuálně běžící instnace na Serveru

                @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL) @OrderBy("planed_when DESC") public List<Model_HomerInstanceRecord> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu


    @JsonIgnore                                                                                                         public boolean virtual_instance; // Pokud je vázaná na project (na držení fiktivního HW)
    @JsonIgnore @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)            public Model_Project project;


    @JsonIgnore @OneToMany(mappedBy="virtual_instance_under_project", cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Board> boards_in_virtual_instance = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_id()             {  return this.getB_program().id;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_name()           {  return this.getB_program().name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_description()    {  return this.getB_program().description;}

    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_name()              {  return cloud_homer_server.personal_server_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_id()                {  return cloud_homer_server.unique_identificator;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public boolean instance_online()          {  return this.online_state();}
    @Transient @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online()         {  return cloud_homer_server.server_is_online();}

    @Transient @JsonProperty @ApiModelProperty(required = true) public String instance_remote_url(){

        if(actual_instance != null) {
            return "ws://" + cloud_homer_server.server_url + cloud_homer_server.webView_port + "/" + blocko_instance_name + "/#token";
        }

        return null;
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Instance_Short_Detail get_instance_short_detail(){
        Swagger_Instance_Short_Detail help = new Swagger_Instance_Short_Detail();
        help.id = blocko_instance_name;
        help.b_program_id = getB_program().id;
        help.b_program_name = getB_program().name;
        help.b_program_description = this.getB_program().description;

        help.server_name = cloud_homer_server.unique_identificator;
        help.server_id = cloud_homer_server.unique_identificator;
        help.instance_is_online = online_state();
        help.server_is_online = server_is_online();
        return help;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore             public Model_BProgram getB_program()   { return b_program;}
    @JsonIgnore @Transient  public List<Model_Board>  getBoards_in_virtual_instance() {
            return Model_Board.find.where().eq("virtual_instance_under_project.blocko_instance_name", blocko_instance_name).findList();
    }


/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        while(true){ // I need Unique Value
            this.blocko_instance_name = UUID.randomUUID().toString();
            if (Model_HomerInstance.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
        }

        super.save();
    }

    @Override
    public void delete(){
        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_instance_start_upload(){

        new Model_Notification(Notification_importance.low,  Notification_level.info)
                .setText("Server started creating new Blocko Instance of Blocko Version ")
                .setText(this.actual_instance.version_object.b_program.name + " ", "black", true, false, false)
                .setObject(Swagger_B_Program_Version.class, this.actual_instance.version_object.id, this.actual_instance.version_object.version_name, this.actual_instance.version_object.b_program.project_id() )
                .setText(" from Blocko program ")
                .setObject(Model_BProgram.class, this.actual_instance.version_object.b_program.id, this.actual_instance.version_object.b_program.name + ".", this.actual_instance.version_object.b_program.project_id())
                .send(Controller_Security.getPerson());

    }

    @JsonIgnore @Transient
    public void notification_instance_successful_upload(){

        new Model_Notification(Notification_importance.low, Notification_level.success)
                .setText("Server successfully created the instance of Blocko Version ")
                .setObject(Swagger_B_Program_Version.class, this.actual_instance.version_object.id, this.actual_instance.version_object.version_name, this.actual_instance.version_object.b_program.project_id() )
                .setText(" from Blocko program ")
                .setObject(Model_BProgram.class, this.actual_instance.version_object.b_program.id, this.actual_instance.version_object.b_program.name + ".", this.actual_instance.version_object.b_program.project_id())
                .send(Controller_Security.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_instance_unsuccessful_upload(String reason){


        new Model_Notification(Notification_importance.normal, Notification_level.warning)
                .setText("Server did not upload instance to cloud on Blocko Version ")
                .setText(this.actual_instance.version_object.version_name, "black", true, false, false)
                .setText(" from Blocko program ")
                .setText(this.b_program.name, "black", true, false, false)
                .setText("for reason: ")
                .setText(reason + " ", "black", true, false, false)
                .setObject(Swagger_B_Program_Version.class, this.actual_instance.version_object.id, this.actual_instance.version_object.version_name, this.b_program.project_id() )
                .setText(" from Blocko program ")
                .setObject(Model_BProgram.class, this.b_program.id, this.b_program.name, this.b_program.project_id() )
                .setText(". Server will try to do that as soon as possible.")
                .send(Controller_Security.getPerson()); // TODO jestli bude uživatel přihlášen, když se notifikace odesílá
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_instance(){

        List<Model_Person> receivers = new ArrayList<>();
        for (Model_ProjectParticipant participant : this.project.participants)
            receivers.add(participant.person);

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("New actualization task was added to Task Queue on Version ")
                .setObject(Swagger_B_Program_Version_New.class, this.actual_instance.version_object.id, this.actual_instance.version_object.version_name, this.actual_instance.version_object.b_program.project_id())
                .send(receivers);

    }





/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER---------------------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


    // Messenger
    @JsonIgnore @Transient public static void Messages(WS_HomerServer homer, ObjectNode json){

        try {
            switch (json.get("messageType").asText()) {

                case WS_Device_connected.messageType: {

                    final Form<WS_Device_connected> form = Form.form(WS_Device_connected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Device_connected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.device_Connected(homer, form.get());
                    return;
                }

                case WS_Yoda_connected.messageType: {

                    // Zpracování Json
                    final Form<WS_Yoda_connected> form = Form.form(WS_Yoda_connected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Yoda_connected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.master_device_Connected(homer, form.get());
                    return;
                }


                case WS_Yoda_disconnected.messageType: {

                    final Form<WS_Yoda_disconnected> form = Form.form(WS_Yoda_disconnected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Yoda_disconnected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.master_device_Disconnected(form.get());
                    return;
                }

                case WS_Device_disconnected.messageType: {

                    final Form<WS_Device_disconnected> form = Form.form(WS_Device_disconnected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Device_disconnected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.device_Disconnected(form.get());
                    return;
                }

                case WS_Update_device_firmware.messageType : {

                    final Form<WS_Update_device_firmware> form = Form.form(WS_Update_device_firmware.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Update_device_firmware:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}


                    Model_Board.update_report_from_homer(form.get());
                    return;
                    
                }

                case WS_Get_summary_information.messageType: {

                    final Form<WS_Get_summary_information> form = Form.form(WS_Get_summary_information.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Get_summary_information:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_HomerInstance.summary_information(homer, form.get());
                    return;
                }

                case WS_Grid_token_verification.messageType : {

                    final Form<WS_Grid_token_verification> form = Form.form(WS_Grid_token_verification.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: token_grid_verification:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    WS_Grid_token_verification help = form.get();
                    help.get_instance().cloud_verification_token_GRID(help);

                    return;
                }

                case WS_WebView_token_verification.messageType : {

                    final Form<WS_WebView_token_verification> form = Form.form(WS_WebView_token_verification.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: token_webView_verification:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    WS_WebView_token_verification help = form.get();
                    help.get_instance().cloud_verification_token_WEBVIEW(help);

                    return;
                }



                default: {
                    logger.error("Homer_Instance:: Incoming message:: Chanel tyrion:: not recognize messageType ->" + json.get("messageType").asText());
                    return;
                }

            }

        }catch (Exception e){
            logger.error("Homer_Instance:: Incoming message:: Error", e.getMessage());
        }
    }





    @JsonIgnore @Transient public  WebSCType send_to_instance(){ return Controller_WebSocket.homer_servers.get(this.cloud_homer_server.unique_identificator);}

    @JsonIgnore @Transient public  WS_Instance_status get_instance_status(){
        try{

            JsonNode node =  send_to_instance().write_with_confirmation( new WS_Instance_status().make_request(this), 1000*3, 0, 2);

            final Form<WS_Instance_status> form = Form.form(WS_Instance_status.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Instance_status:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Instance_status();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Instance_status();
        }catch (Exception e){
            logger.error("Model_HomerServer:: get_instance_status:: Error:: ", e);
            return new WS_Instance_status();
        }
    }

    @JsonIgnore @Transient public  WS_Ping_instance ping() {
        try{

            JsonNode node = send_to_instance().write_with_confirmation( new WS_Ping_instance().make_request(this), 1000*3, 0, 2);

            final Form<WS_Ping_instance> form = Form.form(WS_Ping_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Ping_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Ping_instance();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Ping_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: ping:: Error:: ", e);
            return new WS_Ping_instance();
        }
    }

    @JsonIgnore @Transient public  JsonNode devices_commands(String targetId, Type_of_command command) {
        try{

           return send_to_instance().write_with_confirmation(new WS_Basic_command_for_device().make_request(this, targetId, command), 1000*10, 0, 4);

        }catch (TimeoutException e){
            return null;
        }catch (Exception e){
            logger.error("Model_HomerServer:: devices_commands:: Error:: ", e);
            return null;
        }
    }

    @JsonIgnore @Transient public  WS_Add_yoda_to_instance add_Yoda_to_instance(String yoda_id){
        try{

            if(project != null ){
                // Přidávám Yodu do virutální instance! - skontroluji tedy jestli instance někde běží - virtuální instance
                // nemusí mít žádného Yodu, třeba na začátku. Pak nemá smysl aby byla zapnutá.

                if(!instance_online()) {
                    this.add_instance_to_server();
                }
            }

            JsonNode node =  send_to_instance().write_with_confirmation(new WS_Add_yoda_to_instance().make_request(this, yoda_id), 1000*3, 0, 4);

            final Form<WS_Add_yoda_to_instance> form = Form.form(WS_Add_yoda_to_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_yoda_to_instance:: Incoming Json from Homer server has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Add_yoda_to_instance();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Add_yoda_to_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: add_Yoda_to_instance:: Error:: ", e);
            return new WS_Add_yoda_to_instance();
        }
    }

    @JsonIgnore @Transient public  WS_Remove_yoda_from_instance remove_Yoda_from_instance(String yoda_id) {
        try{

            JsonNode node = send_to_instance().write_with_confirmation(new WS_Remove_yoda_from_instance().make_request(this, yoda_id), 1000*3, 0, 4);

            final Form<WS_Remove_yoda_from_instance> form = Form.form(WS_Remove_yoda_from_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Remove_yoda_from_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Remove_yoda_from_instance();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Remove_yoda_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Yoda_from_instance:: Error:: ", e);
            return new WS_Remove_yoda_from_instance();
        }
    }


    @JsonIgnore @Transient public  WS_Remove_yoda_from_instance remove_Yoda_from_instance(Model_Board yoda) {
        try{

            JsonNode node = send_to_instance().write_with_confirmation(new WS_Remove_yoda_from_instance().make_request(this, yoda.id), 1000*3, 0, 4);

            final Form<WS_Remove_yoda_from_instance> form = Form.form(WS_Remove_yoda_from_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Remove_yoda_from_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Remove_yoda_from_instance();}


            if(yoda.virtual_instance_under_project != null){
                Model_HomerInstance virtual_instance_under_project = yoda.virtual_instance_under_project;
                yoda.virtual_instance_under_project = null;
                yoda.update();

                virtual_instance_under_project.refresh();
                if(virtual_instance_under_project.boards_in_virtual_instance.isEmpty()) virtual_instance_under_project.remove_instance_from_server();
            }

            return form.get();

        }catch (TimeoutException e){
            return new WS_Remove_yoda_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Yoda_from_instance:: Error:: ", e);
            return new WS_Remove_yoda_from_instance();
        }
    }

    @JsonIgnore @Transient public WS_Add_device_to_instance add_Device_to_instance(String yoda_id, List<String> devices_id){
        try{

            JsonNode node =   send_to_instance().write_with_confirmation(new WS_Add_device_to_instance().make_request(this, yoda_id, devices_id), 1000*3, 0, 4);

            final Form<WS_Add_device_to_instance> form = Form.form(WS_Add_device_to_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_Device_to_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Add_device_to_instance();}

            return form.get();
        }catch (TimeoutException e){
            return new WS_Add_device_to_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: add_Device_to_instance:: Error:: ", e);
            return new WS_Add_device_to_instance();
        }
    }

    @JsonIgnore @Transient public WS_Remove_device_from_instance remove_Device_from_instance(String yoda_id, List<String> devices_id){
        try {

            JsonNode node = send_to_instance().write_with_confirmation(new WS_Remove_device_from_instance().make_request(this, yoda_id, devices_id), 1000 * 3, 0, 4);

            final Form<WS_Remove_device_from_instance> form = Form.form(WS_Remove_device_from_instance.class).bind(node);
            if (form.hasErrors()) {
                logger.error("Model_HomerServer:: WS_Remove_Device_to_instance:: Incoming Json from Homer server has not right Form:: " + form.errorsAsJson(new Lang(new play.api.i18n.Lang("en", "US"))).toString());
                return new WS_Remove_device_from_instance();
            }

            return form.get();

        }catch (TimeoutException e){
            return new WS_Remove_device_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Device_from_instance:: Error:: ", e);
            return new WS_Remove_device_from_instance();
        }
    }

    @JsonIgnore @Transient public  WS_Update_device_summary_collection add_instance_to_server() {
        try{


            // Vytvořím Instanci
            WS_Add_new_instance result_instance   = this.cloud_homer_server.add_instance(this);
            if(!result_instance.status.equals("success")) return new WS_Update_device_summary_collection();


            // Doplním do ní HW
            this.update_device_summary_collection();


            if(virtual_instance){

                WS_Update_device_summary_collection ws_update_device_summary_collection = new WS_Update_device_summary_collection();
                ws_update_device_summary_collection.status = "success";
                return ws_update_device_summary_collection; // Virutální instance nemá blocko!
            }



            // Nahraju Blocko Program
            WS_Upload_blocko_program result_blocko_program  = this.upload_blocko_program();
            if(!result_blocko_program.status.equals("success")) return new WS_Update_device_summary_collection();

            WS_Update_device_summary_collection response = new WS_Update_device_summary_collection();
            response.status = "success";

            return   response;


        }catch (Exception e){
            logger.error("Model_HomerServer:: add_instance_to_server:: Error:: ", e);
            return new WS_Update_device_summary_collection();
        }
    }

    @JsonIgnore @Transient public  WS_Destroy_instance remove_instance_from_server() {
        try{

            if(!actual_instance.hardware_group().isEmpty()){

                for(Model_BProgramHwGroup group : actual_instance.hardware_group()){

                    remove_Yoda_from_instance(group.main_board_pair.board.id);

                    group.main_board_pair.board.virtual_instance_under_project = group.main_board_pair.board.project.private_instance;
                    group.main_board_pair.board.update();

                    group.main_board_pair.board.project.private_instance.add_Yoda_to_instance(group.main_board_pair.board_id());

                }
            }

            if(!instance_online()) return new WS_Destroy_instance();

            // Vytvořím Instanci
            WS_Destroy_instance result_instance  = this.cloud_homer_server.remove_instance(this.blocko_instance_name);

            if(this.actual_instance != null) {
                this.actual_instance.running_to = new Date();
                this.actual_instance.actual_running_instance = null;
                this.actual_instance.update();
            }

            this.refresh();

            return result_instance;

        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_instance_from_server:: Error:: ", e);
            return new WS_Destroy_instance();
        }
    }

    @JsonIgnore @Transient public static void upload_Record_immediately(Model_HomerInstanceRecord record) {

        Thread upload_record= new Thread() {
            @Override
            public void run() {

                try {
                    logger.debug("Model_HomerInstance:: upload_Record_immediately:: thread is running under record ID:: " + record.id);

                    Model_HomerInstance instance = record.main_instance_history;


                    if( instance.actual_instance != null) {

                        logger.debug("Model_HomerInstance:: upload_Record_immediately:: Record overwriting previous instance record:: " + instance.actual_instance .id);

                        instance.actual_instance.running_to = new Date();
                        instance.actual_instance.actual_running_instance = null;
                        instance.actual_instance.update();
                    }else {
                        logger.debug("Model_HomerInstance:: upload_Record_immediately:: First uploading of instnace:: ");
                    }

                    instance.actual_instance = record;
                    record.actual_running_instance = instance;
                    record.update();
                    instance.update();

                    for(Model_BProgramHwGroup group : record.hardware_group()){

                        // Kontrola Yody
                        if(group.main_board_pair != null ) {

                            Model_Board yoda = group.main_board_pair.board;

                            //1. Pokud už běží v jiné instanci mimo vlastní dočasnou instnaci
                            if (yoda.virtual_instance_under_project != null) {
                                yoda.virtual_instance_under_project.remove_Yoda_from_instance(yoda);
                            }
                        }
                    }


                        // Ověřím připojený server
                    if (!Controller_WebSocket.homer_servers.containsKey(instance.cloud_homer_server.unique_identificator)) {
                        logger.warn("Server je offline!! Upload instance will be as soon as possible!!");
                        return;
                    }

                    // Server je připojený
                    try {

                        // Na instanci zavolám nastavení na aktuální Record
                        instance.update_instance_to_actual_instance_record();

                    } catch (Exception e) {
                        logger.error("Error while upload_Record_immediately tried upload Instance Record to Homer", e);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        upload_record.start();
    }


    // TODO tuto metodu budu volat ve chvíli kdy nějakou časovu známkou prohodím verze - podle času uživatele
    @JsonIgnore @Transient public  WS_Update_instance_to_actual_instance_record update_instance_to_actual_instance_record() {
        try{

            // Zkontroluji jestli běží server
            if(!server_is_online()) {
                logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: Server is offline and some procedure called update_instance_to_actual_instance_record");

                WS_Update_instance_to_actual_instance_record response = new  WS_Update_instance_to_actual_instance_record();
                response.status = "error";
                response.error = "Server is offline";

                return response;
            }

            if(!instance_online()){

                // Vytvořím Instanci
                WS_Add_new_instance result_instance   = this.cloud_homer_server.add_instance(this);
                if(!result_instance.status.equals("success")){

                    logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: Add Instance Failed:: Error:: " + result_instance.error + " ErrorCode:: "+ result_instance.errorCode);
                    WS_Update_instance_to_actual_instance_record response = new  WS_Update_instance_to_actual_instance_record();
                    response.status = result_instance.status;
                    response.error =  result_instance.error;
                    response.errorCode =  result_instance.errorCode;
                    return response;
                }

            }


            // Doplním respektive vymažu HW, který by měl nebo má být v instanci
            this.update_device_summary_collection();


            // Nahraju Blocko Program
            WS_Upload_blocko_program result_blocko_program  = this.upload_blocko_program();

            if(!result_blocko_program.status.equals("success")){

                logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: upload_blocko_program() Failed:: Error:: " + result_blocko_program.error + " ErrorCode:: " + result_blocko_program.errorCode);

                WS_Update_instance_to_actual_instance_record response = new  WS_Update_instance_to_actual_instance_record();
                response.status = result_blocko_program.status;
                response.error =  result_blocko_program.error;
                response.errorCode =  result_blocko_program.errorCode;
                return response;

            }

            actual_instance.create_actualization_request();
            cloud_homer_server.get_server_webSocket_connection().check_update_for_hw_under_homer_ws.add_new_Procedure(this.get_summary_information());

            WS_Update_instance_to_actual_instance_record response = new WS_Update_instance_to_actual_instance_record();
            response.status = "success";

            return response;

        }catch (Exception e){
            return  new WS_Update_instance_to_actual_instance_record();
        }
    }

    @JsonIgnore @Transient public  WS_Upload_blocko_program upload_blocko_program(){
        try {
            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", actual_instance.version_object.id).eq("file_name", "program.js").findUnique();

            if (fileRecord == null) return new WS_Upload_blocko_program();

            JsonNode node = this.send_to_instance().write_with_confirmation(new WS_Upload_blocko_program().make_request(this, fileRecord, actual_instance.version_object.id), 1000 * 3, 0, 4);

            final Form<WS_Upload_blocko_program> form = Form.form(WS_Upload_blocko_program.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Upload_blocko_program:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Upload_blocko_program();}

            return form.get();

        }catch (TimeoutException e){
            return  new WS_Upload_blocko_program();
        }catch (Exception e){
            logger.error("Model_HomerServer:: upload_blocko_program:: Error:: ", e);
            return  new WS_Upload_blocko_program();
        }
    }

    @JsonIgnore @Transient public void update_device_summary_collection(){
        try {

            if(actual_instance != null) {


                // Seznam - který by na instanci měl běžet!
                List<Model_BProgramHwGroup> hardware_groups = Model_BProgramHwGroup.find.where().eq("b_program_version_groups.id", actual_instance.version_object.id).findList();

                // Zkontroluji HW který aktálně v instanci vysí
                WS_Get_Hardware_list summary_information = this.get_hardware_list();

                if(!summary_information.status.equals("success")){
                    logger.error("Model_HomerServer:: update_device_summary_collection:: Get Instance status Failed:: Error:: " + summary_information.error + " ErrorCode:: "+ summary_information.errorCode);
                    return;
                }

                List<String> yodas_id_on_instance = new ArrayList<>();

                for(YodaOnlyHardwareIdList yoda_list : summary_information.hardwareIdList){

                    yodas_id_on_instance.add(yoda_list.deviceId);

                    // Nová instance k nasazení neobsahuje Yodu který v instanci už je!!!
                    if(!this.actual_instance.contains_HW(yoda_list.deviceId)){

                        for(String devicesId : yoda_list.devicesId){

                            //Model_Board device_board = Model_Board.find.byId(devicesId);
                            // Můžu změnit nějaký parametr tohoto devicu???
                            // Notifikaci???
                            this.remove_Device_from_instance(devicesId, yoda_list.devicesId);
                        }

                        WS_Remove_yoda_from_instance remove_yoda_from_instance =  this.remove_Yoda_from_instance(yoda_list.deviceId);
                        if(!remove_yoda_from_instance.status.equals("success")){
                            logger.error("Model_HomerServer:: update_device_summary_collection:: Remove Yoda Failed:: Error:: " + remove_yoda_from_instance.error + " ErrorCode:: "+ remove_yoda_from_instance.errorCode);
                        }

                        Model_Board master_board = Model_Board.find.byId(yoda_list.deviceId);
                        if(master_board.virtual_instance_under_project == null){
                            master_board.virtual_instance_under_project = master_board.project.private_instance;
                            master_board.virtual_instance_under_project.add_Yoda_to_instance(yoda_list.deviceId);
                            master_board.update();
                        }

                        continue;
                    }


                    Model_BProgramHwGroup group_where_yoda_is = null;

                    for(Model_BProgramHwGroup group :hardware_groups) {
                        if (group.main_board_pair.board.id.equals(yoda_list.deviceId)) {
                            group_where_yoda_is = group;
                            break;
                        }
                    }

                    if(group_where_yoda_is == null) {
                        logger.error("Model_HomerServer:: update_device_summary_collection:: Error::  group_where_yoda_is is Null" );
                        continue; // Tohle by se stát nemělo!
                    }


                    List<String> devicesId_for_removing = new ArrayList<>();

                    for(String deviceId : yoda_list.devicesId){

                        if(!group_where_yoda_is.contains_HW(deviceId)){
                            devicesId_for_removing.add(deviceId);
                        }
                    }

                    // Device je pod špatným Yodou


                    if(devicesId_for_removing.isEmpty()) continue;
                    WS_Remove_device_from_instance remove_device_from_instance = this.remove_Device_from_instance(yoda_list.deviceId,devicesId_for_removing);
                    if(!remove_device_from_instance.status.equals("success")) {
                        logger.error("Model_HomerServer:: update_device_summary_collection:: Remove Yoda Failed:: Error:: " + remove_device_from_instance.error + " ErrorCode:: " + remove_device_from_instance.errorCode);
                    }

                }



                List<Swagger_Instance_HW_Group> hw_groups = new ArrayList<>();

                System.out.println("Budu procházet Grupu k ADD");

                for (Model_BProgramHwGroup b_program_hw_group : hardware_groups) {

                    System.out.println("b_program_hw_group:: " + b_program_hw_group.id);


                    if (b_program_hw_group.main_board_pair != null) {

                        System.out.println("b_program_hw_group:: Main Board " + b_program_hw_group.main_board_pair.board.personal_description);

                        // Neobsahuje Yodu - Tak vytvořím skupinu s Yodou a devicama
                        if(!yodas_id_on_instance.contains(b_program_hw_group.main_board_pair.board_id())) {

                            System.out.println("Instance Neobsahuje Yodu - takže vytvořím skupinu s Yodou " + b_program_hw_group.main_board_pair.board.personal_description);

                            Swagger_Instance_HW_Group group = new Swagger_Instance_HW_Group();
                            group.yodaId = b_program_hw_group.main_board_pair.board.id;

                            for (Model_BPair pair : b_program_hw_group.device_board_pairs) {
                                System.out.println("Instance Neobsahuje Yodu - takže vytvořím skupinu s Yodou a ještě přidám device pod Yodu:: " + pair.board.personal_description);
                                group.devicesId.add(pair.board.id);
                            }

                            hw_groups.add(group);
                            System.out.println("Cyklus opakuji ");
                            continue;
                        }

                        System.out.println("Instance Yodu už má");

                        // Obsahuje Yodu - takže kontroluji ještě Devices
                        List<String> devices_id_under_yoda = new ArrayList<>();

                        YodaOnlyHardwareIdList yodaList = summary_information.getListWithYoda(b_program_hw_group.main_board_pair.board_id());
                        if(yodaList == null){
                            logger.error("Model_HomerServer:: update_device_summary_collection:: Error::  yodaList is Null" );
                            continue;
                        }

                        for(Model_BPair bPair : b_program_hw_group.device_board_pairs){
                            if(!yodaList.devicesId.contains( bPair.board_id())) devices_id_under_yoda.add(bPair.board_id());
                        }

                        if(!devices_id_under_yoda.isEmpty()) add_Device_to_instance(b_program_hw_group.main_board_pair.board_id(), devices_id_under_yoda);
                    }
                }


                if (!hw_groups.isEmpty()) {
                    ObjectNode node = send_to_instance().write_with_confirmation( new WS_Update_device_summary_collection().make_request(this, hw_groups), 1000*3, 0, 4);

                    final Form<WS_Update_device_summary_collection> form = Form.form(WS_Update_device_summary_collection.class).bind(node);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Update_device_summary_collection:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());}

                    WS_Update_device_summary_collection rsp = form.get();
                    if(!rsp.status.equals("success")) {
                        logger.error("Model_HomerServer:: WS_Update_device_summary_collection:: Remove Yoda Failed:: Error:: " + rsp.error + " ErrorCode:: " + rsp.errorCode);
                    }

                }

                // Virutální instance - kontrola Yodů kteřá tam mají být a kteří ne!
            }else if(virtual_instance) {

                List<String> board_id_on_instance = new ArrayList<>();

                // Zkontroluji HW který tam být nemá
                WS_Get_Hardware_list summary_information = get_hardware_list();
                if (!summary_information.status.equals("success")) {
                    logger.error("Model_HomerServer:: update_device_summary_collection:: Get Instance status Failed:: Error:: " + summary_information.error + " ErrorCode:: " + summary_information.errorCode);
                }

                for (YodaOnlyHardwareIdList yoda_list : summary_information.hardwareIdList) {

                    if (!this.actual_instance.contains_HW(yoda_list.deviceId)) {
                        logger.error("Model_HomerServer:: update_device_summary_collection:: Illegal Connected Yoda " + yoda_list.deviceId + " in Virtual instance ");
                        this.remove_Yoda_from_instance(yoda_list.deviceId);
                    }

                    board_id_on_instance.add(yoda_list.deviceId);
                }

                for (Model_Board board : boards_in_virtual_instance) {

                    if (!board_id_on_instance.contains(board.id)) {
                        logger.debug("Model_HomerServer:: update_device_summary_collection:: Missing Connected Yoda " + board.id + " in Virtual instance ");
                        this.add_Yoda_to_instance(board.id);
                    }
                }

            }
        }catch (TimeoutException e){
            return;
        }catch (Exception e){
            logger.error("Model_HomerInstance:: update_device_summary_collection:: Error:: ", e);
            return;
        }
    }

    @JsonIgnore @Transient public  boolean online_state(){
        return this.cloud_homer_server.is_instance_exist(this.blocko_instance_name);
    }

    @JsonIgnore @Transient public  WS_Online_states_devices get_devices_online_state(List<String> device_id){
        try{

            if(!online_state()) return new WS_Online_states_devices();
            JsonNode node = send_to_instance().write_with_confirmation( new WS_Online_states_devices().make_request(this, device_id), 1000 * 5, 0, 3);


            final Form<WS_Online_states_devices> form = Form.form(WS_Online_states_devices.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Online_states_devices:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Online_states_devices();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Online_states_devices();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_devices_online_state: Error:: ", e);
            return new WS_Online_states_devices();
        }
    }

    @JsonIgnore @Transient public  WS_Get_summary_information get_summary_information(){
        try {

            ObjectNode node = send_to_instance().write_with_confirmation(new WS_Get_summary_information().make_request(this), 1000 * 5, 0, 1);

            final Form<WS_Get_summary_information> form = Form.form(WS_Get_summary_information.class).bind(node);
            if (form.hasErrors()) {
                logger.error("Model_HomerInstance:: WS_Get_summary_information: Error:: Some value missing:: " + form.errorsAsJson(new Lang(new play.api.i18n.Lang("en", "US"))).toString());
                throw new Exception("Invalid Json data format");
            }

            return form.get();

        }catch (TimeoutException e){
            return new WS_Get_summary_information();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_summary_information: Error:: ", e);
            return new WS_Get_summary_information();
        }
    }

    @JsonIgnore @Transient public  WS_Get_Hardware_list get_hardware_list(){
        try {

            ObjectNode node = send_to_instance().write_with_confirmation( new WS_Get_Hardware_list().make_request(this), 1000*5, 0, 1);

            final Form<WS_Get_Hardware_list> form = Form.form(WS_Get_Hardware_list.class).bind(node);
            if (form.hasErrors()) {logger.error("Model_HomerInstance:: get_hardware_list: Error:: Some value missing:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); throw new Exception("Invalid Json data format");}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Get_Hardware_list();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_hardware_list: Error:: ", e);
            return new WS_Get_Hardware_list();
        }
    }

    @JsonIgnore @Transient public  static void summary_information(WS_HomerServer homer_server , WS_Get_summary_information summary_information){
        try {


            homer_server.check_update_for_hw_under_homer_ws.add_new_Procedure(summary_information);

        }catch (Exception e){
            logger.error("Model_HomerInstance:: void summary_information: Error:: ", e);
        }
    }


    @JsonIgnore @Transient public  void check_hardware(Model_Board board, WS_AbstractMessageBoard report){

        if(!virtual_instance) {
            logger.debug("Homer_Instance_Record:: check_hardware:: Found one actualization procedure on ", board.id);
            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.find.where()
                    .eq("board.id", board.id)
                    .eq("actualization_procedure.homer_instance_record.id", actual_instance.id)
                    .disjunction()
                        .add(Expr.eq("state", C_ProgramUpdater_State.not_start_yet))
                        .add(Expr.eq("state", C_ProgramUpdater_State.in_progress))
                        .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                        .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                        .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                    .endJunction().findUnique();

            if (plan.firmware_type == Firmware_type.FIRMWARE) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.c_program_version_for_update.c_compilation.firmware_build_id.equals(report.firmware_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);

                }

            } else if (plan.firmware_type == Firmware_type.BOOTLOADER) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.binary_file.boot_loader.version_identificator.equals(report.bootloader_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);
                }

            } else if (plan.firmware_type == Firmware_type.BACKUP) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Backup");

                plan.state = C_ProgramUpdater_State.complete;
                plan.update();
            }

        }
        else {

            System.out.println("Virutální instanci update zatím nepodporujeme!!!");

        }

    }

    // TOKEN verification
    @JsonIgnore @Transient public  void cloud_verification_token_GRID(WS_Grid_token_verification help){
        try {

            logger.debug("Homer_Instance:: cloud_GRID verification_token::  Checking Token");
            WS_HomerServer server = (WS_HomerServer) Controller_WebSocket.homer_servers.get(server_id());

            Model_GridTerminal terminal = Model_GridTerminal.find.where().eq("terminal_token", help.token).findUnique();

            if(terminal == null){
                logger.warn("Homer_Instance:: cloud_verification_token:: Grid_Terminal object not found!");
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                return;
            }

            Integer size;

            if(terminal.person == null) {
                logger.debug("Homer_Instance:: cloud_verification_token:: Grid_Terminal object has not own Person - its probably public - Trying to find Instance");
                size = Model_HomerInstance.find.where().eq("blocko_instance_name", help.instanceId).eq("actual_instance.version_object.public_version", true).findRowCount();
            }else {
                logger.debug("Homer_Instance:: cloud_verification_token:: Grid_Terminal object has  own Person - its probably private or it can be public - Trying to find Instance with user ID and public value");
                size = Model_HomerInstance.find.where().eq("blocko_instance_name", help.instanceId)
                            .disjunction()
                                .eq("b_program.project.participants.person.id", terminal.person.id)
                                .eq("actual_instance.version_object.public_version", true)
                            .findRowCount();
            }

            if(size == 0){
                logger.warn("Homer_Instance:: cloud_verification_token:: Token found but this user has not permission!");
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                return;
            }

            logger.debug("Cloud_Homer_server:: cloud_verification_token:: Token found and user have permission");
            Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(true));
            return;

        }catch (Exception e){
            e.printStackTrace();
            logger.warn("Cloud Homer server", server_id(), " is offline!");
        }

    }
    @JsonIgnore @Transient public  void cloud_verification_token_WEBVIEW(WS_WebView_token_verification help){
        try {

            logger.debug("Homer_Instance:: cloud_verification_token:: WebView  Checking Token");

            Model_FloatingPersonToken floatingPersonToken = Model_FloatingPersonToken.find.where().eq("authToken", help.token).findUnique();

            if(floatingPersonToken == null){
                logger.warn("Homer_Instance:: cloud_verification_token:: FloatingPersonToken not found!");
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                return;
            }

            logger.debug("Cloud_Homer_server:: cloud_verification_token:: WebView FloatingPersonToken Token found and user have permission");
            Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(true));
            return;

        }catch (Exception e){
            e.printStackTrace();
            logger.warn("Cloud Homer server", server_id(), " is offline!");
        }

    }






/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_HomerInstance> find = new Finder<>(Model_HomerInstance.class);

}
