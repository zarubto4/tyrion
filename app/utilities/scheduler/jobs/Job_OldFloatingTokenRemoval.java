package utilities.scheduler.jobs;


import models.Model_FloatingPersonToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.loggy.Loggy;

import java.util.Date;
import java.util.List;

public class Job_OldFloatingTokenRemoval implements Job {

    public Job_OldFloatingTokenRemoval(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_OldFloatingTokenRemoval:: execute: Executing Job_OldFloatingTokenRemoval");

        if(!remove_floating_person_token_thread.isAlive()) remove_floating_person_token_thread.start();
    }

    private Thread remove_floating_person_token_thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: concurrent thread started on {}", new Date());

                while (true) {

                    List<Model_FloatingPersonToken> tokens = Model_FloatingPersonToken.find.where().gt("access_age", new Date()).setMaxRows(100).findList();
                    if (tokens.isEmpty()) {
                        logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: no tokens to remove");
                        break;
                    }

                    logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: removing old tokens (100 per cycle)");

                    tokens.forEach(Model_FloatingPersonToken::delete);
                }

            } catch (Exception e) {
                Loggy.internalServerError("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread:", e);
            }

            logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: thread stopped on {}", new Date());
        }
    };
}
