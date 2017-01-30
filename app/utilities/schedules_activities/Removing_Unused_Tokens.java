package utilities.schedules_activities;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Removing_Unused_Tokens implements Job {

    public Removing_Unused_Tokens(){ /** do nothing */ }


    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.err.println("Vypadá to, že CRON procedury fungují!");

    }


}
