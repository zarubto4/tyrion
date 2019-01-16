package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.Enum_UniqueNameObjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Constraints.Validate
@ApiModel(description = "Json Model for Validation of unique name in project",
        value = "Project_Valid_unique_name")
public class Swagger_Project_Valid_unique_name implements Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required
    @ApiModelProperty(value = "Type", required = true)
    public Enum_UniqueNameObjectType object_type;

    @ApiModelProperty("Required only if object_type is not Project")
    public UUID parent_id;

    @Constraints.Required
    public String name;

    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (parent_id == null && object_type != Enum_UniqueNameObjectType.Project) {
            errors.add(new ValidationError("parent_id", "Parent id is required, unless the object_type is Project."));
        }

        if (parent_id == null && object_type == Enum_UniqueNameObjectType.HomerServer) {
            errors.add(new ValidationError("parent_id", "Parent id is required, unless the object_type is Project."));
        }

        if (parent_id != null && object_type == Enum_UniqueNameObjectType.HomerServer) {
            errors.add(new ValidationError("parent_id", "Parent id is required, unless the object_type is Project."));
        }

        if (parent_id != null && object_type == Enum_UniqueNameObjectType.CodeServer) {
            errors.add(new ValidationError("parent_id", "Parent id must be null!"));
        }

        return errors.isEmpty() ? null : errors;
    }
}
