package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Type_Of_Widget Light (only few properties)",
        value = "Type_Of_Widget_Light")
public class Swagger_Type_Of_Widget_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_widget_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_widget_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_widget_description;
}
