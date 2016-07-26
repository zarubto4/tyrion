package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Json Model for specific Tariff and price plan",
        value = "Tariff")
public class Swagger_Tariff {

    public String tariff_name;
    public int maximum_message_per_day_device;

    public int maximum_version_history;
    public int maximum_project;

    public boolean company_details_required;

}



