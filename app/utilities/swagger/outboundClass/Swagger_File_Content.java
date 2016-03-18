package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for file content",
        value = "File_Content")
public class Swagger_File_Content {

    @ApiModelProperty(required = true, readOnly = true, value = "Content in String")
    public String content;

    @ApiModelProperty(required = true, readOnly = true)
    public String file_name;
}
