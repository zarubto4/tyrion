package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;


@ApiModel(description = "Json Model for new Post",
        value = "Post_Answer")
public class Swagger_Post_Answer {

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The text of post must have at least 24 characters")
    @ApiModelProperty(required = true)
    public String text_of_post;


    @ApiModelProperty(required = false)
    public List<String> hash_tags;

}
