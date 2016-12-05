package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GridWidget Light (only few properties)",
        value = "GridWidget_Light")
public class Swagger_GridWidget_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_description;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_type_of_widget_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_type_of_widget_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String grid_widget_type_of_widget_description;
}
