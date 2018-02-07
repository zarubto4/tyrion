package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Model_Invoice;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.PaymentStatus;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;
import utilities.scheduler.Scheduled;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to send artificial notification in developer mode.
 * External services like GoPay or Fakturoid cannot send REST notification to localhost.
 */
@Restrict
@Scheduled("10 0/1 * * * ?")
public class Job_ArtificialFinancialCallback implements Job {

    private final WSClient ws;

    @Inject
    public Job_ArtificialFinancialCallback(WSClient ws) {
        this.ws = ws;
    }

    // Logger
    private static final Logger logger = new Logger(Job_ArtificialFinancialCallback.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute - executing Job_ArtificialFinancialCallback");

        if (!callback_thread.isAlive()) callback_thread.start();
    }

    /**
     * Thread finds pending invoices and makes artificial callbacks.
     */
    private Thread callback_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("callback_thread: concurrent thread started on {}", new Date());

                List<Model_Invoice> invoices_for_gopay = Model_Invoice.find.query().where()
                        .isNotNull("gopay_id")
                        .eq("status", PaymentStatus.PENDING)
                        .findList();

                List<Model_Invoice> invoices_for_fakturoid = Model_Invoice.find.query().where()
                        .isNull("gopay_id")
                        .isNotNull("proforma_id")
                        .eq("status", PaymentStatus.PENDING)
                        .eq("proforma", true)
                        .findList();

                for (Model_Invoice invoice : invoices_for_gopay) {

                    CompletionStage<WSResponse> responsePromise = ws.url("http://localhost:9000/go_pay/notification?id=" + invoice.gopay_id)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .get();

                    WSResponse response = responsePromise.toCompletableFuture().get();

                    logger.debug("callback_thread: Sending notification for payment: {} - response: {}", invoice.gopay_id, response.getStatus());
                }

                for (Model_Invoice invoice : invoices_for_fakturoid) {

                    ObjectNode body = Json.newObject();
                    body.put("invoice_id", invoice.proforma_id);
                    body.put("number", invoice.invoice_number);
                    body.put("status", "paid");
                    body.put("total", ((double) invoice.total_price()) / 1000);
                    body.put("event_name", "artificial_notification");

                    CompletionStage<WSResponse> responsePromise = ws.url("http://localhost:9000/fakturoid/callback")
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .post(body);

                    WSResponse response = responsePromise.toCompletableFuture().get();

                    logger.debug("callback_thread: Sending notification for invoice: {} - response: {}", invoice.proforma_id, response.getStatus());
                }
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("callback_thread: thread stopped on {}", new Date());
        }
    };
}