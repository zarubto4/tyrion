package utilities.scheduler.jobs;


import models.Model_Blob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;


import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Restrict(ServerMode.DEVELOPER)
//@Scheduled("0 0 0 * * ?")
public class Job_LogAzureUpload implements Job {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_LogAzureUpload.class);

//**********************************************************************************************************************

    public Job_LogAzureUpload(ApplicationLifecycle appLifecycle) {

        appLifecycle.addStopHook(() -> {
            try {
                logger.warn("Interupt Thread ", this.getClass().getSimpleName());
                this.thread.interrupt();
            } catch (Exception e){
                //
            };
            return CompletableFuture.completedFuture(null);
        });
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_LogAzureUpload:: execute: Executing Job_LogAzureUpload");

        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("Job_LogAzureUpload:: thread: concurrent thread started on {}", new Date());

                File file = new File(System.getProperty("user.dir") + "/logs/all.log");

                PrintWriter writer = new PrintWriter(new File(System.getProperty("user.dir") + "/logs/all.log"));
                writer.close();

                String file_name = new Date().toString();

                Model_Blob.upload(file, "application/octet-stream", file_name, "logs");

                logger.debug("Job_LogAzureUpload:: thread: log successfully uploaded");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("Job_LogAzureUpload:: thread: thread stopped on {}", new Date());
        }
    };
}