package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for data Hardware Group",
        value = "HardwareGroup_Short_Detail")
public class Swagger_HardwareGroup_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)  public String      id;
    @ApiModelProperty(required = true, readOnly = true)  public String      name;
    @ApiModelProperty(required = true, readOnly = true)  public String      description;

}
