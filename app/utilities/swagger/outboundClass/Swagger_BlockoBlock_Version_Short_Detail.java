package utilities.swagger.outboundClass;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;

import java.util.Date;

@ApiModel(description = "Json Model details of Version of BlockoBlock",
        value = "BlockoBlock_Version_Short_Detail")
public class Swagger_BlockoBlock_Version_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public Date date_of_create;

    @ApiModelProperty(required = true, readOnly = true)
    public String design_json;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

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
