package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model M_Project - only basic information",
          value = "M_Project_Short_Detail")
public class Swagger_M_Project_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = false, readOnly = true)
    public List<Swagger_M_Program_Short_Detail> programs = new ArrayList<>();
}
