package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_New")
public class Swagger_C_Program_Version_Update {
    // Nutný fiktivní contructor pro zhmotnění vnitřních tříd
    public Swagger_C_Program_Version_Update(){}


    @ApiModelProperty(required = true)
    public String code;

    @Valid public List<User_Files> external_files;

    @Valid public List<External_Libraries>  external_libraries;



            public static class User_Files {
                public User_Files(){}

                public String file_name;
                public String code;


            }


            public static class External_Libraries {
                public External_Libraries(){}

                       public String library_name;
                @Valid public List<File_Lib>  content;



                public static class File_Lib {
                    public File_Lib(){}

                    public String file_name;
                    public String content;
                }

            }


}
