package utilities.scheduler.jobs;

import models.Model_RequestLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.loggy.Loggy;
import utilities.request_counter.RequestCounter;

import java.util.Date;
import java.util.Map.Entry;

public class Job_RequestStatsUpdate implements Job {

    public Job_RequestStatsUpdate(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_RequestStatsUpdate:: execute: Executing Job_RequestStatsUpdate");

        if(!stats_update_thread.isAlive()) stats_update_thread.start();
    }

    private Thread stats_update_thread = new Thread() {

        @Override
        public void run() {

            try {
                logger.trace("Job_RequestStatsUpdate:: stats_update_thread: concurrent thread started on {}", new Date());

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

                    logger.trace("Job_RequestStatsUpdate:: stats_update_thread: logs successfully updated");
                } else {
                    logger.trace("Job_RequestStatsUpdate:: stats_update_thread: no requests");
                }
            } catch (Exception e) {
                Loggy.internalServerError("Job_RequestStatsUpdate:: stats_update_thread:", e);
            }

            logger.trace("Job_RequestStatsUpdate:: stats_update_thread: thread stopped on {}", new Date());
        }
    };
}