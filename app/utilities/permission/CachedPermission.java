package utilities.permission;

import java.util.UUID;

public class CachedPermission {

    /**
     * The identifier of the object that is being accessed.
     */
    public UUID modelId;

    /**
     * The permission that is being checked.
     */
    public Permission permission;

    /**
     * True if the permission was granted before.
     */
    public boolean permitted;
}
