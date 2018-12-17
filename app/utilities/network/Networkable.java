package utilities.network;

import utilities.enums.EntityType;
import utilities.swagger.output.Swagger_Short_Reference;

import java.util.List;
import java.util.UUID;

public interface Networkable {

    UUID getId();

    EntityType getEntityType();

}