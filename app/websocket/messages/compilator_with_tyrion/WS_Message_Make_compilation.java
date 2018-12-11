package websocket.messages.compilator_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HardwareType;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import websocket.interfaces.Compiler;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Make_compilation extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "compilator_build";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public String interface_code;        // Optional value - Only if there is no build_errors
    @Constraints.Required public UUID build_id;                // Optional value - Only if there is no build_errors
    public String build_id_in_firmware;  // Optional value - Only if there is no build_errors
    public String build_url;             // Optional value - Only if there is no build_errors

    @Valid public List<Swagger_Compilation_Build_Error> build_errors = new ArrayList<>();

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HardwareType hardwareType, String library_version, UUID version_id, String code, ObjectNode includes ) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel",    Compiler.CHANNEL);
        request.put("target",             hardwareType.compiler_target_name);
        request.put("library_version",    library_version);
        request.put("version_id",         version_id.toString());
        request.put("code",               code);
        request.set("includes", includes);
        return request;
    }
}