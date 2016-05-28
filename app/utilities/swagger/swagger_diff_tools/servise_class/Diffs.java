package utilities.swagger.swagger_diff_tools.servise_class;


import com.fasterxml.jackson.databind.JsonNode;


public class Diffs{

    public String name;
    public JsonNode old_json;
    public JsonNode new_json;


    public Diffs(String name, JsonNode old_json, JsonNode new_json){
        this.name = name;
        this.new_json = new_json;
        this.old_json = old_json;
    }
}
