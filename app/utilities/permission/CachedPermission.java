package utilities.permission;

import utilities.enums.EntityType;

import java.util.UUID;

public class CachedPermission {

    public CachedPermission(UUID modelId, EntityType entityType, Action action, boolean permitted) {
        this.modelId = modelId;
        this.entityType = entityType;
        this.action = action;
        this.permitted = permitted;
    }

    /**
     * The identifier of the object that is being accessed.
     */
    public UUID modelId;

    /**
     * The type of the object that is being accessed.
     */
    public EntityType entityType;

    /**
     * The permission that is being checked.
     */
    public Action action;

    /**
     * True if the permission was granted before.
     */
    public boolean permitted;
}
