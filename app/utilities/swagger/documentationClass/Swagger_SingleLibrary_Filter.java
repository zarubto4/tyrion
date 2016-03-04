package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Swagger_SingleLibrary_Filter {

    @ApiModelProperty(value = "List of processor.id", required = false)
    public List<String> processors_id;

    @ApiModelProperty(required = false)
    public String library_name;

    @ApiModelProperty(required = false)
    public Integer count_from;

    @ApiModelProperty(required = false)
    public Integer count_to;

    @ApiModelProperty(value = "Value of order", allowableValues = "group_name, id" , required = false)
    public String order;

    @ApiModelProperty(value = "Set type of Order -> ascending / descending, If you used order - its required!!!", allowableValues = "asc OR desc" , required = false)
    public String value;

}
