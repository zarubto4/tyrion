package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;
import utilities.swagger.documentationClass.Swagger_Library_Record;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Library_Version_Short_Detail",
        value = "Library_Version_Short_Detail")
public class Swagger_Library_Version_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = false, readOnly = true, value = "Optional value")
    public Swagger_Person_Short_Detail author;

    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Approval_state publish_status;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean community_publishing_permission;
}
