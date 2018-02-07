package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for adding Tags",
        value = "Tags")
public class Swagger_Tags {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Id of object that should be tagged. (e.g. Project, Block, Widget etc.)")
    public String object_id;

    @Constraints.Required
    public List<String> tags = new ArrayList<>();
}
