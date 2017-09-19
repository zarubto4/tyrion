package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for admin response for publishing GridWidget",
        value = "GridWidget_Publish_Response")
public class Swagger_GridWidget_Publish_Response {

    @ApiModelProperty(required = false, readOnly = true, value = "Required only if decision == true")
    public String grid_widget_type_of_widget_id;

    @Constraints.Required
    public String version_id;

    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = true)
    public String version_description;


    @ApiModelProperty(required = true)
    public String program_name;

    @ApiModelProperty(required = true)
    public String program_description;

    @Constraints.Required
    public boolean decision;

    public String reason;
}
