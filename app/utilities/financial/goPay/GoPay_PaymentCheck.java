package utilities.financial.goPay;

/**
 * Class is used to check GoPay payments when notification is received.
 */
public class GoPay_PaymentCheck {
/*
    // Logger
    private static final Logger logger = new Logger(GoPay_PaymentCheck.class);

    private GoPay goPay;
    private WSClient ws;
    private FormFactory formFactory;
    private Fakturoid fakturoid;

    @Inject
    public GoPay_PaymentCheck(GoPay goPay, WSClient ws, FormFactory formFactory, Fakturoid fakturoid) {
        this.goPay = goPay;
        this.ws = ws;
        this.formFactory = formFactory;
        this.fakturoid = fakturoid;
    }

    /**
     * List of ids of payments that needs to be checked.
     * This is the queue.
     *//*
    private static List<Long> payments = new ArrayList<>();*/

    /**
     * Method starts the concurrent thread.
     *//*
    public static void startThread() {
        logger.info("startThread: starting");
        if (!check_payment_thread.isAlive()) check_payment_thread.start();
    }*/

    /**
     * Method adds payment to the queue and interrupts the thread if it is needed
     * @param payment Long id of payment (invoice.gopay_id)
     *//*
    public static void addToQueue(Long payment) {

        logger.info("addToQueue: adding payment to queue");

        payments.add(payment);

        if (check_payment_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("addToQueue: thread is sleeping, waiting for interruption!");
            check_payment_thread.interrupt();
        }
    }*/

    /**
     * Thread with an infinite loop inside. The thread goes to sleep when there is no payment to check.
     *//*
    private static Thread check_payment_thread = new Thread() {

        @Override
        public void run() {

            while(true) {
                try {

                    if (!payments.isEmpty()) {

                        logger.debug("check_payment_thread: checking {} payments ", payments.size());

                        Long payment = payments.get(0);

                        //checkPayment(payment);

                        payments.remove(payment);

                    } else {

                        logger.debug("check_payment_thread: no payments, thread is going to sleep");
                        sleep(2100000000);
                    }
                } catch (InterruptedException i) {
                    // Do nothing
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    };*/

    /**
     * Method gets the payment from GoPay and checks its status.
     * Adds credit to a product if it is paid and fire Fakturoid event "pay_proforma".
     * @param id Id of the given payment.
     *//*
    public void checkPayment(Long id) {
        try {

            String local_token = goPay.getToken();

            logger.debug("checkPayment: Asking for payment state: gopay_id - {}", id);

            logger.debug("checkPayment: Getting invoice");
            Model_Invoice invoice = Model_Invoice.find.query().where().eq("gopay_id", id).findOne();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database. Gopay ID was: " + id);

            try {

                // Some operations require more tries
                for (int trial = 5; trial > 0; trial--) {

                    WSResponse response;

                    JsonNode result;

                    try {

                        CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + id)
                                .setContentType("application/x-www-form-urlencoded")
                                .addHeader("Accept", "application/json")
                                .addHeader("Authorization", "Bearer " + local_token)
                                .setRequestTimeout(Duration.ofSeconds(5))
                                .get();

                        response = responsePromise.toCompletableFuture().get();

                        result = response.asJson();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                        Thread.sleep(2500);
                        continue;
                    }

                    logger.debug("checkPayment: Status: " + response.getStatus() + " Response " + result);

                    if (response.getStatus() == 200) {
                        final Form<GoPay_Result> form = formFactory.form(GoPay_Result.class).bind(result);
                        if (form.hasErrors()) throw new Exception("Error while binding Json: " + form.errorsAsJson().toString());
                        GoPay_Result help = form.get();

                        switch (help.state) {

                            case "PAID": {

                                logger.debug("checkPayment: state - PAID");

                                if (invoice.status != InvoiceStatus.PAID) {

                                    if (!fakturoid.fakturoid_post("/invoices/" + invoice.proforma_id + "/fire.json?event=pay_proforma"))
                                        logger.internalServerError(new Exception("Error changing status to paid on Fakturoid. Inconsistent state."));

                                    invoice.getProduct().credit_upload(help.amount * 10);
                                    invoice.status = InvoiceStatus.PAID;
                                    invoice.paid = new Date();
                                    invoice.update();

                                    invoice.getProduct().archiveEvent("Payment", "GoPay payment number: " + invoice.gopay_id + " was successful", invoice.id);
                                    invoice.notificationPaymentSuccess(((double) help.amount) / 100);
                                }

                                break;
                            }

                            case "PAYMENT_METHOD_CHOSEN": {

                                logger.debug("checkPayment: state - PAYMENT_METHOD_CHOSEN");

                                invoice.notificationPaymentIncomplete();

                                break;
                            }

                            case "REFUNDED": {

                                logger.debug("checkPayment: state - REFUNDED");

                                invoice.getProduct().credit_remove(help.amount * 10);
                                invoice.status = InvoiceStatus.CANCELED;
                                invoice.update();

                                invoice.getProduct().archiveEvent("Refund Payment", "GoPay payment number: " + invoice.gopay_id + " was successfully refunded", invoice.id);
                                invoice.getProduct().notificationRefundPaymentSuccess(((double) help.amount) / 100);

                                // TODO úprava ve fakturoidu

                                break;
                            }

                            case "PARTIALLY_REFUNDED": {

                                logger.debug("checkPayment: state - PARTIALLY_REFUNDED");

                                // TODO úprava ve fakturoidu

                                invoice.getProduct().credit_remove(help.amount * 10);

                                invoice.getProduct().archiveEvent("Refund Payment", "GoPay payment number: " + invoice.gopay_id + " was successfully partially refunded", invoice.id);
                                invoice.getProduct().notificationRefundPaymentSuccess(((double) help.amount) / 100);

                                break;
                            }

                            case "CANCELED": {

                                logger.debug("checkPayment: state - CANCELED");

                                invoice.status = InvoiceStatus.CANCELED;
                                invoice.gw_url = null;
                                invoice.update();

                                if (!fakturoid.fakturoid_post("/invoices/" + (invoice.proforma ? invoice.proforma_id : invoice.fakturoid_id) + "/fire.json?event=cancel"))
                                    logger.internalServerError(new Exception("Error changing status to canceled on Fakturoid. Inconsistent state."));

                                break;
                            }

                            case "TIMEOUTED": {

                                logger.debug("checkPayment: state - TIMEOUTED");

                                // TODO notifikace

                                break;
                            }

                            case "CREATED": {

                                logger.debug("checkPayment: state - CREATED");

                                // TODO notifikace

                                break;
                            }

                            default:
                                throw new Exception("Payment state could not be handled. State: " + help.state);
                        }

                        break;

                    } else {

                        throw new Exception("Cannot obtain payment. Response from GoPay was: " + result);
                    }
                }
            } catch (Exception e) {
                // TODO náhrada platby?

                invoice.notificationPaymentFail();

                fakturoid.sendInvoiceReminderEmail(invoice, "We were unable to take money from your credit card. " +
                        "Please check your payment credentials or contact our support if anything is unclear. " +
                        "You can also pay this invoice manually through your Byzance account.");
            }
        } catch (Exception e) {

            logger.internalServerError(e);
        }
    }*/
}
