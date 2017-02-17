package utilities.schedules_activities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.Application;
import play.libs.Json;
import utilities.request_counter.RequestCounter;

public class Request_Stats_Update implements Job {

    @Inject
    Application application;

    public Request_Stats_Update(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!stats_update_thread.isAlive()) stats_update_thread.start();
    }

    Thread stats_update_thread = new Thread() {

        @Override
        public void run() {

            try {
                logger.info("Request_Stats_Update:: log_upload_thread:: started");

                if (!RequestCounter.requests.isEmpty()) {

                    ObjectNode json = Json.newObject();

                    json.set("requests", Json.toJson(RequestCounter.requests.entrySet()));


                    //PrintWriter writer = new PrintWriter(new File(application.path() + "/logs/requests.log"));
                    //writer.print(json);
                    //writer.close();

                    logger.info("Request_Stats_Update:: log_upload_thread:: log successfully uploaded");
                } else {
                    logger.info("Request_Stats_Update:: log_upload_thread:: no requests");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Request_Stats_Update:: log_upload_thread:: error in thread");
            }

            logger.info("Request_Stats_Update:: log_upload_thread:: stopped");
        }
    };
}