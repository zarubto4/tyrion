package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for getting GridWidget Filter List",
        value = "GridWidget_Filter")
public class Swagger_GridWidget_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Widgets of given project")
    public String project_id;

    @ApiModelProperty(required = false, value = "Only for Admins with permissions")
    public  boolean pending_widget;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_programs;

    @ApiModelProperty(required = false, value = "Return by Type Of Widgets - and only codes with permissions")
    public List<String> type_of_widgets_ids = new ArrayList<>();
}
