package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_connected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_connected";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public UUID uuid;
                          public String full_id;
                          public List<String> reset_source = new ArrayList<>();
                          public boolean restored;
                          public Integer connected_counter;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}

