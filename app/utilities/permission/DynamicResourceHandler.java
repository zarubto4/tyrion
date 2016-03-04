package utilities.permission;

public interface DynamicResourceHandler {
    boolean check_dynamic(String name) throws PermissionException;
}