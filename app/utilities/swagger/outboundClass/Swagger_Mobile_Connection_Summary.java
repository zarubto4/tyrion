package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "",
        value = "Terminal_Connection_Summary")
public class Swagger_Mobile_Connection_Summary {

    @ApiModelProperty(required = true, readOnly = true, value = "replaces token in URL by verified token from the other APIs if its required by query") public String url;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                             public String m_program;
    @ApiModelProperty(required = false, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")        public String token;
}
