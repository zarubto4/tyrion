package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.time.LocalDate;
import java.util.Date;

@ApiModel(description = "",
        value = "GSM_Credit")
public class Swagger_GSM_Date {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long date_first;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long date_last;
}
