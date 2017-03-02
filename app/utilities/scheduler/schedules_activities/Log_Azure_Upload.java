package utilities.scheduler.schedules_activities;

import models.compiler.Model_FileRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

public class Log_Azure_Upload implements Job {

    public Log_Azure_Upload(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!log_upload_thread.isAlive()) log_upload_thread.start();
    }

    Thread log_upload_thread = new Thread() {

        @Override
        public void run() {

            try {
                logger.info("Independent Thread in Log_Azure_Upload now working");

                File file = new File(System.getProperty("user.dir") + "/app/logs/all.log");

                PrintWriter writer = new PrintWriter(new File(System.getProperty("user.dir") + "/app/logs/all.log"));
                writer.close();

                String file_name = new Date().toString();

                Model_FileRecord.uploadAzure_File(file, file_name, "logs/" + file_name);

                logger.info("Log_Azure_Upload:: log_upload_thread:: log successfully uploaded");

            } catch (Exception e) {
                logger.error("Log_Azure_Upload:: log_upload_thread:: error in thread");
            }

            logger.info("Independent Thread in Log_Azure_Upload stopped!");
        }
    };
}