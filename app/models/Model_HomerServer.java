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
import play.data.Form;
import play.i18n.Lang;
import play.mvc.Http;
import utilities.Server;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Log_level;
import utilities.enums.Enum_Where_logged_tag;
import utilities.hardware_updater.Actualization_Task;
import utilities.independent_threads.Check_Homer_instance_after_connection;
import utilities.independent_threads.Check_Update_for_hw_on_homer;
import utilities.independent_threads.SynchronizeHomerServer;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.homer_instance.WS_Add_new_instance;
import utilities.web_socket.message_objects.homer_instance.WS_Is_instance_exist;
import utilities.web_socket.message_objects.homer_tyrion.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of HomerServer",
        value = "HomerServer")
public class Model_HomerServer extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                      @JsonIgnore @Id           public String unique_identificator;
                                       @JsonIgnore              public String hash_certificate;

    @JsonIgnore                                                 public String personal_server_name;

    @ApiModelProperty(required = true, readOnly = true)         public Integer mqtt_port;              // Přidává se destination_address + "/" mqtt_port
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_password;


    @ApiModelProperty(required = true, readOnly = true)         public Integer grid_port;              // Přidává se destination_address + "/" grid_ulr
    @ApiModelProperty(required = true, readOnly = true)         public Integer webView_port;           // Přidává se destination_address + "/" webView_port
    @ApiModelProperty(required = true, readOnly = true)         public Integer server_remote_port;     // Přidává se destination_address + "/" webView_port

    @ApiModelProperty(required = true, readOnly = true)         public String server_url;  // Může být i IP adresa

                                        @JsonIgnore             public Enum_Cloud_HomerServer_type server_type;  // Určující typ serveru
                                                                public Date time_stamp_configuration;

                                                                public Integer days_in_archive;
                                                                public boolean logging;
                                                                public boolean interactive;
                                                                public Enum_Log_level logLevel;

    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HomerInstance> cloud_instances  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="connected_server", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_Board> latest_know_connected_board = new ArrayList<>();


    /* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(required = true, readOnly = true)
    @JsonProperty @Transient  public boolean server_is_online(){
        return Controller_WebSocket.homer_servers.containsKey(this.unique_identificator);
    }






/* JSON CRUD-------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        this.time_stamp_configuration = new Date();

        if(hash_certificate == null)  // Určeno pro možnost vytvořit testovací server - manuální doplnění hash_certificate
        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            if (Model_HomerServer.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        if(unique_identificator == null)    // Určeno pro možnost vytvořit testovací server - manuální doplnění unique_identificator
        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,10);
            if (Model_HomerServer.find.where().eq("unique_identificator",unique_identificator).findUnique() == null) break;
        }

        super.save();
    }

    @JsonIgnore @Override public void update() {
        super.update();
        this.set_new_configuration_on_homer();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public WS_HomerServer get_server_webSocket_connection(){
        return (WS_HomerServer) Controller_WebSocket.homer_servers.get(this.unique_identificator);
    }

    @JsonIgnore @Transient public static Model_HomerServer get_destination_server(){


        if(Server.server_mode.equals("developer")||Server.server_mode.equals("stage")) {

            return Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.test_server).setMaxRows(1).findUnique();

        }else {

            String wining_server_id = null;
            Integer count = null;

            for (Object server_id :  Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {


                Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", server_id).findRowCount();

                if(actual_Server_count == 0){
                    wining_server_id = server_id.toString();
                    break;
                }
                else if(wining_server_id == null) {

                    wining_server_id = server_id.toString();
                    count = actual_Server_count;

                }else if(actual_Server_count < count ){
                    wining_server_id  = server_id.toString();
                    count = actual_Server_count;

                }
            }

            return  Model_HomerServer.find.byId(wining_server_id);
        }
    }





/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER---------------------------------------------------------------------------------*/
    
    public static final String CHANNEL = "homer-server";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient public static void Messages(WS_HomerServer homer, ObjectNode json){
        try {
            switch (json.get("messageType").asText()) {

                case WS_Unregistred_device_connected.messageType : {

                    final Form<WS_Unregistred_device_connected> form = Form.form(WS_Unregistred_device_connected.class).bind(json);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Unregistred_device_connected:: Incoming Json from Homer Server has not right Form:: " + json.toString());return;}

                    Model_Board.unregistred_device_connected(homer, form.get());
                    return;
                }

                case WS_Check_homer_server_person_permission.messageType : {

                    final Form<WS_Check_homer_server_person_permission> form = Form.form(WS_Check_homer_server_person_permission.class).bind(json);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Check_homer_server_person_permission:: Incoming Json from Homer Server  has not right Form:: " + json.toString());return;}

                    check_person_permission_for_homer_server(homer, form.get());
                    return;
                }

                case WS_Valid_person_token_homer_server.messageType : {

                    final Form<WS_Valid_person_token_homer_server> form = Form.form(WS_Valid_person_token_homer_server.class).bind(json);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Valid_person_token_homer_server:: Incoming Json from Homer Server has not right Form:: " + json.toString());return;}

                    check_person_token_for_homer_server(homer, form.get());
                    return;
                }

                case WS_Invalid_person_token_homer_server.messageType : {

                    final Form<WS_Invalid_person_token_homer_server> form = Form.form(WS_Invalid_person_token_homer_server.class).bind(json);
                    if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Invalid_person_token_homer_server:: Incoming Json from Homer Server has not right Form:: " + json.toString());return;}

                    invalid_person_token_for_homer_server(homer, form.get());
                    return;
                }


                default: {
                    logger.error("Model_HomerServer:: Incoming message:: Chanel homer-server:: not recognize messageType ->" + json.get("messageType").asText());
                    return;
                }
            }
        }catch (Exception e){
            logger.error("Model_HomerServer:: Incoming message:: Error", e);
        }
    }

    @JsonIgnore @Transient  public static void check_person_permission_for_homer_server(WS_HomerServer homer, WS_Check_homer_server_person_permission message){
        try{

            Model_Person person = Model_Person.findByEmailAddressAndPassword(message.email, message.password);

            if (person == null) {
                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }

            if(homer.server.read_permission(person)){

                logger.debug("Cloud_Homer_Server:: check_person_permission_for_homer_server:: Person found with Email:: " + message.email + " with right permissions");

                Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
                floatingPersonToken.person = person;
                floatingPersonToken.user_agent = message.user_agent;
                floatingPersonToken.where_logged = Enum_Where_logged_tag.HOMER_SERVER;
                floatingPersonToken.save();

                homer.write_without_confirmation( message.make_request_success(homer.server, person, floatingPersonToken) );
                return;
            }else {

                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }



        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void check_person_token_for_homer_server(WS_HomerServer homer, WS_Valid_person_token_homer_server message){
        try{

            Model_Person person =  Model_Person.findByAuthToken(message.token);

            if (person == null) {
                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }


            if(homer.server.read_permission(person)){

                homer.write_without_confirmation(message.make_request_success(homer.server, person));

            }else {

                homer.write_without_confirmation(message.make_request_permission_required());

            }

        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void invalid_person_token_for_homer_server(WS_HomerServer homer, WS_Invalid_person_token_homer_server message){
        try{

            Model_FloatingPersonToken token =  Model_FloatingPersonToken.find.byId(message.token);

            if (token == null) {

                logger.warn("Cloud_Homer_Server:: invalid_person_token_for_homer_server:: Token not found!");

                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }

            token.delete(); logger.debug("Cloud_Homer_Server:: invalid_person_token_for_homer_server:: Token found and remove");

            homer.write_without_confirmation(message.make_request_success());

        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public WS_Get_instance_list get_homer_server_listOfInstance(){
        try {

            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Get_instance_list().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Get_instance_list> form = Form.form(WS_Get_instance_list.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Get_instance_list:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return  new WS_Get_instance_list();}
            return form.get();

        }catch (Exception e){
            logger.error("Model_HomerServer:: get_homer_server_listOfInstance:: Error", e);
            return new WS_Get_instance_list();
        }
    }

    @JsonIgnore @Transient  public WS_Number_of_instances_homer_server get_homer_server_number_of_instance(){
        try {

            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Number_of_instances_homer_server().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Number_of_instances_homer_server> form = Form.form(WS_Number_of_instances_homer_server.class).bind(node);
            if(form.hasErrors()){

                Lang leng = new Lang( new play.api.i18n.Lang("en-us", "US"));
                Http.Context.current().setTransientLang(leng);

                logger.error("Model_HomerServer:: WS_Number_of_instances_homer_server:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson().toString());return new WS_Number_of_instances_homer_server();
            }

            return form.get();
        }catch (Exception e){
            logger.error("Model_HomerServer:: set_new_configuration_on_homer:: Error", e);
            return new WS_Number_of_instances_homer_server();
        }
    }

    @JsonIgnore @Transient  public boolean is_instance_exist(String instance_name){
        try {

            if(!server_is_online()) return false;
            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Is_instance_exist().make_request(instance_name), 1000 * 5, 0, 2);

            final Form<WS_Is_instance_exist> form = Form.form(WS_Is_instance_exist.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Is_instance_exist:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return false;}

            WS_Is_instance_exist help = form.get();


            return (help.status.equals("success") && help.exist);

        }catch (Exception e){
            logger.error("Model_HomerServer:: is_instance_exist:: Error", e);
            return false;
        }
    }

    @JsonIgnore @Transient  public WS_Add_new_instance add_instance(Model_HomerInstance instance){
        try {

            if ( is_instance_exist(instance.blocko_instance_name) ) return new WS_Add_new_instance();

            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Add_new_instance().make_request(instance), 1000 * 5, 0, 3);

            final Form<WS_Add_new_instance> form = Form.form(WS_Add_new_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Add_new_instance();}

            return form.get();

        }catch (Exception e){
            logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Add_new_instance();
        }

    }

    @JsonIgnore @Transient  public WS_Destroy_instance remove_instance(String instance_name) {
        try {

            JsonNode node =  get_server_webSocket_connection().write_with_confirmation( new WS_Destroy_instance().make_request(instance_name), 1000 * 5, 0, 3);

            final Form<WS_Destroy_instance> form = Form.form(WS_Destroy_instance.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Destroy_instance();}

            return form.get();

        }catch (Exception e){
            logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Destroy_instance();
        }

    }

    @JsonIgnore @Transient  public void set_new_configuration_on_homer(){
        try{

            SynchronizeHomerServer check = new SynchronizeHomerServer(get_server_webSocket_connection());
            check.start();

        }catch (Exception e) {
            logger.error("Model_HomerServer:: set_new_configuration_on_homer:: Error", e);
        }
    }

    @JsonIgnore @Transient  public  void ask_for_verificationToken(){
        try {

         WS_HomerServer homer_server = (WS_HomerServer) get_server_webSocket_connection();
         homer_server.security_token_confirm_procedure();
            
        }catch (Exception e){
            logger.error("Model-HomerServer:: Server::" + unique_identificator + " is offline");
        }
    }

    @JsonIgnore @Transient  public  WS_Ping_server ping(){
        try {

            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Ping_server().make_request(), 1000 * 2, 0, 2);

            final Form<WS_Ping_server> form = Form.form(WS_Ping_server.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Ping_server();}

           return form.get();

        }catch (Exception e){
            logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Ping_server();
        }
    }

    @JsonIgnore @Transient  public  WS_Is_device_connected is_device_connected(String device_id){
        try{


            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Is_device_connected().make_request(device_id), 1000*10, 0, 2);

            final Form<WS_Is_device_connected> form = Form.form(WS_Is_device_connected.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Is_device_connected();}

            return form.get();

        }catch (Exception e){
            logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Is_device_connected();
        }
    }

    @JsonIgnore @Transient  public void is_disconnect(){
        logger.debug("Tyrion lost connection with blocko cloud_blocko_server: " + unique_identificator);
        // TODO nějaký Alarm když se to stane??
    }

    @JsonIgnore @Transient public  void add_task(Actualization_Task task){
        try {

           WS_HomerServer server = (WS_HomerServer) Controller_WebSocket.homer_servers.get(this.unique_identificator);
           server.add_task(task);

        } catch (Exception e){
            logger.error("Model-HomerServer:: Server::" + unique_identificator + " is offline");
        }
    }

    @JsonIgnore @Transient  public void check_after_connection(){

        logger.debug("Blocko Server: Starting connection control procedure for instancies");

        Check_Homer_instance_after_connection check = new Check_Homer_instance_after_connection(get_server_webSocket_connection(), this);
        check.start();

    }

    @JsonIgnore @Transient  public void check_HW_updates_on_server(){

        logger.debug("Blocko Server: Starting connection control procedure for hardware updates");

        Check_Update_for_hw_on_homer check = new Check_Update_for_hw_on_homer(get_server_webSocket_connection(), this);
        check.start();

    }

    
/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud cloud_blocko_server where the system uniformly creating Blocko instantiates or (Customer) can create private cloud_blocko_server for own projects";

                                                                                                                     // TODO oprávnění bude komplikovanější až se budou podporovat lokální servery
    @JsonIgnore                                                       @Transient public boolean create_permission()  {  return Controller_Security.getPerson().has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore                                                       @Transient public boolean read_permission()    {  return Controller_Security.getPerson().has_permission("Cloud_Homer_Server_read");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean edit_permission()    {  return Controller_Security.getPerson().has_permission("Cloud_Homer_Server_edit");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean delete_permission()  {  return Controller_Security.getPerson().has_permission("Cloud_Homer_Server_delete");  }

    // Speciální řízení oprávnění z důvodů ověřování identit na homer serveru
    // Tyrion v contextu nemá Http.Context.current().args.get("person"); podle kterého se běžně všude ověřuje identita!!!
    @JsonIgnore public boolean create_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore public boolean read_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_read");    }
    @JsonIgnore public boolean edit_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_edit");    }
    @JsonIgnore public boolean delete_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_delete");  }


    public enum permissions{Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_HomerServer> find = new Model.Finder<>(Model_HomerServer.class);


}
