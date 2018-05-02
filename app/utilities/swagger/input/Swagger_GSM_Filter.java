package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Constraint;
import java.util.UUID;

@ApiModel(description = "",
        value = "GSM_Filter")
public class Swagger_GSM_Filter extends _Swagger_filter_parameter{
    @Constraints.Required
    @ApiModelProperty(required = true) public UUID project_id;
}
