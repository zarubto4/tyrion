package utilities.scheduler.jobs;

import models.Model_AuthorizationToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.util.Date;
import java.util.List;

/**
 * Class removes old authTokens, that cannot be used anymore. (Expiration time)
 */
@Scheduled
public class Job_OldAuthTokenRemoval implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_OldAuthTokenRemoval.class);

//**********************************************************************************************************************

    public Job_OldAuthTokenRemoval() {}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_OldFloatingTokenRemoval");

        if (!remove_token_thread.isAlive()) remove_token_thread.start();
    }

    /**
     * Thread finds tokens whose access_age is greater than now.
     */
    private Thread remove_token_thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("remove_token_thread: concurrent thread started on {}", new Date());

                while (true) {

                    List<Model_AuthorizationToken> tokens = Model_AuthorizationToken.find.query()
                            .where()
                            .isNotNull("access_age")
                            .lt("access_age", new Date())
                            .order().asc("access_age")
                            .setMaxRows(100)
                            .findList();

                    if (tokens.isEmpty()) {
                        logger.debug("remove_token_thread: no tokens to remove");
                        break;
                    }

                    logger.debug("remove_token_thread: removing old tokens (100 per cycle)");

                    tokens.forEach(Model_AuthorizationToken::delete);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("remove_token_thread: thread stopped on {}", new Date());
        }
    };
}
