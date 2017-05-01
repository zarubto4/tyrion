package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_Update")
public class Swagger_C_Program_Version_Update {

    // Nutný fiktivní contructor pro zhmotnění vnitřních tříd
    public Swagger_C_Program_Version_Update() {
    }

    @ApiModelProperty(required = false, value = "Required only if user compile code not under C++ code version (where compilation can found type_of_board)")
    public String type_of_board_id;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String main;

    @Valid
    public List<User_File> user_files = new ArrayList<>();

    @Valid
    public List<String> library_files = new ArrayList<>();


    public static class User_File {
        public User_File() {}

        public String file_name;
        public String code;
    }

    public static class Library_File {
        public Library_File() {}

        public String file_name;
        public String content;
        }

}
