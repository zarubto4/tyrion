package utilities.permission;

import utilities.enums.EntityType;

import java.util.List;

public interface Permissible {

    EntityType getEntityType();

    List<Action> getSupportedActions();
}
