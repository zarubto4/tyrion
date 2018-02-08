package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "",
        value = "Instance_Filter")
public class Swagger_Instance_Filter {

    @ApiModelProperty(required = false) public UUID project_id;
    @ApiModelProperty(required = false) public List<UUID> server_unique_ids = new ArrayList<>();
    @ApiModelProperty(required = false) public List<String> instance_types = new ArrayList<>();

}
