package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for ImportLibrary Short Detail",
        value = "Library_Short_Detail")
public class Swagger_Library_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public Swagger_Library_Version_Short_Detail last_version;

}
