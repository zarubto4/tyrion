package utilities.homer_auto_deploy.models.common;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;

@ApiModel(description = "",
          value = "ServerRegistration_FormData")
public class Swagger_ServerRegistration_FormData extends _Swagger_Abstract_Default {

    public List<Swagger_ServerRegistration_FormData_ServerSize> server_sizes;

}
