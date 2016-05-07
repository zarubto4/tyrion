package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_New")
public class Swagger_C_Program_Version_New {


    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = false)
    public String version_description;

    @ApiModelProperty(required = true)
    public String code;

    @Valid
    public List<User_Files> external_files;

    @Valid
    public List<External_Libraries>  external_libraries;



            // Nutný fiktivní contructor pro zhmotnění vnitřních tříd
            public Swagger_C_Program_Version_New(){}


            @ApiModel(description = "Json Model for files in new C program Version", value = "Files")
            public static class User_Files {

                public String file_name;
                public String code;
                public User_Files(){}

            }


            @ApiModel(description = "Json Model for files in new C program Version", value = "Files")
            public static class External_Libraries {

                public String library_name;

                @Valid
                public List<File_Lib>  content;
                public External_Libraries(){}

                @ApiModel(description = "Json Model for files in new C program Version", value = "Files")
                public static class File_Lib {

                    public String file_name;
                    public String content;
                    public File_Lib(){}

                }

            }


}
