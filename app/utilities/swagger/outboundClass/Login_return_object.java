package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;

import java.util.List;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Result")
public class Login_return_object {

    @ApiModelProperty(readOnly = true)
    public Person person;

    @ApiModelProperty(value = "used this token in HTML head for verifying the identities", readOnly = true)
    public String authToken;

    @ApiModelProperty(value = "", readOnly = true)
    public List<SecurityRole> roles;

    @ApiModelProperty(readOnly = true)
    public List<PersonPermission> permissions;

}