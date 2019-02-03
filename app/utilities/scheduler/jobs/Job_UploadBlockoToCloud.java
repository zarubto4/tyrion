package utilities.scheduler.jobs;

import com.google.inject.Inject;
import models.Model_InstanceSnapshot;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.instance.InstanceService;
import utilities.logger.Logger;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Uploads blocko program to homer.
 */
public class Job_UploadBlockoToCloud implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_UploadBlockoToCloud.class);

//**********************************************************************************************************************

    private final InstanceService instanceService;

    @Inject
    public Job_UploadBlockoToCloud(InstanceService instanceService, ApplicationLifecycle appLifecycle) {
        this.instanceService = instanceService;
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

    private UUID record_id;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_UploadBlockoToCloud");

        record_id = UUID.fromString(context.getMergedJobDataMap().getString("record_id"));

        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("thread: concurrent thread started on {}", new Date());

                if (record_id == null) throw new NullPointerException("Job was instantiated without record_id in the JobExecutionContext or the record_id is null for some reason.");

                Model_InstanceSnapshot record = Model_InstanceSnapshot.find.byId(record_id);

                logger.trace("thread: uploading the record");

                instanceService.deploy(record);

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("thread: thread stopped on {}", new Date());
        }
    };
}