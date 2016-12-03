package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "Json Model for Version of GridWidgetVersion short",
        value = "GridWidgetVersion_short")
public class Swagger_GridWidgetVersion_short {

    @ApiModelProperty(required = true, readOnly = true)    public String id;
    @ApiModelProperty(required = true, readOnly = true)    public String name;
    @ApiModelProperty(required = true, readOnly = true)    public String description;
    @ApiModelProperty(required = true, readOnly = true)    public Date date_of_create;

    @ApiModelProperty(required = true, readOnly = true)     public String design_json;

}
