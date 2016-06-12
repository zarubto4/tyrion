package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model for Filtering LibraryGroups",
          value = "LibraryGroup_Filter")
public class Swagger_LibraryGroup_Filter {

    @ApiModelProperty(value = "List of processor.id", required = false)
    public List<String> processors_id;

    @ApiModelProperty(required = false)
    public String group_name;

    @ApiModelProperty(value = "Value of order", allowableValues = "group_name, id, date_of_create" , required = false)
    public String order;

    @ApiModelProperty(value = "Set type of Order -> ascending / descending, If you used order - its required!!!", allowableValues = "asc OR desc" , required = false)
    public String value;


}
