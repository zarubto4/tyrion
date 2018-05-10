package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for register SIMCard Modul to project",
        value = "GSM_Register")
public class Swagger_GSM_Register {

    @Constraints.Required
    public UUID registration_hash;

    @Constraints.Required
    public UUID project_id;

    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = false, value = "Length must be max 60 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

    @ApiModelProperty(value = "Tags - Optional", required = false)
    public List<String> tags = new ArrayList<>();
}
