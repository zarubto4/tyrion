package utilities.gsm_services.things_mobile.helpers;

import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Slouží k vytváření n klíčů jako dodatečný filtr parametr pro API thins_mobile.
 * Je nutné se přispůsobit jejich požadavkům podle application/x-www-form-urlencoded
 *
 */
public class TMKeyStoreSender extends _Swagger_Abstract_Default {


    private HashMap<String,  List<String>> hash = new HashMap<>();

    public TMKeyStoreSender(){}


    public void addKey(String key, String value) {
        if(!hash.containsKey(key)) {
            hash.put(key, new ArrayList<String>());
        }

        hash.get(key).add(value);
    }

    public void addKey(String key, List<String> values ) {
        if(!hash.containsKey(key)) {
            hash.put(key, new ArrayList<String>());
        }

        hash.get(key).addAll(values);
    }


    public HashMap<String,  List<String>> getHash(){
        return hash;
    }

    public TMKey[] getKeys(){

        TMKey[] keys = new  TMKey[hash.size()];

        int pointer = 0;
        for (String key : hash.keySet()) {
            keys[pointer] = new TMKey(key, hash.get(key));
            pointer++;
        }

        return keys;
    }

}
