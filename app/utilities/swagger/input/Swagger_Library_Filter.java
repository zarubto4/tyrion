package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for getting Library Filter List",
        value = "Library_Filter")
public class Swagger_Library_Filter {

    @ApiModelProperty(required = false)
    public UUID project_id;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_library;

    @ApiModelProperty(required = false, value = "Designed for Administrators for publishing decisions of Community Codes - Without permission, the value is ignored. ")
    public boolean pending_library;

    @ApiModelProperty(hidden = true)
    public List<UUID> hardware_type_ids = new ArrayList<>();
}
