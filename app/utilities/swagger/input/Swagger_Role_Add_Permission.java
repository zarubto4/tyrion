package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;


@ApiModel(description = "Json Model for add list of Permission to Role",
        value = "Role_Add_Permission")
public class Swagger_Role_Add_Permission {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "List of permission keys")
    public List<String> permissions;

}
