package utilities.scheduler.jobs;

import models.Model_Invoice;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.api.Play;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.Enum_Payment_status;
import utilities.loggy.Loggy;

import java.util.Date;
import java.util.List;

public class Job_ArtificialGoPayNotification implements Job {

    public Job_ArtificialGoPayNotification(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_ArtificialGoPayNotification:: execute: Executing Job_ArtificialGoPayNotification");

        if(!notify_thread.isAlive()) notify_thread.start();
    }

    private Thread notify_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("Job_ArtificialGoPayNotification:: notify_thread: concurrent thread started on {}", new Date());

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                List<Model_Invoice> invoices = Model_Invoice.find.where()
                        .isNotNull("gopay_id")
                        .eq("status", Enum_Payment_status.pending)
                        .findList();

                for (Model_Invoice invoice : invoices) {

                    F.Promise<WSResponse> responsePromise = ws.url("http://localhost:9000/go_pay/notification?id=" + invoice.gopay_id)
                            .setRequestTimeout(5000)
                            .get();

                    WSResponse response = responsePromise.get(5000);

                    logger.debug("Job_ArtificialGoPayNotification:: notify_thread: Sending notification for payment: {} - response: {}", invoice.gopay_id, response.getStatus());
                }

            } catch (Exception e) {
                Loggy.internalServerError("Job_ArtificialGoPayNotification:: notify_thread:", e);
            }

            logger.debug("Job_ArtificialGoPayNotification:: notify_thread: thread stopped on {}", new Date());
        }
    };
}