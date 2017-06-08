package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import utilities.Server;
import utilities.document_db.document_objects.DM_HomerServer_Connect;
import utilities.document_db.document_objects.DM_HomerServer_Disconnect;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Log_level;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.enums.Enum_Where_logged_tag;
import utilities.hardware_updater.helps_objects.Utilities_HW_Updater_Actualization_Task;
import utilities.independent_threads.Check_Board_Status_after_homer_connection;
import utilities.independent_threads.Check_Homer_instance_after_connection;
import utilities.independent_threads.Check_Update_for_hw_on_homer;
import utilities.independent_threads.SynchronizeHomerServer;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homerServer_with_tyrion.*;
import web_socket.message_objects.homer_instance.WS_Message_Add_new_instance;
import web_socket.message_objects.homer_instance.WS_Message_Is_instance_exist;
import web_socket.services.WS_HomerServer;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Entity
@ApiModel(description = "Model of HomerServer",
        value = "HomerServer")
public class Model_HomerServer extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_HomerServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @Id                      public String unique_identificator;
                                       @JsonIgnore              public String hash_certificate;

    @JsonIgnore                                                 public String personal_server_name;

    @ApiModelProperty(required = true, readOnly = true)         public Integer mqtt_port;              // Přidává se destination_address + "/" mqtt_port
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_password;


    @ApiModelProperty(required = true, readOnly = true)         public Integer grid_port;              // Přidává se destination_address + "/" grid_ulr
    @ApiModelProperty(required = true, readOnly = true)         public Integer web_view_port;           // Přidává se destination_address + "/" web_view_port
    @ApiModelProperty(required = true, readOnly = true)         public Integer server_remote_port;     // Přidává se destination_address + "/" web_view_port

    @ApiModelProperty(required = true, readOnly = true)         public String server_url;  // Může být i IP adresa

                                        @JsonIgnore             public Enum_Cloud_HomerServer_type server_type;  // Určující typ serveru
                                                                public Date time_stamp_configuration;

                                                                public Integer days_in_archive;
                                                                public boolean logging;
                                                                public boolean interactive;
                                                                public Enum_Log_level log_level;



    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HomerInstance> cloud_instances  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="connected_server", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_Board> latest_know_connected_board = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient  public boolean server_is_online(){ return Controller_WebSocket.homer_servers.containsKey(this.unique_identificator);}

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
  
    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");
        
        this.time_stamp_configuration = new Date();

        if(hash_certificate == null)  // Určeno pro možnost vytvořit testovací server - manuální doplnění hash_certificate
        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            if (find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        if(unique_identificator == null)    // Určeno pro možnost vytvořit testovací server - manuální doplnění unique_identificator
        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,10);
            if (get_model(unique_identificator) == null) break;
        }

        super.save();

        //Cache Update
        cache.put(this.unique_identificator, this);
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object unique_identificator: {}",  this.unique_identificator);

        //Cache Update
        cache.put(this.unique_identificator, this);

        super.update();
        this.set_new_configuration_on_homer();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object unique_identificator: {}",  this.unique_identificator);

        //Cache Update
        cache.remove(this.unique_identificator);

        super.delete();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public WS_HomerServer get_server_webSocket_connection(){
        return Controller_WebSocket.homer_servers.get(this.unique_identificator);
    }

    @JsonIgnore @Transient public static Model_HomerServer get_destination_server(){


        String unique_identificator = null;
        Integer count = null;
        

        if(Server.server_mode == Enum_Tyrion_Server_mode.production){

            terminal_logger.debug("get_destination_server:: Creating new instance in production mode on production server");

            for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", unique_identificator).findRowCount();

                if (actual_Server_count == 0) {
                    unique_identificator = unique_identificator_help.toString();
                    break;
                } else if (unique_identificator == null) {

                    unique_identificator = unique_identificator_help.toString();
                    count = actual_Server_count;

                } else if (actual_Server_count < count) {
                    unique_identificator = unique_identificator_help.toString();
                    count = actual_Server_count;
                }
            }

            terminal_logger.debug("get_destination_server:: Detination server is " + unique_identificator);
            return Model_HomerServer.find.byId(unique_identificator);

        }


        /*
            Pro stage server platí komplikovanější vyjímka - v případě že má stejné možnosti jako production server se chová jako production,
            to jest přiděluje instance na public servery. Pokud nejsou k dispozici - registrují se všechny na main.
        */
        if(Server.server_mode == Enum_Tyrion_Server_mode.stage){


            if(Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findRowCount() < 1){

                return  Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();

            }else {

                for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                    Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", unique_identificator).findRowCount();

                    if (actual_Server_count == 0) {
                        unique_identificator = unique_identificator_help.toString();
                        break;
                    } else if (unique_identificator == null) {

                        unique_identificator = unique_identificator_help.toString();
                        count = actual_Server_count;

                    } else if (actual_Server_count < count) {
                        unique_identificator = unique_identificator_help.toString();
                        count = actual_Server_count;

                    }
                }

                return Model_HomerServer.find.byId(unique_identificator);
            }
        }

        /*
                V Developer režimu se instance a vše další přiděluje na Test server, který je vytvořen pomocí demodat. Spoléhá se na to,
                že se vytváří jen jeden server.
         */
        if(Server.server_mode == Enum_Tyrion_Server_mode.developer) {
            return Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.test_server).setMaxRows(1).findUnique();
        }

        return null;

    }


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public static final String CHANNEL = "homer-server";
    
    @JsonIgnore @Transient  public static void Messages(WS_HomerServer homer, ObjectNode json){

        new Thread(() -> {

            try {
                switch (json.get("messageType").asText()) {

                    case WS_Message_Unregistred_device_connected.messageType: {

                        final Form<WS_Message_Unregistred_device_connected> form = Form.form(WS_Message_Unregistred_device_connected.class).bind(json);
                        if (form.hasErrors()) {
                            terminal_logger.error("cWS_Unregistred_device_connected:: Incoming Json from Homer Server has not right Form:: " + json.toString());
                            return;
                        }

                        Model_Board.un_registered_device_connected(homer, form.get());
                        return;
                    }

                    case WS_Message_Check_homer_server_person_permission.messageType: {

                        final Form<WS_Message_Check_homer_server_person_permission> form = Form.form(WS_Message_Check_homer_server_person_permission.class).bind(json);
                        if (form.hasErrors()) {
                            terminal_logger.error("WS_Check_homer_server_person_permission:: Incoming Json from Homer Server  has not right Form:: " + json.toString());
                            return;
                        }

                        check_person_permission_for_homer_server(homer, form.get());
                        return;
                    }

                    case WS_Message_Valid_person_token_homer_server.messageType: {

                        final Form<WS_Message_Valid_person_token_homer_server> form = Form.form(WS_Message_Valid_person_token_homer_server.class).bind(json);
                        if (form.hasErrors()) {
                            terminal_logger.error("WS_Valid_person_token_homer_server:: Incoming Json from Homer Server has not right Form:: " + json.toString());
                            return;
                        }

                        check_person_token_for_homer_server(homer, form.get());
                        return;
                    }

                    case WS_Message_Invalid_person_token_homer_server.messageType: {

                        final Form<WS_Message_Invalid_person_token_homer_server> form = Form.form(WS_Message_Invalid_person_token_homer_server.class).bind(json);
                        if (form.hasErrors()) {
                            terminal_logger.error("WS_Invalid_person_token_homer_server:: Incoming Json from Homer Server has not right Form:: " + json.toString());
                            return;
                        }

                        invalid_person_token_for_homer_server(homer, form.get());
                        return;
                    }


                    default: {
                        terminal_logger.error("Incoming message:: Chanel homer-server:: not recognize messageType ->" + json.get("messageType").asText());

                        if (!Model_HomerServer.get_model(homer.identifikator).server_is_online()) {
                            terminal_logger.error("Incoming message:: Chanel homer-server:: Prerequisite invalidly terminate of connection ");

                        }


                        return;
                    }
                }
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

        }).start();
    }

    @JsonIgnore @Transient  public static void check_person_permission_for_homer_server(WS_HomerServer homer, WS_Message_Check_homer_server_person_permission message){
        try{

            Model_Person person = Model_Person.findByEmailAddressAndPassword(message.email, message.password);

            if (person == null) {
                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }

            if( Model_HomerServer.get_model(homer.identifikator).read_permission(person)){

                terminal_logger.debug("Cloud_Homer_Server:: check_person_permission_for_homer_server:: Person found with Email:: " + message.email + " with right permissions");

                Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
                floatingPersonToken.person = person;
                floatingPersonToken.user_agent = message.user_agent;
                floatingPersonToken.where_logged = Enum_Where_logged_tag.HOMER_SERVER;
                floatingPersonToken.save();

                homer.write_without_confirmation( message.make_request_success(Model_HomerServer.get_model(homer.identifikator), person, floatingPersonToken) );
                return;
            }else {

                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient  public static void check_person_token_for_homer_server(WS_HomerServer homer, WS_Message_Valid_person_token_homer_server message){
        try{

            Model_Person person =  Model_Person.get_byAuthToken(message.token);

            if (person == null) {
                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }


            if(Model_HomerServer.get_model(homer.identifikator).read_permission(person)){

                homer.write_without_confirmation(message.make_request_success(Model_HomerServer.get_model(homer.identifikator) , person));

            }else {

                homer.write_without_confirmation(message.make_request_permission_required());

            }

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient  public static void invalid_person_token_for_homer_server(WS_HomerServer homer, WS_Message_Invalid_person_token_homer_server message){
        try{

            Model_FloatingPersonToken token =  Model_FloatingPersonToken.find.byId(message.token);

            if (token == null) {

                terminal_logger.warn("invalid_person_token_for_homer_server:: Token not found!");

                homer.write_without_confirmation(message.make_request_unsuccess());
                return;
            }

            token.delete();
            terminal_logger.debug("invalid_person_token_for_homer_server:: Token found and remove");

            homer.write_without_confirmation(message.make_request_success());

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient  public WS_Message_Get_instance_list get_homer_server_listOfInstance(){
        try {
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Get_instance_list().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Message_Get_instance_list> form = Form.form(WS_Message_Get_instance_list.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Get_instance_list:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return  new WS_Message_Get_instance_list();}
            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Get_instance_list();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Get_instance_list();
        }
    }

    @JsonIgnore @Transient  public WS_Message_Number_of_instances_homer_server get_homer_server_number_of_instance(){
        try {
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Number_of_instances_homer_server().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Message_Number_of_instances_homer_server> form = Form.form(WS_Message_Number_of_instances_homer_server.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Message_Number_of_instances_homer_server:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return  new WS_Message_Number_of_instances_homer_server();}

            return form.get();
        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Number_of_instances_homer_server();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Number_of_instances_homer_server();
        }
    }

    @JsonIgnore @Transient  public boolean is_instance_exist(String instance_name){
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Message_Is_instance_exist().make_request(instance_name), 1000 * 5, 0, 2);

            final Form<WS_Message_Is_instance_exist> form = Form.form(WS_Message_Is_instance_exist.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Is_instance_exist:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return false;}

            WS_Message_Is_instance_exist help = form.get();


            return (help.status.equals("success") && help.exist);

        }catch (InterruptedException|TimeoutException e){
            return false;
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return false;
        }
    }

    @JsonIgnore @Transient  public WS_Message_Add_new_instance add_instance(Model_HomerInstance instance){
        try {

            if (is_instance_exist(instance.blocko_instance_name) ) return new WS_Message_Add_new_instance();
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Message_Add_new_instance().make_request(instance), 1000 * 5, 0, 3);

            final Form<WS_Message_Add_new_instance> form = Form.form(WS_Message_Add_new_instance.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Add_new_instance();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Add_new_instance();
        }catch (Exception e){
            terminal_logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Message_Add_new_instance();
        }

    }

    @JsonIgnore @Transient  public WS_Message_Destroy_instance remove_instance(String instance_name) {
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node =  get_server_webSocket_connection().write_with_confirmation( new WS_Message_Destroy_instance().make_request(instance_name), 1000 * 5, 0, 3);

            final Form<WS_Message_Destroy_instance> form = Form.form(WS_Message_Destroy_instance.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Destroy_instance();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Destroy_instance();
        }catch (Exception e){
            terminal_logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return new WS_Message_Destroy_instance();
        }

    }

    @JsonIgnore @Transient  public void set_new_configuration_on_homer(){
        try{

            if(!server_is_online()) return;

            SynchronizeHomerServer check = new SynchronizeHomerServer(get_server_webSocket_connection());
            check.start();

        }catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient  public  void ask_for_verificationToken(){
        try {

            WS_HomerServer homer_server = get_server_webSocket_connection();
            homer_server.security_token_confirm_procedure();

        }catch (Exception e){
            terminal_logger.warn("Model-HomerServer:: Server::" + unique_identificator + " is offline");
        }
    }

    @JsonIgnore @Transient  public WS_Message_Ping_server ping(){
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Ping_server().make_request(), 1000 * 2, 0, 2);

            final Form<WS_Message_Ping_server> form = Form.form(WS_Message_Ping_server.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Ping_server();}

           return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Ping_server();
        }catch (Exception e){
            terminal_logger.warn("Cloud Homer server {} Id {} is offline!" , personal_server_name , unique_identificator);
            return new WS_Message_Ping_server();
        }
    }

    @JsonIgnore @Transient  public WS_Message_Is_device_connected is_device_connected(String device_id){
        try{

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Message_Is_device_connected().make_request(device_id), 1000*10, 0, 2);

            final Form<WS_Message_Is_device_connected> form = Form.form(WS_Message_Is_device_connected.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Is_device_connected();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Is_device_connected();
        }catch (Exception e){
            terminal_logger.warn("Cloud Homer server {} Id {} is offline!" , personal_server_name , unique_identificator);
            return new WS_Message_Is_device_connected();
        }
    }

    @JsonIgnore @Transient  public void is_disconnect(){
        terminal_logger.debug("is_disconnect:: Tyrion lost connection with Homer server: " + unique_identificator);
        make_log_disconnect();
    }

    @JsonIgnore @Transient  public  void add_task(Utilities_HW_Updater_Actualization_Task task){
        try {

           WS_HomerServer server =  Controller_WebSocket.homer_servers.get(this.unique_identificator);
           server.add_task(task);

        } catch (Exception e){
            terminal_logger.warn("Cloud Homer server {} Id {} is offline!" , personal_server_name , unique_identificator);
        }
    }

    @JsonIgnore @Transient  public void check_after_connection(){

        terminal_logger.debug("Blocko Server: Starting connection control procedure for instancies");

        Check_Homer_instance_after_connection check = new Check_Homer_instance_after_connection(get_server_webSocket_connection(), this);
        check.start();

    }

    @JsonIgnore @Transient  public void synchronize_all_device_state_with_cache(){

        terminal_logger.debug("Blocko Server: Starting connection control procedure for devices - synchronize with cache");

        Check_Board_Status_after_homer_connection check = new Check_Board_Status_after_homer_connection(get_server_webSocket_connection(), this);
        check.start();

    }


    @JsonIgnore @Transient  public void check_HW_updates_on_server(){

        terminal_logger.debug("Blocko Server: Starting connection control procedure for hardware updates");

        Check_Update_for_hw_on_homer check = new Check_Update_for_hw_on_homer(get_server_webSocket_connection(), this);
        check.start();

    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Connect.make_request(this.unique_identificator), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Disconnect.make_request(this.unique_identificator), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud cloud_blocko_server where the system uniformly creating Blocko instantiates or (Customer) can create private cloud_blocko_server for own projects";

                                                                      // TODO oprávnění bude komplikovanější až se budou podporovat lokální servery
    @JsonIgnore                                                       @Transient public boolean create_permission()  {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore                                                       @Transient public boolean read_permission()    {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_read");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean edit_permission()    {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_edit");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean delete_permission()  {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_delete");  }

    // Speciální řízení oprávnění z důvodů ověřování identit na homer serveru
    // Tyrion v contextu nemá Http.Context.current().args.get("person"); podle kterého se běžně všude ověřuje identita!!!
    @JsonIgnore @Transient public boolean create_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore @Transient public boolean read_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_read");    }
    @JsonIgnore @Transient public boolean edit_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_edit");    }
    @JsonIgnore @Transient public boolean delete_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_delete");  }

    public enum permissions{Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_HomerServer.class.getName() + "_MODEL";

    public static Cache<String, Model_HomerServer> cache = null; // Server_cache Override during server initialization


    public static Model_HomerServer get_model(String unique_identificator){

        if(cache == null){
            terminal_logger.error("get_model:: cache_model_homer_server is null");
            return null;
        }

        Model_HomerServer model = cache.get(unique_identificator);

        if(model == null){

            model = Model_HomerServer.find.byId(unique_identificator);
            if (model == null){
                terminal_logger.error("get_model:: unique_identificator not found:: " + unique_identificator);
                return  null;
            }

            cache.put(unique_identificator, model);
        }

        return model;
    }

    public static List<Model_HomerServer> get_model_all(){
        return Model_HomerServer.find.all();
    }



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_HomerServer> find = new Model.Finder<>(Model_HomerServer.class);


}
