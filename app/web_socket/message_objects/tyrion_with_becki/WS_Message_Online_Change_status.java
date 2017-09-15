package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Board;
import models.Model_Project;
import org.codehaus.jackson.map.ObjectMapper;
import play.libs.Json;
import utilities.enums.Enum_Online_status;
import utilities.logger.Class_Logger;
import web_socket.services.WS_Becki_Website;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WS_Message_Online_Change_status {

    private static final Class_Logger terminal_logger = new Class_Logger(WS_Message_Online_Change_status.class);

    @JsonProperty public static final String messageType = "online_status_change";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  WS_Becki_Website.CHANNEL;

    //------------------------------------------------------------------

     @JsonIgnore public String project_id; // Not required!
    //------------------------------------------------------------------

    @JsonProperty public String model;
    @JsonProperty public String model_id;
    @JsonProperty public Enum_Online_status online_status;

//------------------------------------------------------------------

    @JsonIgnore @Transient
    public static void synchronize_online_state_with_becki_public_objects(Class<?> cls, String id, boolean online_state){

        WS_Message_Online_Change_status message = new WS_Message_Online_Change_status(cls, id, online_state ? Enum_Online_status.online : Enum_Online_status.offline);
        ObjectNode json_message =  message.make_request();

        for (WS_Becki_Website becki : Controller_WebSocket.becki_website.values()) {

            try {

                becki.write_without_confirmation(json_message);

            }catch (Exception e){
                terminal_logger.internalServerError(e);
            }
        }

    }

    @JsonIgnore @Transient
    public static void synchronize_online_state_with_becki_project_objects(Class<?> cls, String id, boolean online_state, String project_id){

        new Thread( () -> {
            try {

                WS_Message_Online_Change_status message = new WS_Message_Online_Change_status(cls, id, online_state ? Enum_Online_status.online : Enum_Online_status.offline);

                List<String> list = Model_Project.get_project_becki_person_ids_list(project_id);

                List<String> toUnsubscribe = new ArrayList<>();

                ObjectNode json_message =  message.make_request();

                for (String person_id : list) {

                    try {

                        // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                        if (Controller_WebSocket.becki_website.containsKey(person_id)) {

                            WS_Becki_Website becki = Controller_WebSocket.becki_website.get(person_id);
                            becki.write_without_confirmation(json_message);

                        }else {
                            toUnsubscribe.add(person_id);
                        }

                    }catch (Exception e){
                        terminal_logger.internalServerError(e);
                    }
                }

                toUnsubscribe.forEach(Model_Project::becki_person_id_unsubscribe);

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }


// -------------------------------------------------------------------------------------------------------------------

    public WS_Message_Online_Change_status(Class<?> cls, String model_id, Enum_Online_status online_status){

        this.model_id = model_id;
        this.model = cls.getSimpleName().replace("Model_", "");
        this.online_status = online_status;

    }

    public WS_Message_Online_Change_status(Class<?> cls, String project_id, Long model_id, Enum_Online_status online_status){

        this.model_id = model_id.toString();
        this.model = cls.getSimpleName().replace("Model_", "");
        this.online_status = online_status;
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(){
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", model)
                .put("model_id", model_id)
                .put("online_status", online_status.name());
    }
}