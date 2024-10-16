package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for Hardware Group Filter List ",
          value = "HardwareGroup_Filter")
public class Swagger_HardwareGroup_Filter extends _Swagger_filter_parameter {

    @ApiModelProperty(required = true)
    @Constraints.Required
    public UUID project_id;

    @ApiModelProperty(value = "List of instance_snapshot.id", required = false)
    public List<UUID> instance_snapshots;
}

