package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;


@ApiModel(description = "Json Model for sharing project with Persons",
          value = "ShareProject_Person")
public class Swagger_ShareProject_Person {


    @Constraints.Required
    @ApiModelProperty(required = true)
    public List<String> persons_id;


 }
