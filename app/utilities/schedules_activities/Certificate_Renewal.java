package utilities.schedules_activities;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Certificate_Renewal implements Job {

    public Certificate_Renewal(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        if(renew_certificate_thread.getState() == Thread.State.NEW) {

            renew_certificate_thread.start();
        } else {

            renew_certificate_thread.interrupt();
        }
    }

    static Thread renew_certificate_thread = new Thread() {

        @Override
        public void run() {

            while (true) {
                try {
                    logger.info("Independent Thread in Certificate_Renewal now working");
                    /*
                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec("certificate_renewal.sh");

                    int exitVal = pr.waitFor();

                    logger.warn("Certificate renewal exited with code " + exitVal);
                    */
                    sleep(90000000);

                } catch (InterruptedException i) {
                    // Do nothing
                } catch (Exception e) {
                    logger.error("Certificate renewal failed");
                }
            }

        }
    };
}
