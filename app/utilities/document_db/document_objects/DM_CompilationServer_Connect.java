package utilities.document_db.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tc.util.UUID;
import models.Model_CompilationServer;
import play.libs.Json;
import utilities.Server;

import java.util.Date;

public class DM_CompilationServer_Connect {

    // MessageType
    @JsonIgnore
    public static final String document_type = "SERVER_STATUS";
    @JsonIgnore private static final String document_type_sub_type = "SERVER_CONNECT";

    @JsonIgnore
    public static ObjectNode make_request(String server_id) {

        ObjectNode request = Json.newObject();

        // Required variables by Mongo
        request.put("id", UUID.getUUID().toString() + UUID.getUUID().toString());
        request.put("collection_type", Model_CompilationServer.class.getSimpleName());
        request.put("document_type", document_type);
        request.put("document_type_sub_type", document_type_sub_type);
        request.put("server_version", Server.server_version);

        request.put("device_id", server_id);
        request.put("time", new Date().getTime() );
        return request;
    }
}


