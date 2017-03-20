package utilities.scheduler.jobs;

import models.Model_FileRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.loggy.Loggy;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

public class Job_LogAzureUpload implements Job {

    public Job_LogAzureUpload(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_LogAzureUpload:: execute: Executing Job_LogAzureUpload");

        if(!log_upload_thread.isAlive()) log_upload_thread.start();
    }

    private Thread log_upload_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("Job_LogAzureUpload:: log_upload_thread: concurrent thread started on {}", new Date());

                File file = new File(System.getProperty("user.dir") + "/app/logs/all.log");

                PrintWriter writer = new PrintWriter(new File(System.getProperty("user.dir") + "/app/logs/all.log"));
                writer.close();

                String file_name = new Date().toString();

                Model_FileRecord.uploadAzure_File(file, file_name, "logs/" + file_name);

                logger.debug("Job_LogAzureUpload:: log_upload_thread: log successfully uploaded");

            } catch (Exception e) {
                Loggy.internalServerError("Job_LogAzureUpload:: log_upload_thread:", e);
            }

            logger.debug("Job_LogAzureUpload:: log_upload_thread: thread stopped on {}", new Date());
        }
    };
}