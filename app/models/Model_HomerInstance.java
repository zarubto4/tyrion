package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import utilities.Server;
import utilities.enums.*;
import utilities.hardware_updater.Utilities_HW_Updater_Master_thread_updater;
import utilities.loggy.Loggy;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_Instance_HW_Group;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import web_socket.services.WS_HomerServer;
import web_socket.services.WS_Interface_type;
import web_socket.message_objects.homer_instance.helps_objects.WS_Message_Help_Yoda_only_hardware_Id_list;
import web_socket.message_objects.homer_instance.*;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Destroy_instance;

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


    @JsonIgnore @OneToOne(mappedBy="instance",cascade=CascadeType.ALL, fetch = FetchType.LAZY) public Model_BProgram b_program;                   //LAZY!! - přes Getter!! // BLocko program ke kterému se Homer Instance váže

                @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)         public Model_HomerInstanceRecord actual_instance;  // Aktuálně běžící instnace na Serveru

                @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL) @OrderBy("planed_when DESC") public List<Model_HomerInstanceRecord> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu

                                                                                                                   public Enum_Homer_instance_type instance_type;

    @JsonIgnore @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)       public Model_Project project;


    @JsonIgnore @OneToMany(mappedBy="virtual_instance_under_project", cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Board> boards_in_virtual_instance = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_id()             {  return  ( instance_type != Enum_Homer_instance_type.VIRTUAL) ? this.getB_program().id           : null;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_name()           {  return  ( instance_type != Enum_Homer_instance_type.VIRTUAL) ? this.getB_program().name         : null;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_description()    {  return  ( instance_type != Enum_Homer_instance_type.VIRTUAL) ? this.getB_program().description  : null;}

    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_name()              {  return cloud_homer_server.personal_server_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_id()                {  return cloud_homer_server.unique_identificator;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public boolean instance_online()          {  return this.online_state();}
    @Transient @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online()         {  return cloud_homer_server.server_is_online();}

    @Transient @JsonProperty @ApiModelProperty(required = true) public String instance_remote_url(){
        try {

            if(actual_instance != null) {

                if(Server.server_mode  == Enum_Tyrion_Server_mode.developer) {
                    return "ws://" + cloud_homer_server.server_url + ":" + cloud_homer_server.webView_port + "/" + blocko_instance_name + "/#token";
                }else{
                    return "wss://" + cloud_homer_server.server_url + ":" + cloud_homer_server.webView_port + "/" + blocko_instance_name + "/#token";
                }

            }

            return null;

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: instance_remote_url", e);
            return null;
        }
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Instance_Short_Detail get_instance_short_detail(){
        try {

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

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: get_instance_short_detail", e);
            return null;
        }
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
        try {

        new Model_Notification()
                .setImportance(Enum_Notification_importance.low)
                .setLevel(Enum_Notification_level.info)
                .setText( new Notification_Text().setText("Server started creating new Blocko Instance of Blocko Version "))
                .setText( new Notification_Text().setText(this.actual_instance.version_object.b_program.name).setBoltText())
                .setObject(this.actual_instance.version_object)
                .setText( new Notification_Text().setText(" from Blocko program "))
                .setObject(this.actual_instance.version_object.b_program)
                .send(Controller_Security.getPerson());

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: notification_instance_start_upload", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_instance_successful_upload(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Server successfully created the instance of Blocko Version "))
                    .setObject(this.actual_instance.version_object)
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.actual_instance.version_object.b_program)
                    .send_under_project(project.id);

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: notification_instance_start_upload", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_instance_unsuccessful_upload(String reason){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.warning)
                    .setText( new Notification_Text().setText("Server did not upload instance to cloud on Blocko Version "))
                    .setText( new Notification_Text().setText(this.actual_instance.version_object.version_name ).setBoltText())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setText( new Notification_Text().setText(this.b_program.name).setBoltText())
                    .setText( new Notification_Text().setText(" for reason: ").setBoltText() )
                    .setText( new Notification_Text().setText(reason + " ").setBoltText())
                    .setObject(this.actual_instance.version_object)
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.b_program)
                    .setText( new Notification_Text().setText(". Server will try to do that as soon as possible."))
                    .send_under_project(project.id);

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: notification_instance_start_upload", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_instance(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.info)
                    .setText( new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                    .setObject(this.actual_instance.version_object)
                    .send_under_project(b_program.project_id());

        }catch (Exception e){
            Loggy.internalServerError("Model_HomerInstance:: notification_instance_start_upload", e);
        }
    }





/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER---------------------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


    // Messenger
    @JsonIgnore @Transient
    public static void Messages(WS_HomerServer homer, ObjectNode json){

        try {
            switch (json.get("messageType").asText()) {

                case WS_Message_Device_connected.messageType: {

                    final Form<WS_Message_Device_connected> form = Form.form(WS_Message_Device_connected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Device_connected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.device_Connected(homer, form.get());
                    return;
                }

                case WS_Message_Yoda_connected.messageType: {

                    // Zpracování Json
                    final Form<WS_Message_Yoda_connected> form = Form.form(WS_Message_Yoda_connected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Yoda_connected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.master_device_Connected(homer, form.get());
                    return;
                }


                case WS_Message_Yoda_disconnected.messageType: {

                    final Form<WS_Message_Yoda_disconnected> form = Form.form(WS_Message_Yoda_disconnected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Yoda_disconnected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.master_device_Disconnected(form.get());
                    return;
                }

                case WS_Message_Device_disconnected.messageType: {

                    final Form<WS_Message_Device_disconnected> form = Form.form(WS_Message_Device_disconnected.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Device_disconnected:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_Board.device_Disconnected(form.get());
                    return;
                }


                case WS_Message_UpdateProcedure_progress.messageType : {

                    final Form<WS_Message_UpdateProcedure_progress> form = Form.form(WS_Message_UpdateProcedure_progress.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_UpdateProcedure_progress:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_CProgramUpdatePlan.update_procedure_progress(form.get());
                    return;
                }

                case WS_Message_Update_device_firmware.messageType : {

                    final Form<WS_Message_Update_device_firmware> form = Form.form(WS_Message_Update_device_firmware.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Update_device_firmware:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}


                    Model_Board.update_report_from_homer(form.get());
                    return;
                    
                }

                case WS_Message_Get_summary_information.messageType: {

                    final Form<WS_Message_Get_summary_information> form = Form.form(WS_Message_Get_summary_information.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Get_summary_information:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    Model_HomerInstance.summary_information(homer, form.get());
                    return;
                }

                case WS_Message_Grid_token_verification.messageType : {

                    final Form<WS_Message_Grid_token_verification> form = Form.form(WS_Message_Grid_token_verification.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: WS_Message_Grid_token_verification:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    WS_Message_Grid_token_verification help = form.get();
                    help.get_instance().cloud_verification_token_GRID(help);

                    return;
                }

                case WS_Message_WebView_token_verification.messageType : {

                    final Form<WS_Message_WebView_token_verification> form = Form.form(WS_Message_WebView_token_verification.class).bind(json);
                    if(form.hasErrors()){logger.error("Homer_Instance:: token_webView_verification:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                    WS_Message_WebView_token_verification help = form.get();
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



    @JsonIgnore @Transient
    public WS_Interface_type send_to_instance(){ return Controller_WebSocket.homer_servers.get(this.cloud_homer_server.unique_identificator);}

    @JsonIgnore @Transient
    public WS_Message_Instance_status get_instance_status(){
        try{
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node =  send_to_instance().write_with_confirmation( new WS_Message_Instance_status().make_request(this), 1000*3, 0, 2);

            final Form<WS_Message_Instance_status> form = Form.form(WS_Message_Instance_status.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Instance_status:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Instance_status();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Instance_status();
        }catch (Exception e){
            logger.error("Model_HomerServer:: get_instance_status:: Error:: ", e);
            return new WS_Message_Instance_status();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Ping_instance ping() {
        try{
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = send_to_instance().write_with_confirmation( new WS_Message_Ping_instance().make_request(this), 1000*3, 0, 2);

            final Form<WS_Message_Ping_instance> form = Form.form(WS_Message_Ping_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Ping_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Ping_instance();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Ping_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: ping:: Error:: ", e);
            return new WS_Message_Ping_instance();
        }
    }

    @JsonIgnore @Transient
    public JsonNode devices_commands(String targetId, Enum_type_of_command command) {
        try{
            if(!server_is_online()) throw new InterruptedException();
           return send_to_instance().write_with_confirmation(new WS_Message_Basic_command_for_device().make_request(this, targetId, command), 1000*10, 0, 4);

        }catch (InterruptedException|TimeoutException e){
            return null;
        }catch (Exception e){
            logger.error("Model_HomerServer:: devices_commands:: Error:: ", e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Add_yoda_to_instance add_Yoda_to_instance(String yoda_id){
        try{

            if(project != null ){
                // Přidávám Yodu do virutální instance! - skontroluji tedy jestli instance někde běží - virtuální instance
                // nemusí mít žádného Yodu, třeba na začátku. Pak nemá smysl aby byla zapnutá.

                if(!instance_online()) {
                    this.add_instance_to_server();
                }
            }

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node =  send_to_instance().write_with_confirmation(new WS_Message_Add_yoda_to_instance().make_request(this, yoda_id), 1000*3, 0, 4);

            final Form<WS_Message_Add_yoda_to_instance> form = Form.form(WS_Message_Add_yoda_to_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_yoda_to_instance:: Incoming Json from Homer server has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Add_yoda_to_instance();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Add_yoda_to_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: add_Yoda_to_instance:: Error:: ", e);
            return new WS_Message_Add_yoda_to_instance();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Remove_yoda_from_instance remove_Yoda_from_instance(String yoda_id) {
        try{

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = send_to_instance().write_with_confirmation(new WS_Message_Remove_yoda_from_instance().make_request(this, yoda_id), 1000*3, 0, 4);

            final Form<WS_Message_Remove_yoda_from_instance> form = Form.form(WS_Message_Remove_yoda_from_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Remove_yoda_from_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Remove_yoda_from_instance();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Remove_yoda_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Yoda_from_instance:: Error:: ", e);
            return new WS_Message_Remove_yoda_from_instance();
        }
    }


    @JsonIgnore @Transient
    public WS_Message_Remove_yoda_from_instance remove_Yoda_from_instance(Model_Board yoda) {
        try{

            if(!server_is_online()) throw new InterruptedException();

            JsonNode node = send_to_instance().write_with_confirmation(new WS_Message_Remove_yoda_from_instance().make_request(this, yoda.id), 1000*3, 0, 4);

            final Form<WS_Message_Remove_yoda_from_instance> form = Form.form(WS_Message_Remove_yoda_from_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Remove_yoda_from_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Remove_yoda_from_instance();}


            if(yoda.virtual_instance_under_project != null){
                Model_HomerInstance virtual_instance_under_project = yoda.virtual_instance_under_project;
                yoda.virtual_instance_under_project = null;
                yoda.update();

                virtual_instance_under_project.refresh();
                if(virtual_instance_under_project.boards_in_virtual_instance.isEmpty()) virtual_instance_under_project.remove_instance_from_server();
            }

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Remove_yoda_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Yoda_from_instance:: Error:: ", e);
            return new WS_Message_Remove_yoda_from_instance();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Add_device_to_instance add_Device_to_instance(String yoda_id, List<String> devices_id){
        try{

            if(!server_is_online()) throw new InterruptedException();

            JsonNode node =   send_to_instance().write_with_confirmation(new WS_Message_Add_device_to_instance().make_request(this, yoda_id, devices_id), 1000*3, 0, 4);

            final Form<WS_Message_Add_device_to_instance> form = Form.form(WS_Message_Add_device_to_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_Device_to_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Add_device_to_instance();}

            return form.get();
        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Add_device_to_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: add_Device_to_instance:: Error:: ", e);
            return new WS_Message_Add_device_to_instance();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Remove_device_from_instance remove_Device_from_instance(String yoda_id, List<String> devices_id){
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = send_to_instance().write_with_confirmation(new WS_Message_Remove_device_from_instance().make_request(this, yoda_id, devices_id), 1000 * 3, 0, 4);

            final Form<WS_Message_Remove_device_from_instance> form = Form.form(WS_Message_Remove_device_from_instance.class).bind(node);
            if (form.hasErrors()) {
                logger.error("Model_HomerServer:: WS_Remove_Device_to_instance:: Incoming Json from Homer server has not right Form:: " + form.errorsAsJson(new Lang(new play.api.i18n.Lang("en", "US"))).toString());
                return new WS_Message_Remove_device_from_instance();
            }

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Remove_device_from_instance();
        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_Device_from_instance:: Error:: ", e);
            return new WS_Message_Remove_device_from_instance();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Update_device_summary_collection add_instance_to_server() {
        try{


            // Vytvořím Instanci
            WS_Message_Add_new_instance result_instance   = this.cloud_homer_server.add_instance(this);
            if(!result_instance.status.equals("success")) return new WS_Message_Update_device_summary_collection();


            // Doplním do ní HW
            this.update_device_summary_collection();


            if(instance_type == Enum_Homer_instance_type.VIRTUAL){

                WS_Message_Update_device_summary_collection ws_update_device_summary_collection = new WS_Message_Update_device_summary_collection();
                ws_update_device_summary_collection.status = "success";
                return ws_update_device_summary_collection; // Virutální instance nemá blocko!
            }



            // Nahraju Blocko Program
            WS_Message_Upload_blocko_program result_blocko_program  = this.upload_blocko_program();
            if(!result_blocko_program.status.equals("success")) return new WS_Message_Update_device_summary_collection();

            WS_Message_Update_device_summary_collection response = new WS_Message_Update_device_summary_collection();
            response.status = "success";

            return   response;


        }catch (Exception e){
            logger.error("Model_HomerServer:: add_instance_to_server:: Error:: ", e);
            return new WS_Message_Update_device_summary_collection();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Destroy_instance remove_instance_from_server() {
        try{

            if(!actual_instance.hardware_group().isEmpty()){

                for(Model_BProgramHwGroup group : actual_instance.hardware_group()){

                    remove_Yoda_from_instance(group.main_board_pair.board.id);

                    group.main_board_pair.board.virtual_instance_under_project = group.main_board_pair.board.project.private_instance;
                    group.main_board_pair.board.update();

                    group.main_board_pair.board.project.private_instance.add_Yoda_to_instance(group.main_board_pair.board_id());

                }
            }

            if(!instance_online()) return new WS_Message_Destroy_instance();

            // Vytvořím Instanci
            WS_Message_Destroy_instance result_instance  = this.cloud_homer_server.remove_instance(this.blocko_instance_name);

            if(this.actual_instance != null) {
                this.actual_instance.running_to = new Date();
                this.actual_instance.actual_running_instance = null;
                this.actual_instance.update();
            }

            this.refresh();

            return result_instance;

        }catch (Exception e){
            logger.error("Model_HomerServer:: remove_instance_from_server:: Error:: ", e);
            return new WS_Message_Destroy_instance();
        }
    }

    @JsonIgnore @Transient
    public static void upload_Record_immediately(Model_HomerInstanceRecord record) {

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
    @JsonIgnore @Transient
    public WS_Message_Update_instance_to_actual_instance_record update_instance_to_actual_instance_record() {
        try{

            // Zkontroluji jestli běží server
            if(!server_is_online()) {
                logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: Server is offline and some procedure called update_instance_to_actual_instance_record");

                WS_Message_Update_instance_to_actual_instance_record response = new WS_Message_Update_instance_to_actual_instance_record();
                response.status = "error";
                response.error = "Server is offline";

                return response;
            }

            if(!instance_online()){

                // Vytvořím Instanci
                WS_Message_Add_new_instance result_instance   = this.cloud_homer_server.add_instance(this);
                if(!result_instance.status.equals("success")){

                    logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: Add Instance Failed:: Error:: " + result_instance.error + " ErrorCode:: "+ result_instance.errorCode);
                    WS_Message_Update_instance_to_actual_instance_record response = new WS_Message_Update_instance_to_actual_instance_record();
                    response.status = result_instance.status;
                    response.error =  result_instance.error;
                    response.errorCode =  result_instance.errorCode;
                    return response;
                }

            }


            // Doplním respektive vymažu HW, který by měl nebo má být v instanci
            this.update_device_summary_collection();


            // Nahraju Blocko Program
            logger.debug("Model_HomerInstance:: update_instance_to_actual_instance_record:: Upload Blocko Program");
            WS_Message_Upload_blocko_program result_blocko_program  = this.upload_blocko_program();

            if(!result_blocko_program.status.equals("success")){

                logger.error("Model_HomerServer:: update_instance_to_actual_instance_record:: upload_blocko_program() Failed:: Error:: " + result_blocko_program.error + " ErrorCode:: " + result_blocko_program.errorCode);

                WS_Message_Update_instance_to_actual_instance_record response = new WS_Message_Update_instance_to_actual_instance_record();
                response.status = result_blocko_program.status;
                response.error =  result_blocko_program.error;
                response.errorCode =  result_blocko_program.errorCode;
                return response;

            }

            logger.debug("Model_HomerInstance:: update_instance_to_actual_instance_record:: Get Summary Information From Instance");
            WS_Message_Get_summary_information summary_information = this.get_summary_information();

            logger.debug("Model_HomerInstance:: update_instance_to_actual_instance_record:: Vytvářím Aktualizační procedury");
            actual_instance.create_actualization_request(summary_information);

            logger.debug("Model_HomerInstance:: update_instance_to_actual_instance_record:: Add new Procedures");

            // Z historických důvodů nepodporováno
            // cloud_homer_server.get_server_webSocket_connection().check_update_for_hw_under_homer_ws.add_new_Procedure(summary_information);


            System.out.println("------------------------------------------");


            actual_instance.refresh();
            for(Model_ActualizationProcedure procedure : actual_instance.procedures) {
                System.out.println("Procedure:: Id:: " + procedure.id + " state:: " + procedure.state);
                Utilities_HW_Updater_Master_thread_updater.add_new_Procedure(procedure);
            }



            WS_Message_Update_instance_to_actual_instance_record response = new WS_Message_Update_instance_to_actual_instance_record();
            response.status = "success";

            return response;

        }catch (Exception e){
            return  new WS_Message_Update_instance_to_actual_instance_record();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Upload_blocko_program upload_blocko_program(){
        try {
            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", actual_instance.version_object.id).eq("file_name", "program.js").findUnique();

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = this.send_to_instance().write_with_confirmation(new WS_Message_Upload_blocko_program().make_request(this, fileRecord, actual_instance.version_object.id), 1000 * 3, 0, 4);

            final Form<WS_Message_Upload_blocko_program> form = Form.form(WS_Message_Upload_blocko_program.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Upload_blocko_program:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Message_Upload_blocko_program();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return  new WS_Message_Upload_blocko_program();
        }catch (Exception e){
            logger.error("Model_HomerServer:: upload_blocko_program:: Error:: ", e);
            return  new WS_Message_Upload_blocko_program();
        }
    }

    @JsonIgnore @Transient
    public void update_device_summary_collection(){
        try {

            if(actual_instance != null) {


                // Seznam - který by na instanci měl běžet!
                List<Model_BProgramHwGroup> hardware_groups = Model_BProgramHwGroup.find.where().eq("b_program_version_groups.id", actual_instance.version_object.id).findList();

                // Zkontroluji HW který aktálně v instanci vysí
                WS_Message_Get_Hardware_list summary_information = this.get_hardware_list();

                if(!summary_information.status.equals("success")){
                    logger.error("Model_HomerServer:: update_device_summary_collection:: Get Instance status Failed:: Error:: " + summary_information.error + " ErrorCode:: "+ summary_information.errorCode);
                    return;
                }

                List<String> yodas_id_on_instance = new ArrayList<>();

                for(WS_Message_Help_Yoda_only_hardware_Id_list yoda_list : summary_information.hardwareIdList){

                    yodas_id_on_instance.add(yoda_list.deviceId);

                    // Nová instance k nasazení neobsahuje Yodu který v instanci už je!!!
                    if(!this.actual_instance.contains_HW(yoda_list.deviceId)){

                        for(String devicesId : yoda_list.devicesId){

                            //Model_Board device_board = Model_Board.find.byId(devicesId);
                            // Můžu změnit nějaký parametr tohoto devicu???
                            // Notifikaci???
                            this.remove_Device_from_instance(devicesId, yoda_list.devicesId);
                        }

                        WS_Message_Remove_yoda_from_instance remove_yoda_from_instance =  this.remove_Yoda_from_instance(yoda_list.deviceId);
                        if(!remove_yoda_from_instance.status.equals("success")){
                            logger.error("Model_HomerServer:: update_device_summary_collection:: Remove Yoda Failed:: Error:: " + remove_yoda_from_instance.error + " ErrorCode:: "+ remove_yoda_from_instance.errorCode);
                        }

                        Model_Board master_board = Model_Board.get_byId(yoda_list.deviceId);
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
                    WS_Message_Remove_device_from_instance remove_device_from_instance = this.remove_Device_from_instance(yoda_list.deviceId,devicesId_for_removing);
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

                        WS_Message_Help_Yoda_only_hardware_Id_list yodaList = summary_information.getListWithYoda(b_program_hw_group.main_board_pair.board_id());
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

                    if(!server_is_online()) throw new InterruptedException();

                    ObjectNode node = send_to_instance().write_with_confirmation( new WS_Message_Update_device_summary_collection().make_request(this, hw_groups), 1000*3, 0, 4);

                    final Form<WS_Message_Update_device_summary_collection> form = Form.form(WS_Message_Update_device_summary_collection.class).bind(node);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Update_device_summary_collection:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());}

                    WS_Message_Update_device_summary_collection rsp = form.get();
                    if(!rsp.status.equals("success")) {
                        logger.error("Model_HomerServer:: WS_Update_device_summary_collection:: Remove Yoda Failed:: Error:: " + rsp.error + " ErrorCode:: " + rsp.errorCode);
                    }

                }

                // Virutální instance - kontrola Yodů kteřá tam mají být a kteří ne!
            }else if(instance_type == Enum_Homer_instance_type.VIRTUAL) {

                List<String> board_id_on_instance = new ArrayList<>();

                // Zkontroluji HW který tam být nemá
                WS_Message_Get_Hardware_list summary_information = get_hardware_list();
                if (!summary_information.status.equals("success")) {
                    logger.error("Model_HomerServer:: update_device_summary_collection:: Get Instance status Failed:: Error:: " + summary_information.error + " ErrorCode:: " + summary_information.errorCode);
                }

                for (WS_Message_Help_Yoda_only_hardware_Id_list yoda_list : summary_information.hardwareIdList) {

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
        }catch (InterruptedException|TimeoutException e){
            return;
        }catch (Exception e){
            logger.error("Model_HomerInstance:: update_device_summary_collection:: Error:: ", e);
            return;
        }
    }

    @JsonIgnore @Transient
    public boolean online_state(){

        return get_status();
    }

    @JsonIgnore @Transient
    public WS_Message_Online_states_devices get_devices_online_state(List<String> device_id){
        try{

            if(!server_is_online()) throw new InterruptedException();

            JsonNode node = send_to_instance().write_with_confirmation( new WS_Message_Online_states_devices().make_request(this, device_id), 1000 * 5, 0, 3);


            final Form<WS_Message_Online_states_devices> form = Form.form(WS_Message_Online_states_devices.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Online_states_devices:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Online_states_devices();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Online_states_devices();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_devices_online_state: Error:: ", e);
            return new WS_Message_Online_states_devices();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Get_summary_information get_summary_information(){
        try {

            if(!server_is_online()) throw new InterruptedException();

            ObjectNode node = send_to_instance().write_with_confirmation(new WS_Message_Get_summary_information().make_request(this), 1000 * 5, 0, 1);

            final Form<WS_Message_Get_summary_information> form = Form.form(WS_Message_Get_summary_information.class).bind(node);
            if (form.hasErrors()) {
                logger.error("Model_HomerInstance:: WS_Get_summary_information: Error:: Some value missing:: " + form.errorsAsJson(new Lang(new play.api.i18n.Lang("en", "US"))).toString());
                throw new Exception("Invalid Json data format");
            }

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Get_summary_information();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_summary_information: Error:: ", e);
            return new WS_Message_Get_summary_information();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Get_Hardware_list get_hardware_list(){
        try {

            if(!server_is_online()) throw new InterruptedException();
            ObjectNode node = send_to_instance().write_with_confirmation( new WS_Message_Get_Hardware_list().make_request(this), 1000*5, 0, 1);

            final Form<WS_Message_Get_Hardware_list> form = Form.form(WS_Message_Get_Hardware_list.class).bind(node);
            if (form.hasErrors()) {logger.error("Model_HomerInstance:: get_hardware_list: Error:: Some value missing:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); throw new Exception("Invalid Json data format");}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Get_Hardware_list();
        }catch (Exception e){
            logger.error("Model_HomerInstance:: get_hardware_list: Error:: ", e);
            return new WS_Message_Get_Hardware_list();
        }
    }

    @JsonIgnore @Transient
    public static void summary_information(WS_HomerServer homer_server , WS_Message_Get_summary_information summary_information){
        try {


            homer_server.check_update_for_hw_under_homer_ws.add_new_Procedure(summary_information);

        }catch (Exception e){
            logger.error("Model_HomerInstance:: void summary_information: Error:: ", e);
        }
    }


    // TOKEN verification
    @JsonIgnore @Transient
    public void cloud_verification_token_GRID(WS_Message_Grid_token_verification help){
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

    @JsonIgnore @Transient
    public void cloud_verification_token_WEBVIEW(WS_Message_WebView_token_verification help){
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

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_HomerInstance.class.getSimpleName();
    public static final String CACHE_STATUS = Model_HomerInstance.class.getSimpleName() + "_STATUS";

    public static Cache<String, Model_HomerInstance> cache; // Server_cache Override during server initialization
    public static Cache<String, Boolean> cache_status; // Server_cache Override during server initialization

    public boolean get_status(){

        Boolean status = cache_status.get(this.blocko_instance_name);
        if (status == null){

            status = this.cloud_homer_server.is_instance_exist(this.blocko_instance_name);
        }

        return status;
    }
}
