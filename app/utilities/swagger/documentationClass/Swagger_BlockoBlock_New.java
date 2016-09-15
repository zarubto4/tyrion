package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new BlockoBlock Model",
        value = "BlockoBlock_New")
public class Swagger_BlockoBlock_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String general_description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Must be true, if user want his BlockoBlock in new TypeOfBlock group.")
    public boolean new_type_of_block;

    @Constraints.Required
    @ApiModelProperty(value = "Can be null, if user do not know, admin will decide which type", required = false)
    public String type_of_block_id;

    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = false, value = "Can be null, if type exists. Required if 'new_type_of_block' = true. Length must be between 8 and 60 characters.")
    public String type_of_block_name;

    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = false, value = "Can be null, if type exists. Required if 'new_type_of_block' = true. Length must be between 24 and 255 characters.")
    public String type_of_block_general_description;

    @ApiModelProperty(required = false, value = "\n \n if you want make private TypeOfBlock group. You have to have \"project_id\" parameter in Json.",
            allowableValues = "Value can be null or contains project_id")
    public String project_id;

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String design_json;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String logic_json;
}
