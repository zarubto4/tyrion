package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Library_tag;

@ApiModel(description = "Json Model for getting ImportLibrary Filter List",
        value = "ImportLibrary_Filter")
public class Swagger_ImportLibrary_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get library with this tag")
    public Enum_Library_tag tag;
}
