package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Person_Short_Detail",
        value = "Person_Middle_Detail")
public class Swagger_Person_Middle_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String nick_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String full_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String mail;

}