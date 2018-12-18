package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "GSM_CRD_Filter")
public class Swagger_GSM_CDR_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = true)
    @Constraints.Required
    public UUID project_id;

    @ApiModelProperty(required = true)
    @Constraints.Required
    public List<UUID> gsm_ids;

}
