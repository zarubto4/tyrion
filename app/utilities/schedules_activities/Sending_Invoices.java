package utilities.schedules_activities;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.goPay.GoPay_Controller;

import java.util.Date;

public class Sending_Invoices implements Job {


    public Sending_Invoices(){ /** do nothing */ }
    static play.Logger.ALogger logger = play.Logger.of("CRON-Sending_Invoices");


    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.warn("Starting with CRON procedure");
        logger.warn("Time" + new Date().toString() );

        logger.warn("GoPay_Controller - do_on_Demand_payment");
        GoPay_Controller.do_on_Demand_payment();

    }

}
