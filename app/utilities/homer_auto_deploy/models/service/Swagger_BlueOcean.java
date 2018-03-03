package utilities.homer_auto_deploy.models.service;

import utilities.homer_auto_deploy.models.common.Swagger_ServerRegistration_FormData_ServerRegion;

import java.math.BigDecimal;

public class Swagger_BlueOcean {

    public Integer id;
    public String server_name;
    public String region_slug;
    public String size_slug;

    public BigDecimal price_monthly;
    public BigDecimal price_hourly;

    public BigDecimal price_monthly_set_for_customer;
    public BigDecimal price_hourly_set_for_customer;

}
