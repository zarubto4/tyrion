package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "B_Program Light (only few properties)",
        value = "B_Program_Short_Detail")
public class Swagger_B_Program_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String id;
    @ApiModelProperty(required = true, readOnly = true) public String name;
    @ApiModelProperty(required = true, readOnly = true) public String description;

    @ApiModelProperty(required = true, readOnly = true) public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true) public boolean update_permission;
    @ApiModelProperty(required = true, readOnly = true) public boolean delete_permission;

}
