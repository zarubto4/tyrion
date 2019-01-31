package _projects.eon.swagger_model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.Date;

@ApiModel(description = "Json Model for EON",
        value = "EON_data_value")
public class Swagger_EON_data_values extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, readOnly = true )
    public String hardware;
    @ApiModelProperty(required = true, readOnly = true )
    public Date date;
    @ApiModelProperty(required = true, readOnly = true )
    public Double avg;
}
