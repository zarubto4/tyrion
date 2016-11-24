package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(value = "Cloud_Compilation_Server_CompilationResult")
public class Swagger_Cloud_Compilation_Server_CompilationResult {

    @Constraints.Required public String buildUrl;
                          public String interface_code;
    @Constraints.Required public String buildId;

    public String status;
    public String error;

    public String buildErrors;
}
