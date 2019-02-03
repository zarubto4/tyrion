package utilities.scheduler.jobs;

import models.Model_Person;
import models.Model_ValidationToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Scheduled("0 0 4 * * ?")
public class Job_UnauthenticatedPersonRemoval implements Job {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_UnauthenticatedPersonRemoval.class);

//**********************************************************************************************************************

    public Job_UnauthenticatedPersonRemoval() {}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_UnauthenticatedPersonRemoval");

        if (!remove_person_thread.isAlive()) remove_person_thread.start();
    }

    private Thread remove_person_thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("remove_person_thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(30)); // before one month

                while (true) {

                    List<Model_ValidationToken> tokens = Model_ValidationToken.find.query().where().lt("created", created).setMaxRows(100).findList();
                    if (tokens.isEmpty()) {
                        logger.debug("Job_UnauthenticatedPersonRemoval:: remove_person_thread: no persons to remove");
                        break;
                    }

                    logger.debug("remove_person_thread: removing unauthenticated persons (100 per cycle)");

                    for (Model_ValidationToken token : tokens) {
                        Model_Person person = Model_Person.find.query().where().eq("email", token.email).findOne();
                        if (person != null && !person.validated) person.delete();
                        token.delete();
                    }
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("Job_UnauthenticatedPersonRemoval:: remove_person_thread: thread stopped on {}", new Date());
        }
    };
}
