package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Homer",
          value = "Homer_New")
public class Swagger_Homer_New {

    @Constraints.Required
    @ApiModelProperty(value = "Required unique value - its MacAddress of Homer", required = true)
    public String homer_id;


    @Constraints.Required
    @ApiModelProperty(value = "Its a name of Device", example = "Raspberry, ServerPC..", required = true)
    public String type_of_device;
}
