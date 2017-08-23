package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for change description for Group Role Permission",
        value = "Role_Short_Detai")
public class Swagger_Role_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String id;
    @ApiModelProperty(required = true, readOnly = true) public String name;
    @ApiModelProperty(required = true, readOnly = true) public String description;

    @ApiModelProperty(required = true, readOnly = true) public boolean update_permission;
    @ApiModelProperty(required = true, readOnly = true) public boolean delete_permission;

}
