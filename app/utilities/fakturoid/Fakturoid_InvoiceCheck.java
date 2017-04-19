package utilities.fakturoid;

import play.Logger;
import utilities.loggy.Loggy;

import java.util.ArrayList;
import java.util.List;

public class Fakturoid_InvoiceCheck {

    // Logger
    private static Logger.ALogger logger = Logger.of("Loggy");

    private static List<Long> invoices = new ArrayList<>(); // Tady se hromadí id plateb, které je potřeba zkontrolovat

    public static void startInvoiceCheckThread(){
        logger.info("Fakturoid_InvoiceCheck:: startInvoiceCheckThread: starting");
        if(!check_invoice_thread.isAlive()) check_invoice_thread.start();
    }

    public static void addToQueue(Long payment){

        logger.info("Fakturoid_InvoiceCheck:: addToQueue: adding payment to queue");

        invoices.add(payment);

        if(check_invoice_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("GoPay_PaymentCheck:: addToQueue: thread is sleeping, waiting for interruption!");
            check_invoice_thread.interrupt();
        }
    }

    private static Thread check_invoice_thread = new Thread(){

        @Override
        public void run(){

            while(true){
                try{

                    if(!invoices.isEmpty()) {

                        logger.debug("Fakturoid_InvoiceCheck:: check_invoice_thread: checking {} invoices ", invoices.size());

                        Long invoice = invoices.get(0);

                        checkInvoice(invoice);

                        invoices.remove(invoice);

                    } else {

                        logger.debug("Fakturoid_InvoiceCheck:: check_invoice_thread: no invoices, thread is going to sleep");
                        sleep(2100000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    Loggy.internalServerError("Fakturoid_InvoiceCheck:: check_invoice_thread:", e);
                }
            }
        }
    };

    private static void checkInvoice(Long id) {

    }
}
