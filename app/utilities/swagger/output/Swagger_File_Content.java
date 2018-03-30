package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for file content",
        value = "File_Content")
public class Swagger_File_Content extends _Swagger_Abstract_Default {

    public String file_in_base64;
}
