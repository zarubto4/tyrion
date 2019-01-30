package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for getting data for EON project",
        value = "eon_data_request")
public class Swagger_EON_data_request {

    @ApiModelProperty(value = "begin of time range", required = true)
    public Date startDate;

    @ApiModelProperty(value = "end of time range", required = true)
    public Date endDate;

    @ApiModelProperty(value = "code of value",required = true)
    public String obis_code;

    @ApiModelProperty(value = "interval in seconds",required = true)
    public Integer interval;

    @ApiModelProperty(value = "list of hardwares", required = true)
    public List<String> hardwares;
}
