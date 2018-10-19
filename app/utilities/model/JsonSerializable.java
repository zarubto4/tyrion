package utilities.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface JsonSerializable {

    ObjectNode json();
}
