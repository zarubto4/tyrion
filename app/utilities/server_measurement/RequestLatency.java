package utilities.server_measurement;

import controllers.Controller_Blocko;
import utilities.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for endpoint usage statistics.
 */
public class RequestLatency {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(RequestLatency.class);

// STATIC CONTENTS #####################################################################################################

    /**
     * Temporary memory of requests, every hour are all Arithmetic mean saved to DB.
     */
    public static Map<String, List<Long>> requests = new HashMap<>();

// METHODS ##############################################################################################################

    /**
     * Adds request to static HashMap or updates the record.
     * @param request String name of action method that is being called
     */
    public static void count_end(String request, String path, Long time){

        logger.trace("count_end:: Request: " + request + ":: path: \"" + path + "\" time: " + time);

        if (!requests.containsKey(request)){
            requests.put(request, new ArrayList<Long>());
        }

        requests.get(request).add(time);
    }
}
