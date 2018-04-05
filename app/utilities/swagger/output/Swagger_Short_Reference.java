package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(value = "Short_Reference", description = "Model of Reference")
public class Swagger_Short_Reference extends _Swagger_Abstract_Default {

    public Swagger_Short_Reference(UUID id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @ApiModelProperty(required = true) @JsonProperty() public String name;
    @ApiModelProperty(required = true) @JsonProperty() public String description;
    @ApiModelProperty(required = true) @JsonProperty() public UUID id;
}
