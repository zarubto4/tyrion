package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting Type_Of_Widget Filter List",
        value = "Type_Of_Widget_Filter")
public class Swagger_Type_Of_Widget_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Type_Of_Widgets of given project")
    public String project_id;

    @ApiModelProperty(required = true, value = "Use 'true' for private Type_Of_Widget or 'false' for non-private")
    public Boolean private_type;
}
