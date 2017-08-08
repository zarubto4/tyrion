package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for getting C_Program Filter List",
        value = "C_Program_Filter")
public class Swagger_C_Program_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get C_Programs of given project")
    public String project_id;

    @ApiModelProperty(hidden = true)
    public List<String> type_of_board_ids = new ArrayList<>();

    @ApiModelProperty(hidden = true)
    public boolean public_programs;
}
