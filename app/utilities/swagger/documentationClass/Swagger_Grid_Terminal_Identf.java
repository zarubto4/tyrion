package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_Grid_Terminal_Identf {

    @Constraints.Required
    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")
    public String device_type;

    @Constraints.Required
    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")
    public String device_name;
}
