package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(value = "Short_Reference", description = "Model of Reference")
public class Swagger_Short_Reference {

    public Swagger_Short_Reference(UUID id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @ApiModelProperty(required = true) @JsonProperty() public String name;
    @ApiModelProperty(required = true) @JsonProperty() public String description;
    @ApiModelProperty(required = true) @JsonProperty() public UUID id;
}
