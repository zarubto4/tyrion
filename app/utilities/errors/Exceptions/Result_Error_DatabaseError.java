package utilities.errors.Exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class Result_Error_DatabaseError extends _Base_Result_Exception {

    private JsonNode form_error;

    public Result_Error_DatabaseError(String form_error, String stack_trace) {
        super(form_error.toString());

        ObjectNode json = Json.newObject();
        json.put("error", form_error);
        json.put("stack_trace", stack_trace);
        this.form_error = json;
    }

    public JsonNode getForm_error() {
        return form_error;
    }
}
