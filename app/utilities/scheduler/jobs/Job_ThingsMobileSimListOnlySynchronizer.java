package utilities.scheduler.jobs;

import models.Model_GSM;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_List;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_List_list;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.util.UUID;
//0/15 0 0 ? * * *
@Scheduled("0 */2 * ? * *")
public class Job_ThingsMobileSimListOnlySynchronizer implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_ThingsMobileSimListOnlySynchronizer.class);

//**********************************************************************************************************************

    public Job_ThingsMobileSimListOnlySynchronizer() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute:: Executing Job_ThingsMobileSimListOnlySynchronizer");

        if (!thread.isAlive()) thread.start();
    }


    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                Controller_Things_Mobile things_mobile = new Controller_Things_Mobile();
                TM_Sim_List_list list = things_mobile.sim_list();
                //procházím list a hledám pokud v něm sim s MSINumber existuje
                //pokud ne vytvářím si novou a ukládám jí do databáze
                for (TM_Sim_List sim : list.sims) {
                    if (Model_GSM.find.query().where().eq("MSINumber", sim.msisdn).findCount() == 0) {
                        Model_GSM gsm = new Model_GSM();
                        gsm.MSINumber = sim.msisdn;
                        gsm.provider = "ThingsMobile";
                        gsm.registration_hash = UUID.randomUUID();
                        gsm.save();
                    }
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }
    };
}
