package utilities.deadbolt.security;


import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.libs.F;
import play.mvc.Http;


public abstract class InterfaceHandler implements DynamicResourceHandler {

    public F.Promise<Boolean> checkPermission(final String permissionValue, final DeadboltHandler deadboltHandler, final Http.Context ctx)
    {return F.Promise.pure(false);}

    public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context ctx)
    { return F.Promise.pure(false);}
}
