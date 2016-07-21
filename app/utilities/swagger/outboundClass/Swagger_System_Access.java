package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.PersonPermission;
import models.person.SecurityRole;

import java.util.List;

@ApiModel(description = "Json Model for System Access>",
        value = "System_Access")
public class Swagger_System_Access {

    @ApiModelProperty(required = true, readOnly = true)
    public List<SecurityRole> roles;

    @ApiModelProperty(required = true, readOnly = true)
    public List<PersonPermission> permissions;
}
