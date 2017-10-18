package utilities.scheduler.jobs;

import models.Model_RequestLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;
import utilities.request_counter.RequestCounter;
import web_socket.message_objects.common.WS_Send_message;

import java.util.Date;
import java.util.Map.Entry;

/**
 * Updates request records in DB
 */
public class Job_RequestStatsUpdate implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_RequestStatsUpdate.class);

//**********************************************************************************************************************

    public Job_RequestStatsUpdate(){}
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_RequestStatsUpdate");

        if(!stats_update_thread.isAlive()) stats_update_thread.start();
    }

    /**
     * Thread empties request HashMap in RequestCounter.class and uploads it to DB
     */
    private Thread stats_update_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.trace("stats_update_thread: concurrent thread started on {}", new Date());

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

                    terminal_logger.trace("stats_update_thread: logs successfully updated");
                } else {
                    terminal_logger.trace("stats_update_thread: no requests");
                }
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("stats_update_thread: thread stopped on {}", new Date());
        }
    };
}