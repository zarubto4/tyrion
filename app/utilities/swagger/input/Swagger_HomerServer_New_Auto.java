package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for new Blocko Server",
          value = "Cloud_Homer_Server_NewAuto")
public class Swagger_HomerServer_New_Auto extends Swagger_NameAndDescription {

    @Constraints.Required
    public String region_slug;

    @Constraints.Required
    public String size_slug;

    @ApiModelProperty(value = "Optiona value - only for private server, for public server, user need permission")
    public UUID project_id;
}
