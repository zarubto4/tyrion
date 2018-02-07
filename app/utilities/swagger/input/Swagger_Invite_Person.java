package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;


@ApiModel(description = "Json Model for sharing project with Persons, invite to permission group etc..",
          value = "Invite_Person")
public class Swagger_Invite_Person {


    @Constraints.Required
    @ApiModelProperty(required = true)
    public List<String> persons_mail;

 }
