
package utilities.deadbolt.understand;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.libs.F;
import play.mvc.Http;

public class AlternativePermission implements DynamicResourceHandler
{
    public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context) {
        // look something up in an LDAP directory, etc, and the answer isn't good for the user
        return F.Promise.pure(false);
    }

    public F.Promise<Boolean> checkPermission(final String permissionValue, final DeadboltHandler deadboltHandler, final Http.Context ctx) {
        // Computer says no
        return F.Promise.pure(false);
    }
}
