package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for Identificator",
        value = "Grid_Terminal_Identf")
public class Swagger_Grid_Terminal_Identf {

    @Constraints.Required
    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")
    public String device_type;

    @Constraints.Required
    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")
    public String device_name;
}
