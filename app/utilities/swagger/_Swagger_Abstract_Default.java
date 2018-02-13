package utilities.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public abstract class _Swagger_Abstract_Default {


    /** Converts this model to JSON
     * @return JSON representation of this model
     */
    public JsonNode json() {
        return Json.toJson(this);
    }

}
