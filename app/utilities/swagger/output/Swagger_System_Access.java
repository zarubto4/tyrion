package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Permission;
import models.Model_Role;

import java.util.List;

@ApiModel(description = "Json Model for System Access>",
        value = "System_Access")
public class Swagger_System_Access {

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Role> roles;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Permission> permissions;
}
