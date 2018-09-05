package utilities.swagger.input;

import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

public class Swagger_GridWidgetVersion_GridApp_source {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID id;

    @ApiModelProperty(required = true, readOnly = true)
    public String logic_json;

}
