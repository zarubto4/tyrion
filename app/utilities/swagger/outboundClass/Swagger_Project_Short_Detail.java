package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Project;
import utilities.enums.Enum_Compile_status;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for specific Project DashBoard ",
        value = "Project_Short_Detail")
public class Swagger_Project_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String project_id;
    @ApiModelProperty(required = true, readOnly = true) public String project_name;

    @ApiModelProperty(required = true, readOnly = true) public String product_id;
    @ApiModelProperty(required = true, readOnly = true) public String product_name;

    @ApiModelProperty(required = true, readOnly = true) public boolean status;
    @ApiModelProperty(required = true, readOnly = true) public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true) public boolean delete_permission;


}
