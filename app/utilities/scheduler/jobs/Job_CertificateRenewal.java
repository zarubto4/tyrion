package utilities.scheduler.jobs;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.loggy.Loggy;

import java.util.Date;

public class Job_CertificateRenewal implements Job {

    public Job_CertificateRenewal(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_CertificateRenewal:: execute: Executing Job_CertificateRenewal");

        if(!renew_certificate_thread.isAlive()) renew_certificate_thread.start();
    }

    private Thread renew_certificate_thread = new Thread() {

        @Override
        public void run() {

            logger.debug("Job_CertificateRenewal:: renew_certificate_thread: concurrent thread started on {}", new Date());

            try {

              Process pr = Runtime.getRuntime().exec(System.getProperty("user.dir") + "/app/certificate_renewal.sh");

              int exitVal = pr.waitFor();

              logger.warn("Job_CertificateRenewal:: renew_certificate_thread: renewal script exited with code " + exitVal);

            } catch (Exception e) {
                Loggy.internalServerError("Job_CertificateRenewal:: renew_certificate_thread:", e);
            }

            logger.debug("Job_CertificateRenewal:: renew_certificate_thread: thread stopped on {}", new Date());
        }
    };
}
