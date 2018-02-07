package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new General Project",
        value = "Project_New")
public class Swagger_Project_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Id of Product (Tariff) where the project will be registered")
    public String product_id;
}
