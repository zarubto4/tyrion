package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Version of BlockoBlock",
        value = "BlockoBlock_ShortVersion ")
public class Swagger_BlockoBlock_ShortVersion {

    @ApiModelProperty(required = true, readOnly = true)    public String id;
    @ApiModelProperty(required = true, readOnly = true)    public String name;

}
