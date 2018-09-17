package utilities.swagger.output.filter_results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import utilities.model.JsonSerializable;

public abstract class _Swagger_Abstract_Default implements JsonSerializable {


    /** Converts this model to JSON
     * @return JSON representation of this model
     */
    @JsonIgnore
    @Override
    public JsonNode json() {
        return Json.toJson(this);
    }

}
