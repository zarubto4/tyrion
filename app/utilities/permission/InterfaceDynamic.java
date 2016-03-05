package utilities.permission;

public abstract class InterfaceDynamic implements DynamicResourceHandler {

    public static boolean checkPermission()
    {return false;}

    public static boolean isAllowed(final String name)
    { return false;}
}