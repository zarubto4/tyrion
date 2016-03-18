package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Description")
public class Description {

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

}
