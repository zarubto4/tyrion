package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
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

    @Valid
    public List<VersionFiles> files;


           public Swagger_C_Program_Version(){}

            @ApiModel(description = "Json Model for files in new C program Version", value = "Files")
            public static class VersionFiles {

                public String file_name;
                public String content;

                public VersionFiles(){}

            }

}
