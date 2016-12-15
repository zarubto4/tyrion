package utilities.schedules_activities;


import models.person.Person;
import models.person.ValidationToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

public class Unauthenticated_Person_Removal implements Job {

    public Unauthenticated_Person_Removal(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!remove_person_thread.isAlive()) remove_person_thread.start();
    }

    static Thread remove_person_thread = new Thread() {

        @Override
        public void run() {

            logger.info("Independent Thread in Unauthenticated_Person_Removal now working");

            Long month = new Long("2592000000");
            Long before_month = new Date().getTime() - month;
            Date created = new Date(before_month);

            while (true){

                List<ValidationToken> tokens = ValidationToken.find.where().lt("created", created).setMaxRows(100).findList();
                if (tokens.isEmpty()) {
                    logger.info("Unauthenticated_Person_Removal has no persons to remove");
                    break;
                }

                logger.info("CRON Task is removing unauthenticated persons (100 per cycle)");

                for (ValidationToken token : tokens){
                    Person person = Person.find.where().eq("mail", token.personEmail).findUnique();
                    if (person != null && !person.mailValidated) person.delete();
                    token.delete();
                }
            }

            logger.info("Independent Thread in Unauthenticated_Person_Removal stopped!");
        }
    };
}
