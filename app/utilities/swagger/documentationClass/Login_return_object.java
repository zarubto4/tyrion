package utilities.swagger.documentationClass;

import models.persons.Person;
import models.persons.PersonPermission;
import models.persons.SecurityRole;

import java.util.List;

public class Login_return_object {
    public Person person;
    public String authToken;
    public List<SecurityRole> roles;
    public List<PersonPermission> permissions;
}