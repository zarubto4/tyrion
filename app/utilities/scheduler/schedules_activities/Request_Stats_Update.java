package utilities.scheduler.schedules_activities;

import models.loggy.Model_RequestLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.request_counter.RequestCounter;

import java.util.Map.Entry;

public class Request_Stats_Update implements Job {

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


                    for (Entry<String,Long> entry : RequestCounter.requests.entrySet()) {

                        Model_RequestLog log = Model_RequestLog.find.where().eq("request",entry.getKey()).findUnique();
                        if (log == null){

                            log = new Model_RequestLog();
                            log.request = entry.getKey();
                            log.call_count = entry.getValue();

                            log.save();

                        }else {

                            log.call_count += entry.getValue();

                            log.update();
                        }
                    }

                    RequestCounter.requests.clear();

                    logger.info("Request_Stats_Update:: log_upload_thread:: log successfully uploaded");
                } else {
                    logger.info("Request_Stats_Update:: log_upload_thread:: no requests");
                }
            } catch (Exception e) {
                logger.error("Request_Stats_Update:: log_upload_thread:: error in thread");
            }

            logger.info("Request_Stats_Update:: log_upload_thread:: stopped");
        }
    };
}