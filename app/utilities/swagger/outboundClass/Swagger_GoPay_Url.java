package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model with url for GoPay Terminal",
        value = "GoPay_Url")
public class Swagger_GoPay_Url {

    @ApiModelProperty(required = true, readOnly = true)
    public String gw_url;

}
