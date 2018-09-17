package utilities.model;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSerializable {

    JsonNode json();
}
