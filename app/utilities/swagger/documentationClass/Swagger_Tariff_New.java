package utilities.swagger.documentationClass;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for creating new tariff.",
        value = "Tariff_New")
public class Swagger_Tariff_New {

    @Constraints.Required public String name;
    @Constraints.Required public String identifier;
    @Constraints.Required public String description;

    @Constraints.Required public String color;

    @Constraints.Required public boolean company_details_required;
    @Constraints.Required public boolean payment_method_required;
    @Constraints.Required public boolean payment_details_required;

    @Constraints.Required public Double  credit_for_beginning;
    @Constraints.Required public Double  monthly_estimate_cost;
}
