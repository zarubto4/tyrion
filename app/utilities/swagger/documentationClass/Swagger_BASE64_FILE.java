package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for getting Blocko_Block Filter List",
          value = "Base64_File")
public class Swagger_BASE64_FILE {

    @Constraints.Required
    @ApiModelProperty(required = false, value = "The file is encoded in base64. If it is null - it is a command to delete a file")
    @Constraints.MaxLength(value = 1333333 , message = "Max Length is 1Mb")
    public String file;

}
