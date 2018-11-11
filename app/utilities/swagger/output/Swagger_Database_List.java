package utilities.swagger.output;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for Database List",
        value = "Database list")
public class Swagger_Database_List extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, value = "List of databases")
    public List<Swagger_Database> databases;

    @ApiModelProperty(required = true, value = "Connection string")
    public String connection_string;

}
