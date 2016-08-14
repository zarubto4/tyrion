package utilities.swagger.documentationClass;


import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.libs.Json;

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
    public List<User_Files> user_files = new ArrayList<>();

    @Valid
    public List<External_Libraries> external_libraries = new ArrayList<>();


    public static class User_Files {
        public User_Files() {}

        public String file_name;
        public String code;
    }

    public static class External_Libraries {
        public External_Libraries() {
        }

        public String library_name;
        @Valid
        public List<File_Lib> files;

        public static class File_Lib {
            public File_Lib() {
            }

            public String file_name;
            public String content;
        }
    }


// Pomocné metody na separování obsahu *********************************************************************************    

    public ObjectNode includes(){

        ObjectNode includes = Json.newObject();

        if(external_libraries != null)
        for(External_Libraries external_library : external_libraries){
            for(External_Libraries.File_Lib file_lib : external_library.files){
                includes.put(file_lib.file_name , file_lib.content);
            }
        }

        if(user_files != null)
        for(User_Files user_file : user_files){
             includes.put(user_file.file_name , user_file.code);
        }
        
        return  includes;
    }

    
}
