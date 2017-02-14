package utilities.web_socket.message_objects.compilator_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.compiler.Model_CompilationServer;
import models.compiler.Model_TypeOfBoard;
import play.libs.Json;
import utilities.swagger.outboundClass.Swagger_Compilation_Build_Error;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Make_compilation extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "build";

    public String interface_code;
    public String buildId;
    public String buildUrl;

    @Valid
    public List<Swagger_Compilation_Build_Error> buildErrors = new ArrayList<>();



    @JsonIgnore
    public ObjectNode make_request(Model_TypeOfBoard typeOfBoard, String version_id, String code, ObjectNode includes ) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType",       messageType);
        request.put("messageChannel",    Model_CompilationServer.CHANNEL);
        request.put("target",            typeOfBoard.compiler_target_name);
        request.put("libVersion",        "v0"); // TODO longetrm podle verzí komplační knohovny
        request.put("versionId",         version_id);
        request.put("code",              code);
        request.set("includes", includes);
        return request;
    }



}