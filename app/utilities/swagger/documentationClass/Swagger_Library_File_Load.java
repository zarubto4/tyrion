package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model for Library_File_Load",
          value = "Library_File_Load")
public class Swagger_Library_File_Load {

    @ApiModelProperty(required = false)
    @Valid public List<Swagger_Library_Record>  library_files = new ArrayList<>();

}
