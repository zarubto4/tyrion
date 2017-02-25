package utilities.scheduler.schedules_activities;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Certificate_Renewal implements Job {

    public Certificate_Renewal(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!renew_certificate_thread.isAlive()) renew_certificate_thread.start();
    }

    Thread renew_certificate_thread = new Thread() {

        @Override
        public void run() {

            logger.info("Certificate_Renewal:: renew_certificate_thread.run():: started");

            try {

              Process pr = Runtime.getRuntime().exec(System.getProperty("user.dir") + "/app/certificate_renewal.sh");

              int exitVal = pr.waitFor();

              logger.warn("Certificate renewal exited with code " + exitVal);

            } catch (Exception e) {
              logger.error("Certificate renewal failed");
            }

            logger.info("Certificate_Renewal:: renew_certificate_thread.run():: stopped");
        }
    };
}
