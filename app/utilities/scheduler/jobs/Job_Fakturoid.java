package utilities.scheduler.jobs;

import com.google.inject.Inject;
import models.Model_Invoice;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.InvoiceStatus;
import utilities.financial.fakturoid.FakturoidService;
import utilities.financial.services.InvoiceService;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 *
 * Fakturoid sends us a notification, when an invoice is paid. In case this callback is missed or not processed correctly,
 * we periodically check for unpaid invoices.
 *
 */
@Scheduled("0 0 * * * ?")
public class Job_Fakturoid implements Job {
    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_Fakturoid.class);

    //**********************************************************************************************************************

    private FakturoidService fakturoid;

    private InvoiceService invoiceService;

    @Inject
    public Job_Fakturoid(FakturoidService fakturoid, InvoiceService invoiceService) {
        this.fakturoid = fakturoid;
        this.invoiceService = invoiceService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("execute: Executing Job_Fakturoid");
        if (!fakturoid_thread.isAlive()) fakturoid_thread.start();
    }

    private Thread fakturoid_thread = new Thread() {
        @Override
        public void run() {
            Collection<Model_Invoice> paidInvoicesWithNoDetails = invoiceService.getPaidInvoicesWithNoDetails();
            logger.info("Found {} paid proforma that are not synchronized with Fakturoid. Try to update them.", paidInvoicesWithNoDetails.size());
            paidInvoicesWithNoDetails.forEach(invoice -> checkPaidProforma(invoice));

            // every six hours, check if we did not miss any update of the pending or overdue invoice
            if(LocalDateTime.now().getHour() % 6 == 0) {
                Collection<Model_Invoice> overdueAndPending = invoiceService.getInvoices(InvoiceStatus.OVERDUE);
                overdueAndPending.addAll(invoiceService.getInvoices(InvoiceStatus.PENDING));
                logger.info("Found {} overdue and pending proforma. Check if they are paid", overdueAndPending.size());

                overdueAndPending.forEach(invoice -> checkPaidProforma(invoice));
            }
        }
    };

    private void checkPaidProforma(Model_Invoice invoice) {
        try {
            fakturoid.checkPaidProforma(invoice);
        } catch (Exception e) {
            logger.error("Error when checking the state of a proforma. ", e);
        }
    }
}
