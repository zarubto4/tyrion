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
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String version_name;


    @ApiModelProperty(required = false, value = "version_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;


    @ApiModelProperty(required = true)
    public String main;

    @ApiModelProperty(required = false)
    @Valid public List<User_Files> user_files;

    @ApiModelProperty(required = false)
    @Valid public List<External_Libraries>  external_libraries;



    public static class User_Files {
        public User_Files(){}

        public String file_name;
        public String code;

    }

    public static class External_Libraries {
        public External_Libraries(){}

        public String library_name;
        @Valid public List<File_Lib> files;

        public static class File_Lib {
            public File_Lib(){}

            public String file_name;
            public String content;
        }
    }


}
