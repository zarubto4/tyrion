package utilities.errors.Exceptions;

import com.fasterxml.jackson.databind.JsonNode;

public class Result_Error_InvalidBody extends _Base_Result_Exception {

    private JsonNode form_error;

    public Result_Error_InvalidBody(JsonNode form_error) {
        super();
        this.form_error = form_error;
    }

    public JsonNode getForm_error() {
        return form_error;
    }
}
