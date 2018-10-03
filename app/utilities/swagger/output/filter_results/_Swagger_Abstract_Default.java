package utilities.swagger.output.filter_results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.model.JsonSerializable;

public abstract class _Swagger_Abstract_Default implements JsonSerializable {


    /** Converts this model to JSON
     * @return JSON representation of this model
     */
    @JsonIgnore
    @Override
    public ObjectNode json() {
        return (ObjectNode) Json.toJson(this);
    }
}
