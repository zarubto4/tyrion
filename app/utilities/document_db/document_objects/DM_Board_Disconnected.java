package utilities.document_db.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tc.util.UUID;
import play.libs.Json;

import java.util.Date;

public class DM_Board_Disconnected {

    // @JsonIgnore public static final String document_type = "device_status";
    @JsonIgnore private static final String document_type_sub_type = "device_disconnect";

    @JsonIgnore
    public ObjectNode make_request(String board_id) {

        ObjectNode request = Json.newObject();
        request.put("id", UUID.getUUID().toString());
        request.put("document_type", DM_Board_Connect.document_type);
        request.put("document_type_sub_type", document_type_sub_type);
        request.put("device_id", board_id);
        request.put("time", new Date().getTime() );
        return request;
    }

}
