package utilities.swagger.documentationClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_MProgram_SnapShot_settings;

@ApiModel(description = "Json Model for M_Program_SnapShot_Detail",
        value = "M_Program_SnapShot_Detail")
public class Swagger_M_Program_SnapShot_Detail {

    public Swagger_M_Program_SnapShot_Detail(){}

    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_id;
    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_name;
    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_description;
    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_id;
    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_name;
    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_description;


    @Constraints.Required @ApiModelProperty(required = true, readOnly = true, value = "Path To program connection:: https:/grid_app_url.com/{instance_id}/{m_program_id}") public String grid_app_url;

    @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public Enum_MProgram_SnapShot_settings snapshot_settings;

    @Constraints.Required @ApiModelProperty(required = false, readOnly = true, value = "Only if snapshot_settings is \"public_with_token\" ") public String  connection_token;
}
