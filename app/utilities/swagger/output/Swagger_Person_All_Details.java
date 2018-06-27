package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Person;
import models.Model_Role;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Result")
public class Swagger_Person_All_Details extends _Swagger_Abstract_Default {

    @ApiModelProperty(readOnly = true)
    public Model_Person person;

    @ApiModelProperty(readOnly = true)
    public String hmac;

    @ApiModelProperty(value = "List of roles, that", readOnly = true)
    public List<Model_Role> roles;

    @ApiModelProperty(readOnly = true, value = "List of all person permission (private and all collections from Person Roles (\"SecurityRole\") ")
    public List<String> permissions;

}