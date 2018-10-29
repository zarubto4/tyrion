package utilities.scheduler.jobs;

import mongo.ModelMongo_ThingsMobile_CRD;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_List;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_List_list;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status_cdr;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;
import utilities.scheduler.Scheduled;

import static utilities.enums.ServerMode.DEVELOPER;
import static utilities.enums.ServerMode.PRODUCTION;
import static utilities.enums.ServerMode.STAGE;

// Každou hodinu v 58 minutu
@Scheduled("0 58 * ? * * *")
@Restrict(value = { DEVELOPER, STAGE, PRODUCTION })
public class Job_ThingsMobile_SimData_Synchronize implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_ThingsMobile_SimData_Synchronize.class);

//**********************************************************************************************************************

    public Job_ThingsMobile_SimData_Synchronize() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute:: Executing Job_ThingsMobile_SimData_Synchronize");

        if (!thread.isAlive()) thread.start();
    }


    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("run:: Executing Job_ThingsMobile_SimData_Synchronize");

                logger.trace("run:: Get SimList");
                TM_Sim_List_list list =  Controller_Things_Mobile.sim_list();

                //procházím list a hledám pokud v něm sim s msi_number existuje
                //pokud ne vytvářím si novou a ukládám jí do databáze
                sim_l: for (TM_Sim_List sim : list.sims) {

                    logger.trace("sim_l:: msisdn:{} crds: ", sim.msisdn, sim.cdrs.size());

                    cdr_c: for(TM_Sim_Status_cdr cdr : sim.cdrs) {

                        if(cdr.cdrNetwork.equals("ActivateJerseyDirect")) {
                            logger.trace("cdr_c:: msisdn:{} ActivateJerseyDirect - continue", sim.msisdn);
                            continue;
                        }


                        ModelMongo_ThingsMobile_CRD find_record = ModelMongo_ThingsMobile_CRD.find.query()
                                .field("msisdn").equal(sim.msisdn)
                                .field("cdrDateStart").equal(cdr.getAsLong_CdrDateStart())
                                .field("cdrDateStop").equal(cdr.getAsLong_CdrDateStart())
                                .get();

                        if(find_record == null) {

                            logger.trace("sim_l:: msisdn:{} new record for start {} and end {} ", sim.msisdn, cdr.cdrDateStart, cdr.cdrDateStop);

                            ModelMongo_ThingsMobile_CRD crd_mongo = new ModelMongo_ThingsMobile_CRD();
                            crd_mongo.msisdn = sim.msisdn;
                            crd_mongo.cdrImsi = cdr.cdrImsi;
                            crd_mongo.cdrDateStart = cdr.getAsLong_CdrDateStart();
                            crd_mongo.cdrDateStop = cdr.getAsLong_CdrDateStart();
                            crd_mongo.cdrNetwork = cdr.cdrNetwork;
                            crd_mongo.cdrCountry = cdr.cdrCountry;
                            crd_mongo.cdrTraffic = cdr.cdrTraffic;
                            crd_mongo.save();

                        } else {
                            logger.trace("sim_l:: msisdn:{} not new Records", sim.msisdn);
                        }
                    }

                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }
    };
}
