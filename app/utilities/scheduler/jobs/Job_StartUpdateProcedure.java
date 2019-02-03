package utilities.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.logger.Logger;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Start or Execute with Update Procedure on required time.
 */
public class Job_StartUpdateProcedure implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_StartUpdateProcedure.class);

//**********************************************************************************************************************

    public Job_StartUpdateProcedure(ApplicationLifecycle appLifecycle) {
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

    private UUID procedure_id;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_StartUpdateProcedure");

        procedure_id = UUID.fromString(context.getMergedJobDataMap().getString("procedure_id"));

        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("thread:: concurrent thread started on {}", new Date());

                if (procedure_id == null) throw new NullPointerException("thread:: Job was instantiated without record_id in the JobExecutionContext or the record_id is null for some reason.");

                logger.trace("thread:: uploading the record");

                // TODO update procedure.execute_update_procedure();

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("upload_blocko_thread: thread stopped on {}", new Date());
        }
    };
}