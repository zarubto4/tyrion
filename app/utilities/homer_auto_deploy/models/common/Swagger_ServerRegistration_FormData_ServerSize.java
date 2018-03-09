package utilities.homer_auto_deploy.models.common;

import io.swagger.annotations.ApiModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
          value = "ServerRegistration_FormData_ServerSize")
public class Swagger_ServerRegistration_FormData_ServerSize {


    public String slug;
    public String alias;
    public String available;
    public BigDecimal price_monthly;
    public BigDecimal price_hourly;
    public Integer memory;
    public Integer vcpus;

    public List<Swagger_ServerRegistration_FormData_ServerRegion> regions = new ArrayList<>();

}
