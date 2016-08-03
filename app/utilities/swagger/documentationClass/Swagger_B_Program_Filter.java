package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting B_Program Filter List",
        value = "B_Program_Filter")
public class Swagger_B_Program_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get b_programs of given project")
    public String project_id;
}
