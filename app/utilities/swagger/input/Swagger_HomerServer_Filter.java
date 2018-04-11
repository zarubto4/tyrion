package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.HomerType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Filtering Homer Servers",
          value = "HomerServer_Filter")
public class Swagger_HomerServer_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = false, value = "Default if empty is public")
    public List<HomerType> server_types = new ArrayList<>();

    @ApiModelProperty(required = false)
    public UUID project_id;

}
