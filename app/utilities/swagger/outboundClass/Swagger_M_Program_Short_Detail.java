package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;

public class Swagger_M_Program_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;


    @JsonProperty @Transient public boolean edit_permission;
    @JsonProperty @Transient public boolean delete_permission;
}
