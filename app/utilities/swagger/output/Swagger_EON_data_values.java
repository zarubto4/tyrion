package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "Json Model for EON",
        value = "EON Data values")
public class Swagger_EON_data_values {
    @ApiModelProperty(required = true, readOnly = true )
    public String hardware;
    @ApiModelProperty(required = true, readOnly = true )
    public Date date;
    @ApiModelProperty(required = true, readOnly = true )
    public Double avg;
}
