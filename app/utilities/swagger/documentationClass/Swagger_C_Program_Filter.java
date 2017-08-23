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

    @ApiModelProperty(required = false, value = "Return by Type Of Board - and only codes with permissions")
    public List<String> type_of_board_ids = new ArrayList<>();

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_programs;

    @ApiModelProperty(required = false, value = "Designed for Administrators for publishing decisions of Community Codes - Without permission, the value is ignored. " +
            "[pending, approved, disapproved, edited]")
    public List<String> public_states = new ArrayList<>();

}
