package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Invoice;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.Enum_Payment_status;
import utilities.logger.Class_Logger;

import java.util.Date;
import java.util.List;

public class Job_ArtificialFinancialCallback implements Job {

    public Job_ArtificialFinancialCallback(){}

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Job_ArtificialFinancialCallback.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_ArtificialFinancialCallback");

        if(!callback_thread.isAlive()) callback_thread.start();
    }

    private Thread callback_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.debug("callback_thread: concurrent thread started on {}", new Date());

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                List<Model_Invoice> invoices_for_gopay = Model_Invoice.find.where()
                        .isNotNull("gopay_id")
                        .eq("status", Enum_Payment_status.pending)
                        .findList();

                List<Model_Invoice> invoices_for_fakturoid = Model_Invoice.find.where()
                        .isNotNull("fakturoid_id")
                        .eq("status", Enum_Payment_status.paid)
                        .eq("proforma", true)
                        .findList();

                for (Model_Invoice invoice : invoices_for_gopay) {

                    F.Promise<WSResponse> responsePromise = ws.url("http://localhost:9000/go_pay/notification?id=" + invoice.gopay_id)
                            .setRequestTimeout(5000)
                            .get();

                    WSResponse response = responsePromise.get(5000);

                    terminal_logger.debug("callback_thread: Sending notification for payment: {} - response: {}", invoice.gopay_id, response.getStatus());
                }

                for (Model_Invoice invoice : invoices_for_fakturoid) {

                    ObjectNode body = Json.newObject();
                    body.put("invoice_id", invoice.fakturoid_id);
                    body.put("number", invoice.invoice_number);
                    body.put("status", "paid");
                    body.put("total", ((double) invoice.total_price()) / 1000);
                    body.put("event_name", "artificial_notification");

                    F.Promise<WSResponse> responsePromise = ws.url("http://localhost:9000/fakturoid/callback")
                            .setRequestTimeout(5000)
                            .post(body);

                    WSResponse response = responsePromise.get(5000);

                    terminal_logger.debug("callback_thread: Sending notification for invoice: {} - response: {}", invoice.fakturoid_id, response.getStatus());
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("callback_thread:", e);
            }

            terminal_logger.debug("callback_thread: thread stopped on {}", new Date());
        }
    };
}