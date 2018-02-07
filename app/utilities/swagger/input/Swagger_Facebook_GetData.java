package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model (private) for Facebook login services",
        value = "Facebook_GetDatar")
public class Swagger_Facebook_GetData {

    @Constraints.Required
    public String id;

    public Integer name;

    public String first_name;

    public Integer last_name;

    public String email;

}