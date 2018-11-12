package utilities.network;

import utilities.enums.EntityType;

import java.util.UUID;

public interface Networkable {

    UUID getId();

    EntityType getEntityType();
}
