package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_C_Program_Version_Approve_WithChanges {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String id;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String name;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String description;

    @ApiModelProperty(required = false)
    public String reason;
}
