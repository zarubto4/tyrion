package utilities.swagger.documentationClass;

import io.swagger.annotations.Api;
import play.data.validation.Constraints;

@Api(hidden = true)
public class Swagger_Tariff_New {

    public String id;
    @Constraints.Required public String name;
    @Constraints.Required public String identifier;
    @Constraints.Required public String description;

    @Constraints.Required public String color;

    @Constraints.Required public boolean company_details_required;
    @Constraints.Required public boolean payment_method_required;

    @Constraints.Required public boolean credit_card_support;
    @Constraints.Required public boolean bank_transfer_support;

    @Constraints.Required public Double  credit_for_beginning;
}
