package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.grid.Screen_Size_Type;
import play.data.validation.Constraints;

import java.util.List;

@ApiModel(description = "Json Model with two lists<Screen_Size_Type>",
          value = "Screen_Size_Type_Combination")
public class Swagger_Screen_Size_Type_Combination {

    @Constraints.MinLength(value = 8, message = "This list contain private Screen_Size_Type objects from all users projects")
    @ApiModelProperty(required = true, readOnly = true)
    public List<Screen_Size_Type> private_types;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Screen_Size_Type> public_types;
}
