package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Model_Person;
import models.person.Model_SecurityRole;

import java.util.List;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Result")
public class Swagger_Person_All_Details {

    @ApiModelProperty(readOnly = true)
    public Model_Person person;


    @ApiModelProperty(value = "List of roles, that", readOnly = true)
    public List<Model_SecurityRole> roles;

    @ApiModelProperty(readOnly = true, value = "List of all person permission (private and all collections from Person Roles (\"SecurityRole\") ")
    public List<String> permissions;

}