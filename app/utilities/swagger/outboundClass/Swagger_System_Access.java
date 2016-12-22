package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Model_Permission;
import models.person.Model_SecurityRole;

import java.util.List;

@ApiModel(description = "Json Model for System Access>",
        value = "System_Access")
public class Swagger_System_Access {

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_SecurityRole> roles;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Permission> permissions;
}
