package utilities.swagger.documentationClass;

import io.swagger.annotations.Api;
import play.data.validation.Constraints;

@Api(hidden = true)
public class Swagger_Tariff_General_Extension_Create {

    @Constraints.Required public String id;
    @Constraints.Required public String name;
    @Constraints.Required public String description;

    @Constraints.Required public String color;


    @Constraints.Required public Double usd;
    @Constraints.Required public Double eur;
    @Constraints.Required public Double czk;
}
