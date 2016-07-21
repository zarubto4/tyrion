package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting TypeOfBoard by Filter",
        value = "TypeOfBoard_Filter")
public class Swagger_TypeOfBoard_Filter {


    @ApiModelProperty(required = false)
    public String producer_name;

    @ApiModelProperty(required = false)
    public Integer count_from;

    @ApiModelProperty(required = false)
    public String processor_name;

    @ApiModelProperty(required = false)
    public Integer count_to;

    @ApiModelProperty(value = "Value of order", allowableValues = "group_name, id" , required = false)
    public String order;

    @ApiModelProperty(value = "Set type of Order -> ascending / descending, If you used order - its required!!!", allowableValues = "asc OR desc" , required = false)
    public String value;

}
