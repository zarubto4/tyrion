package utilities.deadbolt.actions;


import play.mvc.With;

import java.lang.annotation.*;


@With(UpdateContextAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface UpdateContext
{
    String value();
}
