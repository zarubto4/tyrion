package utilities.schedules_activities;


import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.Application;

public class Certificate_Renewal implements Job {

    @Inject
    Application application;

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

              Process pr = Runtime.getRuntime().exec(application.path() + "/certificate_renewal.sh");

              int exitVal = pr.waitFor();

              logger.warn("Certificate renewal exited with code " + exitVal);

            } catch (Exception e) {
              logger.error("Certificate renewal failed");
            }

            logger.info("Certificate_Renewal:: renew_certificate_thread.run():: stopped");
        }
    };
}
