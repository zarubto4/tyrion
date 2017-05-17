package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Blocko_Block Light (only few properties)",
        value = "Blocko_Block_Short_Detail")
public class Swagger_Blocko_Block_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true, value = "Id of Block in Blocko Group (TypeOfBlock)")
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_BlockoBlock_Version_Short_Detail> versions = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

}
