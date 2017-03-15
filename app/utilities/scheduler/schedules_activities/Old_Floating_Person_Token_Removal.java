package utilities.scheduler.schedules_activities;


import models.Model_FloatingPersonToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

public class Old_Floating_Person_Token_Removal implements Job {

    public Old_Floating_Person_Token_Removal(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!remove_floating_person_token_thread.isAlive()) remove_floating_person_token_thread.start();
    }

    Thread remove_floating_person_token_thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.info("Independent Thread in Old_Floating_Person_Token_Removal now working");

                Long before_72_hours = new Date().getTime() - (72 * 3600000);
                Date created = new Date(before_72_hours);

                while (true) {

                    List<Model_FloatingPersonToken> tokens_to_remove = Model_FloatingPersonToken.find.where().lt("created", created).setMaxRows(100).findList();
                    if (tokens_to_remove.isEmpty()) {
                        logger.info("Old_Floating_Person_Token_Removal has no tokens to remove");
                        break;
                    }

                    logger.info("CRON Task is removing old tokens (100 per cycle)");

                    for (Model_FloatingPersonToken token : tokens_to_remove) {
                        token.delete();
                    }
                }

            } catch (Exception e) {
                logger.error("Error in Thread - Old_Floating_Person_Token_Removal");
            }

            logger.info("Independent Thread in Old_Floating_Person_Token_Removal stopped!");
        }
    };
}
