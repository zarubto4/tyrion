package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new BlockoBlock Model",
        value = "BlockoBlock_New")
public class Swagger_BlockoBlock_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true, value = "The name must have at least 8 characters")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The name must have at least 24 characters")
    @ApiModelProperty(required = true, value = "The name must have at least 24 characters")
    public String general_description;

    @Constraints.Required
    @ApiModelProperty(value = "Required valid type_of_block_id", required = true)
    public String type_of_block_id;
}
