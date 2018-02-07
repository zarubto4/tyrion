package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for creating new tariff.",
        value = "Tariff_New")
public class Swagger_Tariff_New extends Swagger_NameAndDescription {

    @Constraints.Required public String identifier;
    @Constraints.Required public String color;
    @Constraints.Required public String awesome_icon;

    @Constraints.Required public boolean company_details_required;
    @Constraints.Required public boolean payment_method_required;
    @Constraints.Required public boolean payment_details_required;

    @Constraints.Required public Double  credit_for_beginning;

    @Valid public List<Swagger_TariffLabel> labels = new ArrayList<>();
}
