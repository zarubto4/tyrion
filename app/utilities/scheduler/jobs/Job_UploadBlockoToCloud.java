package utilities.scheduler.jobs;

import models.Model_HomerInstanceRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;

import java.util.Date;

/**
 * Uploads blocko program to homer.
 */
public class Job_UploadBlockoToCloud implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_UploadBlockoToCloud.class);

//**********************************************************************************************************************

    public Job_UploadBlockoToCloud(){}

    private String record_id;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_UploadBlockoToCloud");

        record_id = context.getMergedJobDataMap().getString("record_id");

        if(!upload_blocko_thread.isAlive()) upload_blocko_thread.start();
    }

    private Thread upload_blocko_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.trace("upload_blocko_thread: concurrent thread started on {}", new Date());

                if (record_id == null) throw new NullPointerException("Job was instantiated without record_id in the JobExecutionContext or the record_id is null for some reason.");

                Model_HomerInstanceRecord record = Model_HomerInstanceRecord.get_byId(record_id);
                if (record == null) throw new NullPointerException("Cannot find the Instance Record in the DB.");

                terminal_logger.trace("upload_blocko_thread: uploading the record");

                record.set_record_into_cloud();

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("upload_blocko_thread: thread stopped on {}", new Date());
        }
    };
}