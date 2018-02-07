package utilities.swagger.output;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Json Model for file content",
        value = "File_Content")
public class Swagger_File_Content {

    public String file_in_base64;
}
