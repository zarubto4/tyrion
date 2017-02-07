package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting Blocko_Block Filter List",
          value = "Base64_File")
public class Swagger_BASE64_FILE {

    @ApiModelProperty(required = false, value = "The file is encoded in base64. If it is null - it is a command to delete a file")
    public String file;

}
