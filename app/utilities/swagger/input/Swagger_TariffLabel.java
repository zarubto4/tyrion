package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for getting Type_Of_Block Filter List",
          value = "TariffLabel")
public class Swagger_TariffLabel {

    public Swagger_TariffLabel() {}

    @Constraints.Required public String description;
    @Constraints.Required public String icon;

}


