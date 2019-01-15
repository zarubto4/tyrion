package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for upload File to server",
          value = "Base64_File")
public class Swagger_BASE64_FILE {

    @Constraints.Required
    @ApiModelProperty(required = false, value = "The file is encoded in base64. If it is null - it is a command to delete a file")
    @Constraints.MaxLength(value = 4333333 , message = "Max Length is 4Mb")
    public String file;


}
