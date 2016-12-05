package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting GridWidget Filter List",
        value = "GridWidget_Filter")
public class Swagger_GridWidget_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Widgets of given project")
    public String project_id;
}
