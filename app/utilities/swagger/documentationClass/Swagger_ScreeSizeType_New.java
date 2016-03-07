package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Scree Size Type ",
          value = "ScreeSizeType_New")
public class Swagger_ScreeSizeType_New {


    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters. Must be unique if its not private!")
    @ApiModelProperty(required = true)
    public String name;

    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true)
    public Integer height;

    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true)
    public Integer width;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public boolean height_lock;

    @ApiModelProperty(required = true)
    @Constraints.Required
    public boolean width_lock;

    @ApiModelProperty(required = true)
    @Constraints.Required
    public boolean touch_screen;

    @ApiModelProperty(value = "When you want create private screen", required = false)
    public String project_id;

}
