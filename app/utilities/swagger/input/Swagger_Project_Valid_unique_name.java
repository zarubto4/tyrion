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


    @ApiModelProperty(value = "Type", required = false)
    public UUID project_id;


    @ApiModelProperty(value = "Required only if there is control of some Version", required = false)
    public UUID object_id;

    @Constraints.Required
    public String name;






    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (object_type != Enum_UniqueNameObjectType.Project && project_id == null) {
            errors.add(new ValidationError("project_id", "If object_type is not 'Project', project_id is required"));
        }

        if (object_type == Enum_UniqueNameObjectType.Project && project_id != null) {
            errors.add(new ValidationError("project_id", "Must be null if you want to check Project Name"));
        }

        if (object_type.name().contains("Version") && project_id == null) {
            errors.add(new ValidationError("object_id", "If object_type is Version of something, object_id is required"));
        }


        return errors.isEmpty() ? null : errors;
    }
}
