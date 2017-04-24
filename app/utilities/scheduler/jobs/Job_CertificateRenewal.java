package utilities.scheduler.jobs;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;
import web_socket.message_objects.common.WS_Send_message;


import java.util.Date;

public class Job_CertificateRenewal implements Job {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_Send_message.class);

//**********************************************************************************************************************

    public Job_CertificateRenewal(){}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("Job_CertificateRenewal:: execute: Executing Job_CertificateRenewal");

        if(!renew_certificate_thread.isAlive()) renew_certificate_thread.start();
    }

    private Thread renew_certificate_thread = new Thread() {

        @Override
        public void run() {

            terminal_logger.debug("Job_CertificateRenewal:: renew_certificate_thread: concurrent thread started on {}", new Date());

            try {

              Process pr = Runtime.getRuntime().exec(System.getProperty("user.dir") + "/app/certificate_renewal.sh");

              int exitVal = pr.waitFor();

                terminal_logger.warn("Job_CertificateRenewal:: renew_certificate_thread: renewal script exited with code " + exitVal);

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.debug("Job_CertificateRenewal:: renew_certificate_thread: thread stopped on {}", new Date());
        }
    };
}
