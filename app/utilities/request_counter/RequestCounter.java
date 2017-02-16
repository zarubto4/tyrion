package utilities.request_counter;

import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.util.HashMap;
import java.util.Map;

public class RequestCounter extends Controller{

    public static Map<String, Long> requests = new HashMap<>(); // počítadlo requestů

    public static void count(String request){

        // Pokud mapa už request obsahuje, pouze zvýším value++
        if (requests.containsKey(request)){

            Long value = requests.get(request);

            value++;

            requests.replace(request, value);
        }
        else {
            requests.put(request, 1L);
        }
    }

    public Result get_request_stats() {

        try {

            return GlobalResult.result_ok();

        } catch (Exception e) {

            return Loggy.result_internalServerError(e, request());
        }
    }
}
