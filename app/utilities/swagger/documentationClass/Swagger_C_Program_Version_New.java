package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_New")
public class Swagger_C_Program_Version_New {


    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String version_name;


    @ApiModelProperty(required = false, value = "version_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;


    @ApiModelProperty(required = true)
    public String main;

    @ApiModelProperty(required = false)
    @Valid public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = false, value = "Contains IDs of imported Library versions")
    @Valid public List<String>  imported_libraries = new ArrayList<>();

}
