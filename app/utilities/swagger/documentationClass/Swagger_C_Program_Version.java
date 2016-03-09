package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_New")
public class Swagger_C_Program_Version{


    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = false)
    public String version_description;


    public List<VersionFiles> files;


    @ApiModel(description = "Json Model for files in new C program Version",
              value = "File")
    public class VersionFiles {
        public String file_name;
        public String content;
    }

}
