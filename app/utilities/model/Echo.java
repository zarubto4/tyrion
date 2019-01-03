package utilities.model;

import common.Identifiable;
import models.Model_Project;

/**
 * For objects that have relations to project.
 */
public interface Echo extends Identifiable, Publishable {

    void save();

    void update();

    boolean delete();

    /**
     * Gets the parent object from the project hierarchy.
     * @return _BaseModel
     */
    Echo getParent();

    Model_Project getProject();
}
