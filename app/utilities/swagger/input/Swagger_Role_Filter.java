package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "Json Model for getting Role Filter List",
        value = "Role_Filter")
public class Swagger_Role_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = false, value = "Include only if you want to get Roles of given project")
    public UUID project_id;
}
