package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "Project_AddHardware")
public class Swagger_Project_AddHardware {

    @ApiModelProperty(required = false)
    public List<UUID> group_ids;

    @Constraints.Required
    public UUID project_id;

    @ApiModelProperty(required = false, value = "Optional value on Begging")
    public String name;

    @Constraints.Required
    public String registration_hash;
}
