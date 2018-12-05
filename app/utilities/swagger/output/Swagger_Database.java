package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for Database",
        value = "Database")
public class Swagger_Database extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = false, readOnly = true )
    public String name;
    @ApiModelProperty(required = false, readOnly = true )
    public String description;
    @ApiModelProperty(required = false, readOnly = true )
    public UUID id;
    @ApiModelProperty(required = false, readOnly = true)
    public String connection_string;
    @ApiModelProperty(required = false, readOnly = true)
    public List<String> collections = new ArrayList<>();
}
