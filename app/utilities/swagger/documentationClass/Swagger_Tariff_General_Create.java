package utilities.swagger.documentationClass;

import io.swagger.annotations.Api;
import play.data.validation.Constraints;

@Api(hidden = true)
public class Swagger_Tariff_General_Create {

    @Constraints.Required public String id;
    @Constraints.Required public String tariff_name;
    @Constraints.Required public String identificator;
    @Constraints.Required public String tariff_description;

    @Constraints.Required public String color;

    @Constraints.Required public boolean company_details_required;
    @Constraints.Required public boolean required_payment_mode;
    @Constraints.Required public boolean required_payment_method;

    @Constraints.Required public boolean credit_card_support;
    @Constraints.Required public boolean bank_transfer_support;


    @Constraints.Required public boolean mode_annually;
    @Constraints.Required public boolean mode_credit;

    @Constraints.Required public boolean free;

    @Constraints.Required public boolean required_paid_that;

    @Constraints.Required public Integer  number_of_free_months;


    @Constraints.Required public Double usd;
    @Constraints.Required public Double eur;
    @Constraints.Required public Double czk;

}
