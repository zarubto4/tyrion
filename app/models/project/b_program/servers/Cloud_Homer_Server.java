package models.project.b_program.servers;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import controllers.WebSocketController;
import models.compiler.Board;
import models.project.b_program.instnace.Homer_Instance;
import play.libs.Json;
import utilities.hardware_updater.Actualization_Task;
import utilities.webSocket.WS_BlockoServer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Cloud_Homer_Server extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                       @JsonIgnore              public String unique_identificator;
                                       @JsonIgnore              public String hash_certificate;

                                       @Column(unique=true)     public String server_name;
                                                                public String destination_address;

                                                                public String mqtt_port;              // Přidává se destination_address + "/" mqtt_port
                                                                public String grid_port;              // Přidává se destination_address + "/" grid_ulr
                                                                public String webView_port;           // Přidává se destination_address + "/" webView_port
                                        @Column(unique=true)    public String server_url;             // Může být i IP adresa

    @JsonIgnore                                                 public boolean is_private = false;  // Todo navázat na produkt

    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL) public List<Homer_Instance> cloud_instances  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="latest_know_server") public List<Board>  boards  = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @JsonProperty @Transient  public boolean server_is_online(){
        return WebSocketController.blocko_servers.containsKey(this.server_name);
    }

    @JsonIgnore @Transient public WS_BlockoServer get_websocketServer(){
        return (WS_BlockoServer) WebSocketController.blocko_servers.get(this.server_name);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Homer_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,6);
            if (Cloud_Homer_Server.find.where().eq("unique_identificator",unique_identificator).findUnique() == null) break;
        }
    }

    @JsonIgnore @Transient
    public WS_BlockoServer get_server_webSocket_connection(){
        return (WS_BlockoServer) WebSocketController.blocko_servers.get(this.server_name);
    }




/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER---------------------------------------------------------------------------------*/
    
    public static String CHANNEL = "homer-server";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient  public JsonNode get_homer_server_listOfInstance(){
        try {

            logger.debug("Tyrion: Server want know instances on: " + server_name);

            ObjectNode request = Json.newObject();
            request.put("messageType", "listInstances");
            request.put("messageChannel", CHANNEL);

            return WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 4, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public JsonNode get_homer_server_numberOfInstance(){
        try {

            logger.debug("Tyrion: Server want know instances on: " + server_name);

            ObjectNode request = Json.newObject();
            request.put("messageType", "numberOfInstances");
            request.put("messageChannel", CHANNEL);

            return WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 4, 0, 3);

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

            JsonNode result = WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 4, 0, 3);
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

            if (isInstanceExist(instance_name)) return this.RESULT_instance_already_exist();

            ObjectNode request = Json.newObject();
            request.put("messageType", "createInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance_name);
            request.put("devices", "[]");
            request.put("grid_websocket_token", "ws_" + instance_name);

            logger.debug("Sending to cloud_blocko_server request for new instance ");
            return  WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            return this.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  JsonNode add_instance(Homer_Instance instance){
        try {

            if (isInstanceExist(instance.blocko_instance_name)) return RESULT_instance_already_exist();

            System.err.println("Instance neexistuje a tak jí nahraji na Server");

            ObjectNode request = Json.newObject();
            request.put("messageType", "createInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance.blocko_instance_name);
            request.put("grid_websocket_token", instance.virtual_instance ? (UUID.randomUUID().toString() + UUID.randomUUID().toString()) : instance.actual_instance.websocket_grid_token);

            logger.debug("Sending to cloud_blocko_server request for new instance ");
            logger.debug("Server Name: " + server_name);
            return  WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            e.printStackTrace();
            return RESULT_server_is_offline();
        }

    }

    @JsonIgnore @Transient  public  JsonNode remove_instance(String instance_name) {
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "destroyInstance");
            request.put("messageChannel", CHANNEL);
            request.put("instanceId", instance_name);

            return WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 5, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }

    }

    @JsonIgnore @Transient  public  JsonNode ping(){
        try {

            ObjectNode request = Json.newObject();
            request.put("messageType", "pingServer");
            request.put("messageChannel", CHANNEL);

            return WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000 * 2, 0, 2);
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

            return WebSocketController.blocko_servers.get(server_name).write_with_confirmation(request, 1000*5, 0, 3);

        }catch (Exception e){
            return RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient  public  void is_disconnect(){
        logger.debug("Tyrion lost connection with blocko cloud_blocko_server: " +  WebSocketController.blocko_servers.get(server_name).identifikator);
        // TODO nějaký Alarm když se to stane??
    }

    @JsonIgnore @Transient public  void add_task(Actualization_Task task){
        try {

           WS_BlockoServer server = (WS_BlockoServer) WebSocketController.blocko_servers.get(this.server_name);
           server.add_task(task);

        } catch (Exception e){
            logger.error("Server is offline");
        }
    }

    // Pomocné objekty - Defaultní zprávy
    @JsonIgnore @Transient  public static JsonNode RESULT_server_is_offline(){

        ObjectNode result = Json.newObject();
        result.put("status", "error");
        result.put("code",   800);
        result.put("error", "Server is offline");
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

    @JsonIgnore   @Transient public boolean create_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_read");    }
    @JsonProperty @Transient public boolean edit_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_delete");  }

    public enum permissions{Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Homer_Server> find = new Model.Finder<>(Cloud_Homer_Server.class);


}
