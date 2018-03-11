package utilities.server_measurement;

import com.google.inject.Inject;
import controllers.Controller_Blocko;
import controllers._BaseController;
import controllers._BaseFormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerController;

import java.util.HashMap;
import java.util.Map;


public class Controller_RequestCounter extends _BaseController {


// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private SchedulerController scheduler;

    @Inject
    public Controller_RequestCounter(_BaseFormFactory formFactory, SchedulerController scheduler) {
        this.baseFormFactory = formFactory;
        this.scheduler = scheduler;
    }

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

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    /**
     * Resets all stats about requests, deletes all RequestLog objects from DB.
     * @return Result ok
     */
    public Result reset_request_stats() {
        try {

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

}
