package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Project;
import play.libs.Json;
import utilities.enums.NetworkStatus;
import utilities.logger.Logger;
import websocket.interfaces.WS_Portal;

import javax.persistence.Transient;
import java.util.*;

public class WS_Message_Online_Change_status {

    private static final Logger logger = new Logger(WS_Message_Online_Change_status.class);

    @JsonProperty public static final String messageType = "online_status_change";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  WS_Portal.CHANNEL;

    //------------------------------------------------------------------

     @JsonIgnore public String project_id; // Not required!
    //------------------------------------------------------------------

    @JsonProperty public String model;
    @JsonProperty public UUID model_id;
    @JsonProperty public NetworkStatus status;

//------------------------------------------------------------------

    @JsonIgnore @Transient
    public static void synchronize_online_state_with_becki_public_objects(Class<?> cls, UUID id, boolean online_state) {

        WS_Message_Online_Change_status message = new WS_Message_Online_Change_status(cls, id, online_state ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE);
        ObjectNode json_message =  message.make_request();

        for (WS_Portal portal : Controller_WebSocket.portals.values()) {
            try {
                portal.send(json_message);

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }

    }

    @JsonIgnore @Transient
    public static void synchronize_online_state_with_becki_project_objects(Class<?> cls, UUID id, boolean online_state, UUID project_id) {

        if (project_id == null) {
            throw  new NullPointerException("Project ID is null - Not allow! Something is wrong - Message for Developers and Test Procedures");
        }
        new Thread(() -> {
            try {

                WS_Message_Online_Change_status message = new WS_Message_Online_Change_status(cls, id, online_state ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE);

                List<UUID> list = Model_Project.get_project_becki_person_ids_list(project_id);

                List<UUID> toUnsubscribe = new ArrayList<>();

                ObjectNode json_message =  message.make_request();

                for (UUID person_id : list) {

                    try {

                        // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                        if (Controller_WebSocket.portals.containsKey(person_id)) {

                            WS_Portal portal = Controller_WebSocket.portals.get(person_id);
                            portal.send(json_message);

                        } else {
                            toUnsubscribe.add(person_id);
                        }

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                toUnsubscribe.forEach(Model_Project::becki_person_id_unsubscribe);

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }


// -------------------------------------------------------------------------------------------------------------------

    public WS_Message_Online_Change_status(Class<?> cls, UUID model_id, NetworkStatus status) {

        this.model_id = model_id;
        this.model = cls.getSimpleName().replace("Model_", "");
        this.status = status;

    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", model)
                .put("model_id", model_id.toString())
                .put("status", status.name());
    }
}