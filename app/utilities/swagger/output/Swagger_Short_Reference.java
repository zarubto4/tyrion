package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.NetworkStatus;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Short_Reference", description = "Model of Reference")
public class Swagger_Short_Reference extends _Swagger_Abstract_Default {

    public Swagger_Short_Reference(UUID id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Swagger_Short_Reference(UUID id, String name, String description, List<String> tags){
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

    public Swagger_Short_Reference(UUID id, String name, String description, List<String> tags, NetworkStatus status){
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.online_state = status;
    }

    public String name;
    public String description;
    public UUID id;
    public List<String> tags;

    @ApiModelProperty(value = "Only for Special Object type like Server, Instance, HW")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty()
    public NetworkStatus online_state;
}
