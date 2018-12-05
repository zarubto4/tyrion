package utilities.swagger.output;

import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Swagger_DatabaseCollectionList extends  _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, readOnly = true)
    public List<String> names = new ArrayList<>();
}
