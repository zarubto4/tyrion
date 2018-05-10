package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for getting Article Filter List",
        value = "Article_Filter")
public class Swagger_Article_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = false, value = "List of Tagst")
    public List<String> tags;

}
