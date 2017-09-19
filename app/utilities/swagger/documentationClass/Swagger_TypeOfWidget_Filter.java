package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for getting Type_Of_Widget Filter List",
        value = "Type_Of_Widget_Filter")
public class Swagger_TypeOfWidget_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Type_Of_Widgets of given project")
    public String project_id;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_programs;
}
