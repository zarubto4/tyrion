package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.input.Swagger_GridWidgetVersion_GridApp_source;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "Terminal_Connection_Summary")
public class Swagger_Mobile_Connection_Summary {

    @ApiModelProperty(required = true, readOnly = true, value = "WS address -> replaces token in URL by verified token from the other APIs if its required by query") public String grid_app_url;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                            public String m_program;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                            public UUID m_project_id;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                            public UUID m_program_id;
    @ApiModelProperty(required = true, readOnly = true, value = "M Program code in String")                                                            public UUID m_program_version_id;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")        public UUID instance_id;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")        public List<Swagger_GridWidgetVersion_GridApp_source> source_code_list = new ArrayList<>();

}
