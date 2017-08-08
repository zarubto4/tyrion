package utilities.document_db.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tc.util.UUID;
import models.Model_Board;
import play.libs.Json;
import utilities.Server;

import java.util.Date;

public class DM_Board_BackupIncident {

    // MessageType
    @JsonIgnore
    public static final String document_type = "DEVICE_STATUS";

    @JsonIgnore private static final String document_type_sub_type_succes_backup = "DEVICE_BACKUP_INCIDENT";    // Když se úspěšně deska přepne na backup
    @JsonIgnore private static final String document_type_sub_type_backup_incident = "DEVICE_BACKUP_INCIDENT";          // Když se deska sekne a zapne se backup

    @JsonIgnore
    public static ObjectNode make_request_success_backup(String board_id) {

        ObjectNode request = Json.newObject();

        // Required variables by Mongo
        request.put("id", UUID.getUUID().toString() + UUID.getUUID().toString());
        request.put("collection_type", Model_Board.class.getSimpleName());
        request.put("document_type", document_type);
        request.put("document_type_sub_type", document_type_sub_type_succes_backup);
        request.put("server_version", Server.server_version);

        request.put("hardware_id", board_id);
        request.put("time", new Date().getTime() );
        return request;
    }

    @JsonIgnore
    public static ObjectNode make_request_backup_incident(String board_id) {

        ObjectNode request = Json.newObject();

        return request;
    }


}
