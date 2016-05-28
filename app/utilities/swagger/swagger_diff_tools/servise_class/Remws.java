package utilities.swagger.swagger_diff_tools.servise_class;

import com.fasterxml.jackson.databind.JsonNode;

public class Remws {

    public String name;
    public JsonNode new_json;

    public Remws(String name, JsonNode new_json){
        this.name = name;
        this.new_json = new_json;

    }
}
