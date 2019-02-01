package _projects.eon.swagger_model.out;

import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.Date;

public class Swagger_EON_measurment extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, readOnly = true )
    public Double value;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer")
    public Date time;
}
