package utilities.scheduler.jobs;


import models.Model_Blob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;
import utilities.scheduler.Scheduled;


import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

@Restrict(ServerMode.DEVELOPER)
//@Scheduled("0 0 0 * * ?")
public class Job_LogAzureUpload implements Job {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_LogAzureUpload.class);

//**********************************************************************************************************************

    public Job_LogAzureUpload() {}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_LogAzureUpload:: execute: Executing Job_LogAzureUpload");

        if (!log_upload_thread.isAlive()) log_upload_thread.start();
    }

    private Thread log_upload_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("Job_LogAzureUpload:: log_upload_thread: concurrent thread started on {}", new Date());

                File file = new File(System.getProperty("user.dir") + "/logs/all.log");

                PrintWriter writer = new PrintWriter(new File(System.getProperty("user.dir") + "/logs/all.log"));
                writer.close();

                String file_name = new Date().toString();

                Model_Blob.uploadAzure_File(file, file_name, "logs/" + file_name);

                logger.debug("Job_LogAzureUpload:: log_upload_thread: log successfully uploaded");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("Job_LogAzureUpload:: log_upload_thread: thread stopped on {}", new Date());
        }
    };
}