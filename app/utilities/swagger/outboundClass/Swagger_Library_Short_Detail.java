package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Library Short Detail",
        value = "Library_Short_Detail")
public class Swagger_Library_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<String> type_of_board_names = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;


}
