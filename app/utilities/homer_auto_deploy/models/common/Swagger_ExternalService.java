package utilities.homer_auto_deploy.models.common;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.homer_auto_deploy.models.service.Swagger_BlueOcean;

public class Swagger_ExternalService {

    @Constraints.Required public Enum_ServiceType type;

    @ApiModelProperty(required = false, value = "Optional only if type== BLUE_OCEAN")
    public Swagger_BlueOcean blue_ocean_config;
}
