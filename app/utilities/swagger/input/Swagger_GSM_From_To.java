package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "",
        value = "GSM_From_To")
public class Swagger_GSM_From_To {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long from = 0L;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long to = 0L;
}
