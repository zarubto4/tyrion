package utilities.swagger.documentationClass;


import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.libs.Json;

import javax.validation.Valid;
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
    public String code;

    @Valid
    public List<User_Files> user_files;

    @Valid
    public List<External_Libraries> external_libraries;


    public static class User_Files {
        public User_Files() {
        }

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
    
    public String comprimate_code() {
        for (User_Files user_file : user_files) {
            code += "\n \n " + user_file.code;
        }
        return  code;
    }

    
    public ObjectNode includes(){
        ObjectNode includes = Json.newObject();
        
        for(External_Libraries external_library : external_libraries){
            for(External_Libraries.File_Lib file_lib : external_library.files){
                includes.put(file_lib.file_name , file_lib.content);
            }
        }
        
        return  includes;
    }

    
}
