package utilities.request_counter;

import com.google.inject.Inject;
import play.Application;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RequestCounter extends Controller{

    @Inject
    Application application;

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

            return GlobalResult.result_ok(Json.parse(Files.readAllBytes(Paths.get(application.path() + "/logs/requests.log"))));

        } catch (Exception e) {

            return Loggy.result_internalServerError(e, request());
        }
    }
}
