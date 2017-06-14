package utilities.scheduler.jobs;


import models.Model_Person;
import models.Model_ValidationToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_UnauthenticatedPersonRemoval implements Job {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_UnauthenticatedPersonRemoval.class);

//**********************************************************************************************************************

    public Job_UnauthenticatedPersonRemoval(){}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_UnauthenticatedPersonRemoval");

        if(!remove_person_thread.isAlive()) remove_person_thread.start();
    }

    private Thread remove_person_thread = new Thread() {

        @Override
        public void run() {

            try {

                terminal_logger.debug("remove_person_thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(30)); // before one month

                while (true) {

                    List<Model_ValidationToken> tokens = Model_ValidationToken.find.where().lt("created", created).setMaxRows(100).findList();
                    if (tokens.isEmpty()) {
                        terminal_logger.debug("Job_UnauthenticatedPersonRemoval:: remove_person_thread: no persons to remove");
                        break;
                    }

                    terminal_logger.debug("remove_person_thread: removing unauthenticated persons (100 per cycle)");

                    for (Model_ValidationToken token : tokens) {
                        Model_Person person = Model_Person.find.where().eq("mail", token.personEmail).findUnique();
                        if (person != null && !person.mailValidated) person.delete();
                        token.delete();
                    }
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("remove_person_thread", e);
            }

            terminal_logger.debug("Job_UnauthenticatedPersonRemoval:: remove_person_thread: thread stopped on {}", new Date());
        }
    };
}
