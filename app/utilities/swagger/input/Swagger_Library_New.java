package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "Library_New", description = "Json Model for new Library")
public class Swagger_Library_New extends Swagger_NameAndDesc_ProjectIdOptional {

    @ApiModelProperty(hidden = true)
    public List<UUID> hardware_type_ids = new ArrayList<>();
}