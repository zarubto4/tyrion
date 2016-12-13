package utilities.schedules_activities;


import models.person.Person;
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

                List<Person> persons = Person.find.where().lt("created", created).setMaxRows(100).findList(); // TODO podle čeho zjistím, kdy se registroval
                if (persons.isEmpty()) {
                    logger.info("Unauthenticated_Person_Removal has no persons to remove");
                    break;
                }

                logger.info("CRON Task is removing unauthenticated persons (100 per cycle)");

                for (Person person : persons){
                    person.delete();
                }
            }

            logger.info("Independent Thread in Unauthenticated_Person_Removal stopped!");
        }
    };
}
