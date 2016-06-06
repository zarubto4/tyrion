package utilities.swagger.swagger_diff_tools.servise_class;


public class Diffs{

    public String name;
    public String old_json;
    public String new_json;


    public Diffs(String name, String old_json, String new_json){
        this.name = name;
        this.new_json = new_json;
        this.old_json = old_json;
    }
}
