package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;

@ApiModel(description = "Json Model with details of C_program>",
        value = "C_program_Short_Detail")
public class Swagger_C_program_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_board_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_board_name;

    @JsonProperty @Transient public boolean update_permission;
    @JsonProperty @Transient public boolean delete_permission;
}
