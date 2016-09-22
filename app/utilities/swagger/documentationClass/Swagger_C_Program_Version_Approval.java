package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_C_Program_Version_Approval {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String id;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public boolean decision;

    @ApiModelProperty(required = false)
    public String reason;
}
