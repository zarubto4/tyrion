package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Json Model with File_Link",
        value = "File_Link")
public class Swagger_File_Link {

    @ApiModelProperty(value = "Complete URL link", required = true, readOnly = true)
    public String file_link;
}
