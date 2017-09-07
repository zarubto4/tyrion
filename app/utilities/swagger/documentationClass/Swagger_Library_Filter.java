package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for getting Library Filter List",
        value = "Library_Filter")
public class Swagger_Library_Filter {

    @ApiModelProperty(required = false)
    public String project_id;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_library;

    @ApiModelProperty(required = false, value = "Designed for Administrators for publishing decisions of Community Codes - Without permission, the value is ignored. ")
    public boolean pending_library;

    @ApiModelProperty(hidden = true)
    public List<String> type_of_board_ids = new ArrayList<>();
}
