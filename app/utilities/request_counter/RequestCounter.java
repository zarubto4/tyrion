package utilities.request_counter;

import models.Model_RequestLog;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for endpoint usage statistics.
 */
@Security.Authenticated(Secured_API.class)
public class RequestCounter extends Controller{

    /**
     * Temporary memory of requests, every hour are all requests saved to DB.
     */
    public static Map<String, Long> requests = new HashMap<>();

    /**
     * Adds request to static HashMap or updates the record.
     * @param request String name of action method that is being called
     */
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

    /**
     * Serves to show data in Tyrion administration.
     * @return Result list of models RequestLog
     */

    public Result get_request_stats() {

        try {

            return GlobalResult.result_ok(Json.toJson(Model_RequestLog.find.order().desc("call_count").findList()));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    /**
     * Resets all stats about requests, deletes all RequestLog objects from DB.
     * @return Result ok
     */
    public Result reset_request_stats() {

        try {

            for (Model_RequestLog log : Model_RequestLog.find.all()){

                log.delete();
            }

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
