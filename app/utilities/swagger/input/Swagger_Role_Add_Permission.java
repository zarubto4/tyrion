package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for add list of Permission to Role",
        value = "Role_Add_Permission")
public class Swagger_Role_Add_Permission {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "List of permission ids")
    public List<UUID> permissions = new ArrayList<>();

}
