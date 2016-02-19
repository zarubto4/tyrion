
package utilities.deadbolt.understand;

import be.objectify.deadbolt.core.models.Role;

public enum Roles implements Role
{
    superAdmin,
    admin,
    operator,
    user;

    @Override
    public String getName()
    {
        System.out.println("Roles.getName");
        return name();
    }
}
