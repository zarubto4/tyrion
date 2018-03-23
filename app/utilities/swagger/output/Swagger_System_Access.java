package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Permission;
import models.Model_Role;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;

@ApiModel(description = "Json Model for System Access>",
        value = "System_Access")
public class Swagger_System_Access extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Role> roles;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Permission> permissions;
}
