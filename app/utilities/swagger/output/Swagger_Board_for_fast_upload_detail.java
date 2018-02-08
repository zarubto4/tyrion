package utilities.swagger.output;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.BoardUpdateCollision;

import java.util.UUID;

@ApiModel(description = "Json Model for hardware with details for fast upload",
        value = "Board_for_fast_upload_detail")
public class Swagger_Board_for_fast_upload_detail {

    @ApiModelProperty(required = true, readOnly = true)  public UUID id;
    @ApiModelProperty(required = true, readOnly = true)  public String name;
    @ApiModelProperty(required = true, readOnly = true)  public String description;

    @ApiModelProperty(required = true, readOnly = true)  public BoardUpdateCollision collision;

    @ApiModelProperty(required = true, readOnly = true)  public UUID hardware_type_id;
    @ApiModelProperty(required = true, readOnly = true)  public String hardware_type_name;
}
