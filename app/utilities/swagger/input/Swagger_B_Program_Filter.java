package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for getting B_Program Filter List",
        value = "B_Program_Filter")
public class Swagger_B_Program_Filter extends _Swagger_filter_parameter {

    @ApiModelProperty(required = false, value = "Include only if you want to get b_programs of given project")
    @Constraints.Required
    public UUID project_id;


/** FIND BY SOME VALUE  ------------------------------------------------------------------------------------------ **/

    @ApiModelProperty(value = "Hardware Alias Name - substring supported", required = false)
    public String name;
}
