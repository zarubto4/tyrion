package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for getting Type_Of_Block Filter List",
        value = "Type_Of_Block_Filter")
public class Swagger_TypeOfBlock_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Type_Of_Blocks of given project")
    public String project_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Use 'true' for private Type_Of_Block or 'false' for non-private")
    public Boolean public_programs;
}
