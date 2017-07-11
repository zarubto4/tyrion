package web_socket.message_objects.compilator_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_CompilationServer;
import models.Model_TypeOfBoard;
import play.libs.Json;
import utilities.swagger.outboundClass.Swagger_Compilation_Build_Error;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Make_compilation extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "compilator_build";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public String interface_code;        // Optional value - Only if there is no build_errors
    public String build_id;              // Optional value - Only if there is no build_errors
    public String build_url;             // Optional value - Only if there is no build_errors

    @Valid public List<Swagger_Compilation_Build_Error> build_errors = new ArrayList<>();



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_TypeOfBoard typeOfBoard, String version_id, String code, ObjectNode includes ) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type",       messageType);
        request.put("message_channel",    Model_CompilationServer.CHANNEL);
        request.put("target",             typeOfBoard.compiler_target_name);
        request.put("library_version",    "v0"); // TODO longterm podle verzí komplační knihovny
        request.put("version_id",         version_id);
        request.put("code",               code);
        request.set("includes", includes);
        return request;
    }



}