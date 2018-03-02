package utilities.swagger.output.filter_results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public abstract class _Swagger_Abstract_Default {


    /** Converts this model to JSON
     * @return JSON representation of this model
     */
    @JsonIgnore
    public JsonNode json() {
        return Json.toJson(this);
    }

}
