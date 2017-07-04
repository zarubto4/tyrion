package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import models.Model_MProgramInstanceParameter;
import play.libs.Json;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Instance_terminal_set_snap extends WS_AbstractMessage_Instance {

    // MessageType

    @JsonIgnore
    public static final String message_type = "instance_set_terminals";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/





/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<Model_MProgramInstanceParameter> parameters) {

        List<Terminal_parameter> terminal_parameters = new ArrayList<>();

        for(Model_MProgramInstanceParameter parameter : parameters){

            Terminal_parameter terminal_parameter = new Terminal_parameter();
            terminal_parameter.terminal_id = parameter.id.toString();
            terminal_parameter.target_id = parameter.m_program_version.m_program.m_project_id();
            terminal_parameter.settings = parameter.snapshot_settings;

            terminal_parameters.add(terminal_parameter);
        }

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerInstance.CHANNEL);
        request.set("terminals", Json.toJson(terminal_parameters) );

        return request;

    }


    public class Terminal_parameter{

        public String terminal_id;  // Toto chtěl David U. - nikdo tomu nerozumí proč... Ale je to prý terminologie Homera
        public String target_id;    // Toto chtěl David U. - nikdo tomu nerozumí proč... Ale je to prý terminologie Homera

        public Enum_MProgram_SnapShot_settings settings;    // absolutely_public, with token etc.

    }


}
