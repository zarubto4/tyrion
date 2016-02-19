package utilities.deadbolt.actions;


import utilities.deadbolt.understand.Roles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface RoleGroup
{
    /**
     * The roles with access to the target.
     *
     * @return the role names
     */
    Roles[] value() default {};

    Roles[] not() default {};
}
