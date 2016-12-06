package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;

@ApiModel(description = "Json Model for Public Version of C_program",
        value = "C_Program_Version_Short_Detail")
public class Swagger_C_Program_Version_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;


    @JsonProperty @Transient public boolean update_permission;
    @JsonProperty @Transient public boolean delete_permission;
}
