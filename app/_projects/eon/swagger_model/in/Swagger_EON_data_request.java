package _projects.eon.swagger_model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for getting data for EON project",
        value = "eon_data_request")
public class Swagger_EON_data_request {

    @ApiModelProperty(value = "begin of time range", required = true)
    public Long startDate = 1549223285L;

    @ApiModelProperty(value = "end of time range", required = true)
    public Long endDate = 1704067200L;

    @ApiModelProperty(value = "code of value",required = true)
    public String obis_code = "1.0.1.8.0.255";

    @ApiModelProperty(value = "interval in seconds",required = true)
    public Integer interval = 900;

    @ApiModelProperty(value = "list of hardwares", required = true)
    public List<String> hardwares;
}
