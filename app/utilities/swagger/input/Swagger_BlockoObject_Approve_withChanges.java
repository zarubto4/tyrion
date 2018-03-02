package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "",
        value = "BlockoObject_Approve_withChanges")
public class Swagger_BlockoObject_Approve_withChanges extends Swagger_NameAndDescription {

    @Constraints.Required
    public UUID object_id;

    @Constraints.Required
    public String state;

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    public String version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;

    @Constraints.Required
    public String design_json;

    @Constraints.Required
    public String logic_json;

    @Constraints.Required
    public String reason;
}
