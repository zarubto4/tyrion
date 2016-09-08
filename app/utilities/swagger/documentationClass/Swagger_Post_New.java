package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;

@ApiModel(description = "Json Model for new Post",
          value = "Post_New")
public class Swagger_Post_New {

      @Constraints.Required
      @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
      @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
      @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
      public String name;

      @Constraints.Required
      @Constraints.MinLength(value = 24, message = "The text must have at least 24 characters")
      @ApiModelProperty(required = true, value =  "The text must have at least 24 characters")
      public String text_of_post;

      @Constraints.Required
      @ApiModelProperty(value = "Required valid type_of_post_id", required = true)
      public String type_of_post_id;

      @ApiModelProperty(required = false)
      public List<String> hash_tags;

}
