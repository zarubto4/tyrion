package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Publishing_type;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "TypeOfBlock details Json model (only few properties)",
          value = "TypeOfBlock_Short_Detail")
public class Swagger_TypeOfBlock_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer order_position;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Blocko_Block_Short_Detail> blocko_blocks = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true, value = "Visible only for administrator with permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active;

    @ApiModelProperty(required = false, readOnly = true)
    public Enum_Publishing_type publish_type;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;
}
