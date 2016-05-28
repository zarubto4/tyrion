package utilities.swagger.swagger_diff_tools.servise_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import javax.validation.Valid;
import java.util.*;

public class Swagger_Api {

    public String swagger;
    public String host;

    @Valid public Info info;
    @Valid public List<Tag> tags;

    @JsonIgnore public Map<String, JsonNode> models = new HashMap<>();


//*------ Methods --------------------------

    public boolean contains_tag(String name){
        for(Tag tag : tags) if(name.equals(tag.name)) return true;
        return false;
    }


//*-------- Class ---------------------------

    public static class Info {
        public Info(){}
        public String version;
    }


    public static class Tag {
        public Tag(){}

        public String name;
    }

    public class Model_Object{
        public String name;
        public JsonNode body;
        public Model_Object(String name, JsonNode body){
            this.name = name;
            this.body = body;
        }
    }

    public void arrange_models(JsonNode json){
        Iterator<Map.Entry<String, JsonNode>> iterator_old = json.fields();

        while (iterator_old.hasNext()){
            Map.Entry<String, JsonNode> s  = iterator_old.next();
            models.put( s.getKey(), s.getValue() );
        }
    }



}
