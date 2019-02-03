package utilities.scheduler.jobs;

import models.Model_AuthorizationToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class removes old authTokens, that cannot be used anymore. (Expiration time)
 */
@Scheduled
public class Job_OldAuthTokenRemoval implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_OldAuthTokenRemoval.class);

//**********************************************************************************************************************

    public Job_OldAuthTokenRemoval(ApplicationLifecycle appLifecycle) {

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

        logger.info("execute: Executing Job_OldFloatingTokenRemoval");

        if (!thread.isAlive()) thread.start();
    }

    /**
     * Thread finds tokens whose access_age is greater than now.
     */
    private Thread thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("thread: concurrent thread started on {}", new Date());

                while (true) {

                    List<Model_AuthorizationToken> tokens = Model_AuthorizationToken.find.query()
                            .where()
                            .isNotNull("access_age")
                            .lt("access_age", new Date())
                            .order().asc("access_age")
                            .setMaxRows(100)
                            .findList();

                    if (tokens.isEmpty()) {
                        logger.debug("thread: no tokens to remove");
                        break;
                    }

                    logger.debug("thread: removing old tokens (100 per cycle)");

                    tokens.forEach(Model_AuthorizationToken::delete);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("thread: thread stopped on {}", new Date());
        }
    };
}
