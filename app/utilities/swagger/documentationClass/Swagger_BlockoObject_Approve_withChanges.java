package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_BlockoObject_Approve_withChanges {

    @Constraints.Required
    public String object_name;

    @Constraints.Required
    public String object_id;

    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = false, value = "Only if object_name = 'type_of_block'. Length must be between 8 and 60 characters.")
    public String type_of_block_name;

    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = false, value = "Only if object_name = 'type_of_block'. Length must be between 24 and 255 characters.")
    public String type_of_block_general_description;

    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block'. Length must be between 8 and 60 characters.")
    public String blocko_block_name;

    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block'. Length must be between 24 and 255 characters.")
    public String blocko_block_general_description;

    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block'.")
    public String blocko_block_type_of_block_id;

    @Constraints.MinLength(value = 2, message = "The name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block_version'. Length must be between 8 and 60 characters.")
    public String blocko_block_version_name;

    @Constraints.MinLength(value = 8, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block_version'. Length must be between 24 and 255 characters.")
    public String blocko_block_version_description;

    @ApiModelProperty(required = false, value = "Only if object_name = 'blocko_block_version'.")
    public String blocko_block_logic_json;
}
