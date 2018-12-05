package utilities.model;

import java.util.UUID;

/**
 * For objects that have relations to project.
 */
public interface Echo {

    UUID getId();

    /**
     * Get ID of parent project.
     * @return UUID id of project
     */
    UUID getProjectId();

    /**
     * Gets the parent object from the project hierarchy.
     * @return _BaseModel
     */
    Echo getParent();

    /**
     * For checking - object can be public, thus project can be null.
     * @return boolean true if object has project
     */
    boolean hasProject();
}
