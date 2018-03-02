package utilities.document_db.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import utilities.Server;
import utilities.enums.FirmwareType;

import java.util.Date;
import java.util.UUID;

public class DM_Board_VersionChange {

    @JsonIgnore public static final String document_type = DM_Board_Connect.document_type;
    @JsonIgnore private static final String document_type_sub_type = "VERSION_CHANGE";

    @JsonIgnore
    public ObjectNode make_request(String board_id, FirmwareType firmware_type, String version_id) {

        ObjectNode request = Json.newObject();

        // Required variables by Mongo
        request.put("id", UUID.randomUUID().toString() + UUID.randomUUID().toString());
        request.put("collection_type", Model_Hardware.class.getSimpleName());
        request.put("document_type", document_type);
        request.put("document_type_sub_type", document_type_sub_type);
        request.put("server_version", Server.version);

        request.put("hardware_id", board_id);
        request.put("firmware_type", firmware_type.name());
        request.put("version_id", version_id);
        request.put("time", new Date().getTime() );
        return request;
    }
}
