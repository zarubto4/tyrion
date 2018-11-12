package utilities.server_measurement;

import com.typesafe.config.Config;
import controllers.Controller_Blocko;
import controllers._BaseController;
import controllers._BaseFormFactory;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.Result;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;

import java.util.HashMap;
import java.util.Map;


public class Controller_RequestCounter extends _BaseController {


// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

// CONTROLLER CONFIGURATION ############################################################################################

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_RequestCounter(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerService scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
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
