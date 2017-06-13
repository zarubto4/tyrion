package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GridWidgetVersion;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
        value = "Terminal_Connection_Summary")
public class Swagger_Mobile_Connection_Summary {

    @ApiModelProperty(required = true, readOnly = true, value = "WS adress -> replaces token in URL by verified token from the other APIs if its required by query") public String grid_app_url;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                             public String m_program;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")        public String instance_id;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")        public List<Swagger_GridWidgetVersion_Short_Detail> source_code_list = new ArrayList();

}
