package utilities.document_db.document_objects;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_ProductExtension;
import play.libs.Json;
import utilities.Server;

import java.util.Date;
import java.util.UUID;

public class DM_RestApiRequestRecord {

    // MessageContents

    public String custom_id;
    public Long time;

    // MessageType
    @JsonIgnore public static final String document_type = "PRODUCT_EXTENSION";
    @JsonIgnore private static final String document_type_sub_type = "RESTAPI_REQUEST";

    @JsonIgnore
    public static ObjectNode make_request(String custom_id) {

        ObjectNode request = Json.newObject();

        // Required variables by Mongo
        request.put("id", UUID.randomUUID().toString() + UUID.randomUUID().toString());
        request.put("collection_type", Model_ProductExtension.class.getSimpleName());
        request.put("document_type", document_type);
        request.put("document_type_sub_type", document_type_sub_type);
        request.put("server_version", Server.version);

        request.put("custom_id", custom_id);
        request.put("time", new Date().getTime() );
        return request;
    }

}
