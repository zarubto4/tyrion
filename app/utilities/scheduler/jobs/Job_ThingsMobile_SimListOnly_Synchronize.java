package utilities.scheduler.jobs;

import models.Model_GSM;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_List;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_List_list;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;
import utilities.scheduler.Scheduled;

import java.util.UUID;

import static utilities.enums.ServerMode.DEVELOPER;
import static utilities.enums.ServerMode.PRODUCTION;
import static utilities.enums.ServerMode.STAGE;

// Každý den ve 2:30 ráno
@Scheduled("0 30 2 ? * * *")
@Restrict(value = { DEVELOPER, STAGE, PRODUCTION })
public class Job_ThingsMobile_SimListOnly_Synchronize implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_ThingsMobile_SimListOnly_Synchronize.class);

//**********************************************************************************************************************

    public Job_ThingsMobile_SimListOnly_Synchronize() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute:: Executing Job_ThingsMobile_SimListOnly_Synchronize");

        if (!thread.isAlive()) thread.start();
    }


    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("run:: Executing Job_ThingsMobile_SimListOnly_Synchronize");

                logger.trace("run:: Get SimList");
                TM_Sim_List_list list =  Controller_Things_Mobile.sim_list();


                //logger.trace("run:: list:: " + list.prettyPrint());

                //procházím list a hledám pokud v něm sim s msi_number existuje
                //pokud ne vytvářím si novou a ukládám jí do databáze
                for (TM_Sim_List sim : list.sims) {

                    logger.trace("fpr:: msisdn:{} \n overview:: {} ", sim.msisdn, sim.prettyPrint());

                    if (Model_GSM.find.query().where().eq("msi_number", sim.msisdn).findCount() == 0) {

                        Model_GSM gsm = new Model_GSM();
                        gsm.msi_number = sim.msisdn;
                        gsm.provider = "ThingsMobile";
                        gsm.registration_hash = UUID.randomUUID();
                        gsm.save();

                        if(sim.status.equals("not active")) {
                            Controller_Things_Mobile.sim_active(sim.msisdn, null);
                        }

                    }else {
                        logger.trace("fpr:: msisdn:{} ", sim.msisdn + " found already in database");
                    }

                    logger.trace("fpr:: msisdn:{} done. Total {} ", sim.msisdn, sim.cdrs.size());
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.internalServerError(e);
            }
        }
    };
}
