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
import play.data.Form;
import play.i18n.Lang;
import utilities.Server;
import utilities.document_db.document_objects.DM_CompilationServer_Connect;
import utilities.document_db.document_objects.DM_CompilationServer_Disconnect;
import utilities.enums.Enum_Compile_status;
import utilities.enums.Enum_Online_status;
import utilities.independent_threads.compilator_server.Compilation_After_BlackOut;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_CompilerServer_public_Detail;
import web_socket.message_objects.common.WS_Send_message;
import web_socket.services.WS_CompilerServer;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Make_compilation;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Ping_compilation_server;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="CompilationServer")
@ApiModel(description = "Model of CompilationServer",
          value = "Compilation_Server")
public class Model_CompilationServer extends Model {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CompilationServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                      @Id @ApiModelProperty(required = true)     public UUID id;
     @Column(unique=true) @ApiModelProperty(required = true)     public String personal_server_name;

    @JsonIgnore public String connection_identificator;
    @JsonIgnore public String hash_certificate;

    @JsonIgnore public Date date_of_create;

    @ApiModelProperty(required = true, readOnly = true) @Column(unique=true) public String server_url;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient  public Enum_Online_status online_state(){

        return  Controller_WebSocket.compiler_cloud_servers.containsKey( this.id.toString()) ? Enum_Online_status.online : Enum_Online_status.offline;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_CompilerServer_public_Detail get_public_info(){

        Swagger_CompilerServer_public_Detail detail = new Swagger_CompilerServer_public_Detail();
        detail.id = this.id.toString();
        detail.personal_server_name = personal_server_name;
        detail.online_state = online_state();
        detail.edit_permission   = this.edit_permission();
        detail.update_permission = false; // TODO: Doplnit až půjde rekonfigurovat server nadálku - Long term task
        detail.delete_permission = this.delete_permission();


        return detail;
    }




/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save(){

        terminal_logger.debug("save :: Creating new Object");

        // TODO - ADD SSH public KEY from USER
        if(hash_certificate == null) hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        if(connection_identificator == null) connection_identificator = UUID. randomUUID().toString() + "-" + UUID. randomUUID().toString() ;
        date_of_create = new Date();

        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object id: {}",   this.id.toString());
        
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object id: {} ",  this.id.toString());
        super.delete();
    }


/* SERVER WEBSOCKET  --------------------------------------------------------------------------------------------------*/

    public static String CHANNEL = "compilation_server";

    @JsonIgnore @Transient public static boolean is_online(){
        return  !Controller_WebSocket.compiler_cloud_servers.isEmpty();
    }

    @JsonIgnore @Transient public static WS_Message_Make_compilation make_Compilation(ObjectNode request){
        try{

            List<String> keys        = new ArrayList<>(Controller_WebSocket.compiler_cloud_servers.keySet());
            WS_CompilerServer server = Controller_WebSocket.compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size())) );

            ObjectNode compilation_request = server.write_with_confirmation(request, 1000*5, 0, 3);

            if(!compilation_request.get("status").asText().equals("success")) {

                terminal_logger.debug("make_Compilation:: Incoming message has not contains state = success");

                WS_Message_Make_compilation make_compilation = new WS_Message_Make_compilation();

                make_compilation.status = "error_message";
                make_compilation.error_message = "Something was wrong";
                return  make_compilation;
            }

            terminal_logger.debug("make_Compilation:: Start of compilation was successful - waiting for result");

            WS_Send_message get_compilation = new WS_Send_message(null, null, "compilation_message", 1000 * 35, 0, 1);
            get_compilation.set_sender(server);
            server.sendMessageMap.put( compilation_request.get("build_id").asText(), get_compilation);

            ObjectNode node = get_compilation.send_with_response();

            terminal_logger.trace("make_Compilation:: Result is here:: {} ", node.toString());

            final Form<WS_Message_Make_compilation> form = Form.form(WS_Message_Make_compilation.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Make_compilation: Incoming Json from Compilation Server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            WS_Message_Make_compilation compilation = form.get();

            if(compilation_request.has("interface_code")) compilation.interface_code = compilation_request.get("interface_code").toString();

            if(compilation.build_url != null){
                terminal_logger.trace("make_Compilation:: Build URL is not null: {} ", compilation.build_url);
                compilation.status = "success";
            }

            return compilation;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Make_compilation();
        }
    }

    @JsonIgnore @Transient public WS_Message_Ping_compilation_server ping(){
        try {

            JsonNode node =  Controller_WebSocket.compiler_cloud_servers.get( this.id.toString()).write_with_confirmation(new WS_Message_Ping_compilation_server().make_request(), 1000 * 3, 0, 3);

            final Form<WS_Message_Ping_compilation_server> form = Form.form(WS_Message_Ping_compilation_server.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Ping_compilation_server: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")));

            return form.get();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Ping_compilation_server();
        }
    }

    @JsonIgnore @Transient public void compiler_server_is_disconnect(){
        terminal_logger.debug("compiler_server_is_disconnect:: Connection lost with compilation cloud_blocko_server!: " + id + " name " + personal_server_name);
        make_log_disconnect();
    }

    @JsonIgnore @Transient public  void check_after_connection(){

        // Po připojení compilačního serveru s nastartuje procedura zpětné kompilace všeho.
        // Předpoklad je že se připojí více serverů (třeba po pádu nebo udpatu) a tím by došlo k průseru kdy by si
        // servery kompilovali verze navzájem.
        // Proto byl vytvořen singleton který to má řešit.

        // a) ověřím zda existuje vůbec něco, co by mělo smysl kompilovat

        Model_VersionObject version_object = Model_VersionObject.find.where().eq("c_compilation.status", Enum_Compile_status.server_was_offline.name()).order().desc("date_of_create").setMaxRows(1).findUnique();
        if(version_object == null){
            terminal_logger.debug("check_after_connection:: 0 c_program versions for compilations");
            return;
        }

        make_log_connect();

        // b) pokud ano pošlu to do Compilation_After_BlackOut
        Compilation_After_BlackOut.getInstance().start(this);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_CompilationServer_Connect.make_request( this.id.toString()), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_CompilationServer_Disconnect.make_request( this.id.toString()), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.get_person().has_permission("Cloud_Compilation_Server_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("Cloud_Compilation_Server_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().has_permission("Cloud_Compilation_Server_delete"); }

    public enum permissions{Cloud_Compilation_Server_create, Cloud_Compilation_Server_edit, Cloud_Compilation_Server_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_CompilationServer get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_CompilationServer> find = new Model.Finder<>(Model_CompilationServer.class);
}

