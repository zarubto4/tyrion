package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for getting Post by Filter",
        value = "Post_Filter")
public class Swagger_Post_Filter {

    @ApiModelProperty(value = "List of hashTags", required = false)
    public List<String> hash_tags;

    @ApiModelProperty(value = "List of confirms", required = false)
    public List<String> confirms;

    @ApiModelProperty(value = "List of TypeOfPost.id", required = false)
    public List<String> types;


    @ApiModelProperty(required = false)
    public Date date_from;

    @ApiModelProperty(required = false)
    public Date date_to;

    @ApiModelProperty(value = "Post author nick_name", required = false)
    public String nick_name;

    @Constraints.Min(value = 1)
    @ApiModelProperty(required = false, value = "Minimum >= 1")
    public Integer count_from;

    @ApiModelProperty(required = false)
    public Integer count_to;

    @ApiModelProperty(value = "Value of order", allowableValues = "group_name, id, date_of_create" , required = false)
    public String order;

    @ApiModelProperty(value = "Set type of Order -> ascending / descending, If you used order - its required!!!", allowableValues = "asc OR desc" , required = false)
    public String value;

}
