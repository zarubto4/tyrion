package websocket.messages.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Instance;
import play.libs.Json;
import utilities.enums.GridAccess;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;
import java.util.UUID;

public class WS_Message_Instance_set_terminals extends WS_AbstractMessage_Instance {

    // MessageType

    @JsonIgnore
    public static final String message_type = "instance_set_terminals";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<UUID> terminalIds) {
/*
        List<Terminal_parameter> terminal_parameters = new ArrayList<>();

        for (Model_MProgramInstanceParameter parameter : parameters) {

            Terminal_parameter terminal_parameter = new Terminal_parameter();
            terminal_parameter.terminal_id = parameter.id.toString();
            terminal_parameter.target_id = parameter.m_program_version.m_program.m_project_id();
            terminal_parameter.settings = parameter.snapshot_settings;

            terminal_parameters.add(terminal_parameter);
        }
*/
        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Instance.CHANNEL);
        request.set("terminals", Json.toJson(terminalIds) );

        return request;
    }

    public class Terminal_parameter {

        public String terminal_id; // M_Program_snap_parameter_ID
        public String target_id;   // m_project_id

        public GridAccess settings; // absolutely_public, with token etc.
    }
}
