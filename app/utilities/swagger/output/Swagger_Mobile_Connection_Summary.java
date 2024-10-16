package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.input.Swagger_GridWidgetVersion_GridApp_source;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "Terminal_Connection_Summary")
public class Swagger_Mobile_Connection_Summary extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true, value = "WS address -> replaces token in URL by verified token from the other APIs if its required by query") public String grid_app_url;
    @ApiModelProperty(required = true, readOnly = true, value = "Grid Program code in String")                                                                        public String grid_program;
    @ApiModelProperty(required = true, readOnly = true) public UUID grid_project_id;
    @ApiModelProperty(required = true, readOnly = true) public UUID grid_program_id;
    @ApiModelProperty(required = true, readOnly = true) public UUID grid_program_version_id;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")                       public UUID instance_id;
    @ApiModelProperty(required = true, readOnly = true, value = "Generated Token used for public programs, use as standard verification token")                       public List<Swagger_GridWidgetVersion_GridApp_source> source_code_list = new ArrayList<>();

}
