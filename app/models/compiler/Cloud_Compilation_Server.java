package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import controllers.WebSocketController;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.servers.Cloud_Homer_Server;
import play.libs.Json;
import utilities.webSocket.SendMessage;
import utilities.webSocket.WS_CompilerServer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
public class Cloud_Compilation_Server extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  @ApiModelProperty(required = true)     public String id;
                                        @Column(unique=true) @ApiModelProperty(required = true)     public String server_name;
                                        @Column(unique=true) @JsonIgnore                            public String unique_identificator;
                                                             @JsonIgnore                            public String hash_certificate;
                                        @Column(unique=true) @ApiModelProperty(required = true)     public String destination_address;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online(){
        return WebSocketController.compiler_cloud_servers.containsKey(this.server_name);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Compilation_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,6);
            if (Cloud_Compilation_Server.find.where().eq("unique_identificator",unique_identificator).findUnique() == null) break;
        }
    }


/* SERVER WEBSOCKET CONTROLLING ---------------------------------------------------------------------------------------*/

    public static String CHANNEL = "compilation-server";
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


    @JsonIgnore @Transient public static boolean is_online(){
        return  !WebSocketController.compiler_cloud_servers.isEmpty();
    }

    @JsonIgnore @Transient public static JsonNode make_Compilation(ObjectNode request){
        try{

            List<String> keys        = new ArrayList<>(WebSocketController.compiler_cloud_servers.keySet());
            WS_CompilerServer server = (WS_CompilerServer) WebSocketController.compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size())) );

            request.put("messageChannel", CHANNEL);
            ObjectNode compilation_request = server.write_with_confirmation(request, 1000*5, 0, 3);

            if(!compilation_request.get("status").asText().equals("success")) {

                logger.debug("Incoming message has not contains state = success");

                ObjectNode error_result = Json.newObject();
                error_result.put("error", "Something was wrong");
                return  error_result;
            }

            logger.debug("Start of compilation was successful - waiting for result");

            SendMessage get_compilation = new SendMessage(null, null, null, "compilation_message", 1000 * 35, 0, 1);
            server.sendMessageMap.put( compilation_request.get("buildId").asText(), get_compilation);

            ObjectNode result = get_compilation.send_with_response();
            result.set("interface_code", compilation_request.get("interface_code") ); // Přiřadím interface do zprávy
            result.put("status", "success" );

            return result;

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public JsonNode ping(){
        try {
            ObjectNode request = Json.newObject();
            request.put("messageType", "ping");
            request.put("messageChannel", CHANNEL);

            return  WebSocketController.compiler_cloud_servers.get(this.server_name).write_with_confirmation(request, 1000 * 3, 0, 3);

        }catch (Exception e){
            return Cloud_Homer_Server.RESULT_server_is_offline();
        }
    }

    @JsonIgnore @Transient public void compiler_server_is_disconnect(){
        logger.debug("Connection lost with compilation cloud_blocko_server!: " + server_name);
        // Nějaké upozornění???
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_delete"); }

    public enum permissions{Cloud_Compilation_Server_create, Cloud_Compilation_Server_edit, Cloud_Compilation_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Compilation_Server> find = new Model.Finder<>(Cloud_Compilation_Server.class);


}
