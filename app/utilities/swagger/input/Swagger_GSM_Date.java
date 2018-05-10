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
    @ApiModelProperty(required = true) public Date date_first;

    @Constraints.Required
    @ApiModelProperty(required = true) public Date date_last;
}
