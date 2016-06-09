package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_New")
public class Swagger_C_Program_Version_Update {

    // Nutný fiktivní contructor pro zhmotnění vnitřních tříd
    public Swagger_C_Program_Version_Update(){}


    @Constraints.Required
    @ApiModelProperty(required = true)
    public String code;


    @ApiModelProperty(required = true, value = "Important - if you want compile c++ code and not under version object - you have to specify type_of_board id!")
    public String type_of_board_id;


    @Valid public List<User_Files> user_files;


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
