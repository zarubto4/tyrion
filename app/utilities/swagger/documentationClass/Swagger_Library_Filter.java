package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model for getting Library Filter List",
        value = "Library_Filter")
public class Swagger_Library_Filter {

    @ApiModelProperty(required = true, value = "The tag describes what the library is doing")
    public String project_id;

}
