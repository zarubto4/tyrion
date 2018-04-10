package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for getting GridWidget Filter List",
        value = "GridWidget_Filter")
public class Swagger_GridWidget_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = false, value = "Include only if you want to get Widgets of given project")
    public UUID project_id;

    @ApiModelProperty(required = false, value = "Only for Admins with permissions")
    public  boolean pending_widgets;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_programs;
}
