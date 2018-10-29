package utilities.gsm_services.things_mobile.helpers;

import java.util.ArrayList;
import java.util.List;

public class TMKey {

    public String key;
    public List<String> values = new ArrayList<>();

    public TMKey(String key, List<String> values){
        this.key = key;
        this.values = values;
    }

}
