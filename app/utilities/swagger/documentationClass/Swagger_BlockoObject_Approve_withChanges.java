package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "",
        value = "BlockoObject_Approve_withChanges")
public class Swagger_BlockoObject_Approve_withChanges {

    @Constraints.Required
    public String object_id;

    @Constraints.Required
    public String state;

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    public String blocko_block_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String blocko_block_general_description;

    @Constraints.Required
    public String blocko_block_type_of_block_id;

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    public String blocko_block_version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String blocko_block_version_description;

    @Constraints.Required
    public String blocko_block_design_json;

    @Constraints.Required
    public String blocko_block_logic_json;

    @Constraints.Required
    public String reason;
}
