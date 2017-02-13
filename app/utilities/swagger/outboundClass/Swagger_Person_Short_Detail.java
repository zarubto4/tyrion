package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Person_Short_Detail",
        value = "Person_Short_Detail")
public class Swagger_Person_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String nick_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String mail;

}