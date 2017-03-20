package utilities;

import play.mvc.Controller;

import java.util.*;

/**
 * Pomocná třída, realizující jednoúčelové metody. Zde si odkládám cokoliv, co by bylo v kódu zbytečně duplicitní.
 * Například při create a edit vždy ukládám něco. Vždy parsuji datum.. atd.
 */

public class UtilTools extends Controller {

    public static Map<String, String> getMap_From_query(Set<Map.Entry<String, String[]>> url){
        Map<String, String> map = new HashMap<>();

        for (Map.Entry<String,String[]> entry : url) {

            final String key = entry.getKey();
            final String value = Arrays.toString(entry.getValue());
            map.put(key, value);
        }
        return  map;
    }





}
