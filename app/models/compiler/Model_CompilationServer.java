package models.compiler;

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
import play.libs.Json;
import utilities.enums.Compile_Status;
import utilities.independent_threads.Compilation_After_BlackOut;
import utilities.web_socket.SendMessage;
import utilities.web_socket.WS_CompilerServer;
import utilities.web_socket.message_objects.compilator_tyrion.WS_Ping_compilation_server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name="CompilationServer")
@ApiModel(description = "Model of CompilationServer",
        value = "CompilationServer")
public class Model_CompilationServer extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                      @Id @ApiModelProperty(required = true)     public String unique_identificator;
     @Column(unique=true) @ApiModelProperty(required = true)     public String personal_server_name;
                          @JsonIgnore                            public String hash_certificate;

    @ApiModelProperty(required = true, readOnly = true) @Column(unique=true)    public String server_url; // TODO - Tohle změnit na server_url  // Může být i IP adresa
/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online(){
        return Controller_WebSocket.compiler_cloud_servers.containsKey(this.unique_identificator);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void save(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Model_CompilationServer.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,6);
            if (Model_CompilationServer.find.where().eq("unique_identificator",unique_identificator).findUnique() == null) break;
        }

        super.save();
    }


/* SERVER WEBSOCKET CONTROLLING ---------------------------------------------------------------------------------------*/

    public static String CHANNEL = "compilation-server";

    @JsonIgnore @Transient public static boolean is_online(){
        return  !Controller_WebSocket.compiler_cloud_servers.isEmpty();
    }

    @JsonIgnore @Transient public static JsonNode make_Compilation(ObjectNode request){
        try{

            List<String> keys        = new ArrayList<>(Controller_WebSocket.compiler_cloud_servers.keySet());
            WS_CompilerServer server = (WS_CompilerServer) Controller_WebSocket.compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size())) );

            request.put("messageChannel", CHANNEL);
            ObjectNode compilation_request = server.write_with_confirmation(request, 1000*5, 0, 3);

            if(!compilation_request.get("status").asText().equals("success")) {

                logger.debug("Model_CompilationServer:: make_Compilation:: Incoming message has not contains state = success");

                ObjectNode error_result = Json.newObject();
                error_result.put("error", "Something was wrong");
                return  error_result;
            }

            logger.debug("Model_CompilationServer:: make_Compilation:: Start of compilation was successful - waiting for result");

            SendMessage get_compilation = new SendMessage(null, null, "compilation_message", 1000 * 35, 0, 1);
            server.sendMessageMap.put( compilation_request.get("buildId").asText(), get_compilation);

            ObjectNode result = get_compilation.send_with_response();
            result.set("interface_code", compilation_request.get("interface_code") ); // Přiřadím interface do zprávy
            result.put("status", "success" );

            return result;

        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore @Transient public WS_Ping_compilation_server ping(){
        try {
            ObjectNode request = Json.newObject();
            request.put("messageType", "ping");
            request.put("messageChannel", CHANNEL);

            JsonNode node =  Controller_WebSocket.compiler_cloud_servers.get(this.unique_identificator).write_with_confirmation(new WS_Ping_compilation_server().make_request(), 1000 * 3, 0, 3);

            final Form<WS_Ping_compilation_server> form = Form.form(WS_Ping_compilation_server.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Ping_compilation_server:: Incoming Json for Yoda has not right Form");return new WS_Ping_compilation_server();}

            return form.get();

        }catch (Exception e){
            return new WS_Ping_compilation_server();
        }
    }

    @JsonIgnore @Transient public void compiler_server_is_disconnect(){
        logger.debug("Model_CompilationServer:: compiler_server_is_disconnect:: Connection lost with compilation cloud_blocko_server!: " + unique_identificator + " name " + personal_server_name);
        // Nějaké upozornění???
    }

    @JsonIgnore @Transient public  void check_after_connection(){
        // Po připojení compilačního serveru s nastartuje procedura zpětné kompilace všeho.
        // Předpoklad je že se připojí více serverů (třeba po pádu nebo udpatu) a tím by došlo k průseru kdy by si
        // servery kompilovali verze navzájem.
        // Proto byl vytvořen singleton který to má řešit.

        // a) ověřím zda existuje vůbec něco, co by mělo smysl kompilovat

        Model_VersionObject version_object = Model_VersionObject.find.where().eq("c_compilation.status", Compile_Status.server_was_offline.name()).order().desc("date_of_create").setMaxRows(1).findUnique();
        if(version_object == null){
            logger.debug("Model_CompilationServer:: check_after_connection:: 0 c_program versions for compilations");
            return;
        }

        // b) pokud ano pošlu to do Compilation_After_BlackOut
        Compilation_After_BlackOut.getInstance().start(this);
    }
/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/



/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.getPerson().has_permission("Cloud_Compilation_Server_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("Cloud_Compilation_Server_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("Cloud_Compilation_Server_delete"); }

    public enum permissions{Cloud_Compilation_Server_create, Cloud_Compilation_Server_edit, Cloud_Compilation_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CompilationServer> find = new Model.Finder<>(Model_CompilationServer.class);


}

