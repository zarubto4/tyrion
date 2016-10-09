package utilities.swagger.documentationClass;

import io.swagger.annotations.Api;
import play.data.validation.Constraints;

@Api(hidden = true)
public class Swagger_Tariff_General_Label {

    @Constraints.Required public String general_tariff_id;
    @Constraints.Required public String description;
    @Constraints.Required public String label;
    @Constraints.Required public String icon;

}


