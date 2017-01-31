package models.project.b_program.servers;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Model_FloatingPersonToken;
import models.person.Model_Person;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.Server;
import utilities.enums.CLoud_Homer_Server_Type;
import utilities.hardware_updater.Actualization_Task;
import utilities.webSocket.WS_HomerServer;
import utilities.webSocket.messageObjects.WS_CheckPersonPermission_OnHomerServer;
import utilities.webSocket.messageObjects.WS_InvalidPersonToken_OnHomerServer;
import utilities.webSocket.messageObjects.WS_ValidPersonToken_OnHomerServer;

import javax.persistence.*;
import java.util.ArrayList;
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

    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_port;              // Přidává se destination_address + "/" mqtt_port
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_password;


    @ApiModelProperty(required = true, readOnly = true)         public String grid_port;              // Přidává se destination_address + "/" grid_ulr
    @ApiModelProperty(required = true, readOnly = true)         public String webView_port;           // Přidává se destination_address + "/" webView_port

    @ApiModelProperty(required = true, readOnly = true) @Column(unique=true)    public String server_url;  // Může být i IP adresa

                                        @JsonIgnore             public CLoud_Homer_Server_Type server_type;  // Určující typ serveru


    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HomerInstance> cloud_instances  = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(required = true, readOnly = true)
    @JsonProperty @Transient  public boolean server_is_online(){
        return Controller_WebSocket.blocko_servers.containsKey(this.unique_identificator);
    }






/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

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

    @JsonIgnore @Transient public WS_HomerServer get_server_webSocket_connection(){
        return (WS_HomerServer) Controller_WebSocket.blocko_servers.get(this.unique_identificator);
    }

    @JsonIgnore @Transient public WS_HomerServer get_websocketServer(){
        return (WS_HomerServer) Controller_WebSocket.blocko_servers.get(this.unique_identificator);
    }




    @JsonIgnore @Transient public static Model_HomerServer getDestinationServer(){


        if(Server.server_mode) {

            return Model_HomerServer.find.where().eq("server_type", CLoud_Homer_Server_Type.test_server).setMaxRows(1).findUnique();

        }else {

            String wining_server_id = null;
            Integer count = null;

            for (Object server_id :  Model_HomerServer.find.where().eq("server_type", CLoud_Homer_Server_Type.public_server).findIds()) {

                System.out.println();


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


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER---------------------------------------------------------------------------------*/
    
    public static String CHANNEL = "homer-server";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient public static void Messages(WS_HomerServer homer, ObjectNode json){
        try {
            switch (json.get("messageType").asText()) {

                case "yoda_unauthorized_logging": {
                    logger.warn("Cloud_Homer_Server:: Incoming message:: Chanel homer-server:: Unauthorized login!");
                    //TODO
                    return;
                }

                case "checkUserPermission" : {

                    WS_CheckPersonPermission_OnHomerServer message_help = WS_CheckPersonPermission_OnHomerServer.getObject(json);
                    check_person_permission_for_homer_server(homer, message_help);
                    return;
                }

                case "checkPersonToken" : {

                    WS_ValidPersonToken_OnHomerServer message_help = WS_ValidPersonToken_OnHomerServer.getObject(json);
                    check_person_token_for_homer_server(homer, message_help);

                    return;
                }

                case "removePersonLoginToken" : {

                    WS_InvalidPersonToken_OnHomerServer message_help = WS_InvalidPersonToken_OnHomerServer.getObject(json);
                    invalid_person_token_for_homer_server(homer, message_help);
                    return;
                }


                default: {
                    logger.error("Cloud_Homer_Server:: Incoming message:: Chanel homer-server:: not recognize messageType ->" + json.get("messageType").asText());
                    return;
                }
            }
        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: Incoming message:: Error", e);
        }
    }

    @JsonIgnore @Transient  public static void check_person_permission_for_homer_server(WS_HomerServer homer, WS_CheckPersonPermission_OnHomerServer message){
        try{


            Model_Person person = Model_Person.findByEmailAddressAndPassword(message.email, message.password);

            if (person == null) {

                logger.warn("Cloud_Homer_Server:: check_person_permission_for_homer_server:: Person not found! Email:: " + message.email);
                ObjectNode request = Json.newObject();
                request.put("messageType", "checkUserPermission");
                request.put("messageChannel", CHANNEL);
                request.put("messageId", message.messageId);
                request.put("status", "error");
                request.put("message", "Email or Password is wrong");
                homer.write_without_confirmation(request);
                return;
            }


            if(homer.server.read_permission(person)){

                logger.debug("Cloud_Homer_Server:: check_person_permission_for_homer_server:: Person found with Email:: " + message.email + " with right permissions");

                Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
                floatingPersonToken.set_basic_values();
                floatingPersonToken.person = person;
                floatingPersonToken.user_agent = message.user_agent;
                floatingPersonToken.where_logged  = "Homer Server (" + homer.server.personal_server_name + ")";
                floatingPersonToken.save();

                ObjectNode request = Json.newObject();
                request.put("messageType", "checkUserPermission");
                request.put("messageChannel", CHANNEL);
                request.put("status", "success");
                request.put("messageId", message.messageId);
                request.put("read_permission", homer.server.read_permission(person));
                request.put("edit_permission", homer.server.edit_permission(person));
                request.put("delete_permission", homer.server.delete_permission(person));
                request.put("create_permission", homer.server.create_permission(person));
                request.put("authToken", floatingPersonToken.authToken);

                homer.write_without_confirmation(request);
                return;
            }else {

                ObjectNode request = Json.newObject();
                request.put("messageType", "checkUserPermission");
                request.put("messageChannel", CHANNEL);
                request.put("messageId", message.messageId);
                request.put("status", "error");
                request.put("message", "Permission Required");

                homer.write_without_confirmation(request);
                return;
            }



        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void check_person_token_for_homer_server(WS_HomerServer homer, WS_ValidPersonToken_OnHomerServer message){
        try{

            Model_Person person =  Model_Person.findByAuthToken(message.token);

            if (person == null) {

                logger.warn("Cloud_Homer_Server:: check_person_token_for_homer_server:: Person not found!");
                ObjectNode request = Json.newObject();
                request.put("messageType", "checkPersonToken");
                request.put("messageChannel", CHANNEL);
                request.put("messageId", message.messageId);
                request.put("status", "error");
                request.put("message", "Token is not valid");
                homer.write_without_confirmation(request);
                return;
            }


            if(homer.server.read_permission(person)){

                logger.debug("Cloud_Homer_Server:: check_person_permission_for_homer_server:: Person found with Email:: " + person.mail + " with right permissions");

                ObjectNode request = Json.newObject();
                request.put("messageType", "checkPersonToken");
                request.put("messageChannel", CHANNEL);
                request.put("status", "success");
                request.put("messageId", message.messageId);
                request.put("read_permission",   homer.server.read_permission(person));
                request.put("edit_permission",   homer.server.edit_permission(person));
                request.put("delete_permission", homer.server.delete_permission(person));
                request.put("create_permission", homer.server.create_permission(person));

                homer.write_without_confirmation(request);
                return;

            }else {

                ObjectNode request = Json.newObject();
                request.put("messageType", "checkPersonToken");
                request.put("messageChannel", CHANNEL);
                request.put("messageId", message.messageId);
                request.put("status", "error");
                request.put("message", "Permission Required");

                homer.write_without_confirmation(request);
                return;
            }



        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void invalid_person_token_for_homer_server(WS_HomerServer homer, WS_InvalidPersonToken_OnHomerServer message){
        try{

            Model_FloatingPersonToken token =  Model_FloatingPersonToken.find.byId(message.token);

            if (token == null) {

                logger.warn("Cloud_Homer_Server:: invalid_person_token_for_homer_server:: Token not found!");
                ObjectNode request = Json.newObject();
                request.put("messageType", "checkPersonToken");
                request.put("messageChannel", CHANNEL);
                request.put("messageId", message.messageId);
                request.put("status", "error");
                request.put("message", "Token is not valid");
                homer.write_without_confirmation(request);
                return;
            }

            token.delete(); logger.debug("Cloud_Homer_Server:: invalid_person_token_for_homer_server:: Token found and remove");

            ObjectNode request = Json.newObject();
            request.put("messageType", "removePersonLoginToken");
            request.put("messageChannel", CHANNEL);
            request.put("status", "success");
            request.put("messageId", message.messageId);

            homer.write_without_confirmation(request);


        }catch (Exception e){
            logger.error("Cloud_Homer_Server:: check_person_permission_for_homer_server :: Error:: ", e);
        }
    }

    @JsonIgnore @Transient  public JsonNode get_homer_server_listOfInstance(){
        try {

            logger.debug("Tyrion: Server want know instances on: " + unique_identificator);

            ObjectNode request = Json.newObject();
            request.put("messageType", "listInstances");
            request.put("messageChannel", CHANNEL);

            return Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 4, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public JsonNode get_homer_server_numberOfInstance(){
        try {

            logger.debug("Tyrion: Server want know instances on: " + personal_server_name + " " + unique_identificator);

            ObjectNode request = Json.newObject();
            request.put("messageType", "numberOfInstances");
            request.put("messageChannel", CHANNEL);

            return Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 4, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  boolean isInstanceExist(String instance_name){
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "instanceExist");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance_name);

            JsonNode result = Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 4, 0, 3);

            if(result.get("status").asText().equals("success") && result.get("exist").asText().equals("true")){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    @JsonIgnore @Transient  public  JsonNode add_temporary_instance(String instance_name){
        try{

            // Slouží pro nahrávání testovacích instnací samotnými vývojáři z Byzance

            if (isInstanceExist(instance_name)) return RESULT_instance_already_exist();

            ObjectNode request = Json.newObject();
            request.put("messageType", "createInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance_name);
            request.put("devices", "[]");
            request.put("grid_websocket_token", "ws_" + instance_name);

            logger.debug("Sending to cloud_blocko_server request for new instance ");
            return  Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  JsonNode add_instance(Model_HomerInstance instance){
        try {

            if (isInstanceExist(instance.blocko_instance_name)) return RESULT_instance_already_exist();

            logger.debug("Instance neexistuje a tak jí nahraji na Server");

            ObjectNode request = Json.newObject();
            request.put("messageType", "createInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance.blocko_instance_name);
           // request.put("grid_websocket_token", instance.virtual_instance ? (UUID.randomUUID().toString() + UUID.randomUUID().toString()) : instance.actual_instance.websocket_grid_token);

            logger.debug("Sending to cloud_blocko_server request for new instance ");
            logger.debug("Server Name: "+ personal_server_name + " " + unique_identificator);
            return  Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
            return RESULT_server_is_offline();
        }

    }

    @JsonIgnore @Transient  public  JsonNode remove_instance(String instance_name) {
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "destroyInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance_name);


            return Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }

    }

    @JsonIgnore @Transient  public  void ask_for_verificationToken(){
        try {

         WS_HomerServer homer_server = (WS_HomerServer) Controller_WebSocket.blocko_servers.get(unique_identificator);
         homer_server.security_token_confirm_procedure();
            
        }catch (Exception e){
            logger.error("Model-HomerServer:: Server::" + unique_identificator + " is offline");
        }
    }

    @JsonIgnore @Transient  public  JsonNode ping(){
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "pingServer");
            request.put("messageChannel", CHANNEL);

            return Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000 * 2, 0, 2);
        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  JsonNode unregistered_board_are_connected(String macAddress){
        try{

            ObjectNode request = Json.newObject();
            request.put("messageType", "unregisteredHardware");
            request.put("messageChannel", CHANNEL);
            request.put("macAddress", macAddress);

            return Controller_WebSocket.blocko_servers.get(unique_identificator).write_with_confirmation(request, 1000*5, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  void is_disconnect(){
        logger.debug("Tyrion lost connection with blocko cloud_blocko_server: " +  Controller_WebSocket.blocko_servers.get(unique_identificator));
        // TODO nějaký Alarm když se to stane??
    }

    @JsonIgnore @Transient public  void add_task(Actualization_Task task){
        try {

           WS_HomerServer server = (WS_HomerServer) Controller_WebSocket.blocko_servers.get(this.unique_identificator);
           server.add_task(task);

        } catch (Exception e){
            logger.error("Model-HomerServer:: Server::" + unique_identificator + " is offline");
        }
    }

    // Procedura po připojení serveru
    @JsonIgnore @Transient  public void check_after_connection( WS_HomerServer ws_blockoServer){

        logger.debug("Connection lost with compilation cloud_blocko_server!: " + personal_server_name + " " + unique_identificator);

        Model_HomerServer homer_server = this;

        class Control_Blocko_Server_Thread extends Thread{

            @Override
            public void run() {
                Long interrupter = (long) 6000;
                try {

                    while (interrupter > 0) {

                        sleep(1000);
                        interrupter -= 500;

                        if (ws_blockoServer.isReady()){

                            logger.trace("Homer Server:: Connection::  Tyrion send to Homer Server request for listInstances");

                            JsonNode result = homer_server.get_homer_server_listOfInstance();
                            if (!result.get("status").asText().equals("success")) {interrupt();}

                            // Vylistuji si seznam instnancí, které běží na serveru
                            List<String> instances_on_server = new ArrayList<>();
                            final JsonNode arrNode = result.get("instances");
                            for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());
                            logger.trace("Homer Server:: Connection:: Number of instances on cloud_blocko_server: " + instances_on_server.size());


                            // Vylistuji si seznam instnancí, které by měli běžet na serveru

                            List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

                            // Přidám všechny reálné instance, které mají běžet.
                            instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", homer_server.unique_identificator).eq("virtual_instance", false).isNotNull("actual_instance").select("blocko_instance_name").findList());

                            // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                            instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", homer_server.unique_identificator).eq("virtual_instance", true).isNotNull("boards_in_virtual_instance").select("blocko_instance_name").findList());


                            List<String> instances_for_removing = new ArrayList<>();

                            // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                            for(String  identificator : instances_on_server){

                                // NAjdu jestli instance má oprávnění být nazasená podle parametrů nasaditelné instnace
                                Integer size = Model_HomerInstance.find.where().eq("blocko_instance_name", identificator)
                                        .disjunction()
                                        .conjunction()
                                        .eq("virtual_instance", false)
                                        .isNotNull("actual_instance")
                                        .endJunction()
                                        .conjunction()
                                        .eq("virtual_instance", true)
                                        .isNotNull("boards_in_virtual_instance")
                                        .endJunction()
                                        .endJunction().findRowCount();

                                if(size < 1){
                                    logger.warn("Blocko Server: removing instnace:: ", identificator);
                                    instances_for_removing.add(identificator);
                                }
                            }

                            logger.debug("Blocko Server: The number of instances for removing from homer server: ");


                            if (!instances_for_removing.isEmpty()) {
                                for (String identificator : instances_for_removing) {
                                    JsonNode remove_result = homer_server.remove_instance(identificator);
                                    if(!remove_result.has("status") || !remove_result.get("status").asText().equals("success"))   logger.error("Blocko Server: Removing instance Error: ", remove_result.toString());
                                }
                            }


                            // Nahraji tam ty co tam patří
                            logger.trace("Homer Server:: Connection::Starting to uploud new instances to cloud_blocko_server");
                            for (Model_HomerInstance instance : instances_in_database_for_uploud) {

                                if(instances_on_server.contains(instance.blocko_instance_name)){
                                    logger.debug("Homer Server:: Connection:: ", instance.blocko_instance_name , " is on server already");
                                }else {
                                    JsonNode add_instance = instance.add_instance_to_server();
                                    logger.debug("add_instance: " + add_instance.toString());

                                    if (add_instance.get("status").asText().equals("success")) {
                                        logger.trace("Blocko Server: Uploud instance was successful");
                                        instance.check_hardware_c_program_state();
                                    }
                                    else if (add_instance.get("status").asText().equals("error")) {
                                        logger.warn("Blocko Server: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: ", add_instance.toString());
                                    }

                                    sleep(50); // Abych Homer server tolik nevytížil
                                }
                            }


                            logger.debug("Blocko Server: Successfully finished connection procedure");
                            interrupter = (long) 0;

                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        logger.debug("Blocko Server: Starting connection control procedure");
        new Control_Blocko_Server_Thread().start();

    }

    // Pomocné objekty - Defaultní zprávy
    @JsonIgnore @Transient  public static JsonNode RESULT_server_is_offline(){

        ObjectNode result = Json.newObject();
        result.put("status", "error");
        result.put("code",   800);
        result.put("error", "Model-HomerServer:: Server:: is offline");
        return result;
    }

    @JsonIgnore @Transient  public static JsonNode RESULT_instance_already_exist(){

        ObjectNode result = Json.newObject();
        result.put("status", "error");
        result.put("code",   801);
        result.put("error", "Instance alrady Exist");
        return result;
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
