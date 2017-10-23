package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;
import utilities.enums.Enum_Compile_status;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Json Model for Public Version of C_program",
        value = "C_Program_Version_Short_Detail")
public class Swagger_C_Program_Version_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public String library_compilation_version;


    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Compile_status status;


    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;


    @ApiModelProperty(required = true, readOnly = true)
    public Swagger_Person_Short_Detail author;

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, readOnly = true, value = "Only for TypeOfBoard. Mark of default Version")
    public Boolean main_mark = null;


    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Approval_state publish_status;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean community_publishing_permission;

}
