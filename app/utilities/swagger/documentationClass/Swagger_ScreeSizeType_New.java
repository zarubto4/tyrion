package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Scree Size Type ",
          value = "ScreeSizeType_New")
public class Swagger_ScreeSizeType_New {


    @Constraints.Required
    @Constraints.MinLength(value = 3, message = "The name must have at least 3 characters. Must be unique if its not private!")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters. Must be unique if its not private!")
    @ApiModelProperty(required = true, value = "The name length must be between 3 and 60 characters. Must be unique if its not private!" )
    public String name;

    @Constraints.Required
    @ApiModelProperty(required = true )
    public boolean height_lock;

    @ApiModelProperty(required = true)
    @Constraints.Required
    public boolean width_lock;

    @ApiModelProperty(required = true)
    @Constraints.Required
    public boolean touch_screen;

    @ApiModelProperty(value = "When you want create private screen", required = false)
    public String project_id;


    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer landscape_height;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer landscape_width;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer landscape_square_height;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer landscape_square_width;

    @Constraints.Required @Constraints.Min(value = 1) @Constraints.Max(value = 10) @ApiModelProperty(required = true) public Integer landscape_max_screens;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer landscape_min_screens;

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer portrait_height;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer portrait_width;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer portrait_square_height;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer portrait_square_width;


    @Constraints.Required @Constraints.Min(value = 1) @Constraints.Max(value = 10)  @ApiModelProperty(required = true) public Integer portrait_max_screens;
    @Constraints.Required @Constraints.Min(value = 0) @ApiModelProperty(required = true) public Integer portrait_min_screens;

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------


}
