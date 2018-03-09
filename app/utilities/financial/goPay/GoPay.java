package utilities.financial.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import controllers._BaseController;
import models.Model_Product;
import models.Model_Invoice;
import models.Model_PaymentDetails;
import play.api.Play;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.enums.Currency;
import utilities.enums.PaymentStatus;
import utilities.enums.RecurrenceCycle;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.helps_objects.*;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Payment_Refund;

import javax.validation.ValidationException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Class is used to communicate with GoPay,
 * contains operations like single payment,
 * on demand payment, refund payment etc.
 */
@Singleton
public class GoPay extends _BaseController {

    // Logger
    private static final Logger logger = new Logger(GoPay.class);

    /**
     * Hash token used to authenticate the application in GoPay, token has TTL 30 minutes.
     */
    private String token;

    /**
     * Date when the token was last updated, so it is not needed to ask for new token for every request.
     */
    private Date last_refresh;

    private WSClient ws;
    private FormFactory formFactory;
    private Fakturoid fakturoid;

    @Inject
    public GoPay(WSClient ws, FormFactory formFactory, Fakturoid fakturoid) {
        this.ws = ws;
        this.formFactory = formFactory;
        this.fakturoid = fakturoid;
    }

// PUBLIC METHODS ######################################################################################################

    /**
     * Method serves to retrieve the token from GoPay if the local token is expired else it returns local static token.
     * Method tries 5 times to get the token.
     * @return String token from GoPay or null if error occur.
     */
    public String getToken() {
        try {

            logger.debug("getToken: Getting Token");
            if ( token != null && last_refresh != null && new Date().getTime() - last_refresh.getTime() <= 28*60*1000) {
                logger.debug("getToken: Returning cached token");
                return token;
            }

            logger.debug("getToken: Token is expired or not obtained yet.");

            int trial = 0;

            while (trial < 5) {

                JsonNode result;

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/oauth2/token")
                            .setContentType("application/x-www-form-urlencoded")
                            .setAuth(Server.GoPay_client_id, Server.GoPay_client_secret)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .setBody("grant_type=client_credentials&scope=payment-all")
                            .post("grant_type=client_credentials&scope=payment-all");

                    result = responsePromise.toCompletableFuture().get().asJson();

                } catch (Exception e) {

                    trial++;
                    continue;
                }


                if (result.has("access_token")) {
                    token = result.get("access_token").asText();
                    last_refresh = new Date();

                    logger.debug("getToken: Returning new token");

                    return token;

                } else {
                    token = null;
                    throw new NullPointerException("Incoming Json from GoPay does not contain access_token: " + result);
                }
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /**
     * Method is used to provide payment through GoPay.
     * It provides a single payment or creates recurrence if user allowed on demand payments.
     * Important field, that is returned in invoice, is "gw_url" where user can access the payment gate.
     * The method will try 5 times to get the result.
     * @param payment_description String name of the payment.
     * @param product Model product the payment is for.
     * @param invoice Related model invoice where details are stored.
     * @return Related invoice with details of payment.
     */
    public Model_Invoice singlePayment(String payment_description , Model_Product product, Model_Invoice invoice) {

        logger.debug("singlePayment: Creating new payment");

        GoPay_Payment payment = new GoPay_Payment();
        payment.setItems(invoice.invoice_items);
        payment.order_number = invoice.invoice_number;
        payment.currency = Currency.CZK;
        payment.order_description = payment_description;

        if (product.on_demand && product.gopay_id == null) {

            payment.recurrence = new Recurrence();
            payment.recurrence.recurrence_cycle = RecurrenceCycle.ON_DEMAND;
        }

        GoPay_Payer payer = new GoPay_Payer();
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYMENT_CARD);
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYSAFECARD);
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYPAL);
        payer.payment_instrument         = GoPay_Payer.PaymentInstrument.PAYMENT_CARD;
        payer.default_payment_instrument = GoPay_Payer.PaymentInstrument.PAYMENT_CARD;


        Model_PaymentDetails details = product.payment_details;

        GoPay_Contact payerContact = new GoPay_Contact();
        payerContact.first_name     = details.full_name;
        payerContact.email          = details.invoice_email;
        payerContact.street         = details.street + " " + details.street_number;
        payerContact.postal_code    = details.zip_code;
        payerContact.country_code   = details.country;
        payerContact.city           = details.city;

        if (details.company_account) {
            payerContact.phone_number = details.company_authorized_phone;
        }

        payer.contact               = payerContact;

        payment.payer               = payer;
        payment.lang                = GoPay_Payer.Lang.EN;

        String local_token = getToken();
        if (local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        logger.debug("singlePayment: Sending Request for new Payment to GoPay with object: " + Json.toJson(payment).toString());

        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        JsonNode response;

        for (int trial = 5; trial > 0; trial--) {
            try {

                CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment")
                        .setContentType("application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(Duration.ofSeconds(10))
                        .post(Json.toJson(payment));

                response = responsePromise.toCompletableFuture().get().asJson();

            } catch (Exception e) {
                logger.internalServerError(new Exception("Error getting result", e));
                continue;
            }


            logger.debug("singlePayment: Response from GoPay: " + response.toString());

            final Form<GoPay_Result> form = formFactory.form(GoPay_Result.class).bind(response);
            if (form.hasErrors())
                logger.internalServerError(new Exception("Error while binding Json: " + form.errorsAsJson().toString()));
            else {
                GoPay_Result help = form.get();

                logger.debug("singlePayment: Set GoPay ID to Invoice");

                invoice.gopay_id = help.id;
                invoice.gopay_order_number = help.order_number;
                invoice.gw_url = help.gw_url;
                invoice.update();

                product.archiveEvent("Single payment", "Single GoPay payment number: " + invoice.gopay_id + " was created", invoice.id);

                if (help.recurrence != null && help.recurrence.recurrence_cycle == RecurrenceCycle.ON_DEMAND) {

                    logger.debug("singlePayment: Set GoPay ID to Product because it is ON_DEMAND payment");

                    product.archiveEvent("On demand payment", "On demand GoPay payment number: " + invoice.gopay_id + " was set", invoice.id);

                    product.gopay_id = invoice.gopay_id;
                    product.update();
                }
            }
            break;
        }
        return invoice;
    }

    /**
     * This method is used to automatically take money from credit card.
     * The method tries 5 times to get the result.
     * @param invoice Model invoice related to payment.
     * @throws Exception Exception is thrown if some error occur. (e.g. GoPay does not return status 200 OK)
     */
    public void onDemandPayment(Model_Invoice invoice) throws Exception{

        logger.debug("onDemandPayment: Starting with procedure ON_DEMAND - taking money from Credit-Card");

        Calendar cal = Calendar.getInstance();

        // String[] monthNames_cz = {"Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"};
        String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        logger.debug("onDemandPayment: Creating GoPay_Recurrence");
        GoPay_Recurrence recurrence = new GoPay_Recurrence();
        recurrence.currency = Currency.USD;
        recurrence.setItems(invoice.invoice_items());
        recurrence.order_number  = invoice.invoice_number;
        recurrence.order_description =  "Services for " + monthNames_en[cal.get(Calendar.MONTH)];

        // Token
        logger.debug("onDemandPayment: Asking for token");
        String local_token = getToken();

        if (local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + invoice.getProduct().gopay_id + "/create-recurrence")
                        .setContentType("application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(Duration.ofSeconds(10))
                        .post(Json.toJson(recurrence));

                logger.debug("onDemandPayment: Sending request for new payment!");
                response = responsePromise.toCompletableFuture().get();

            } catch (Exception e) {
                logger.internalServerError(new Exception("Error getting result", e));
                Thread.sleep(2500);
                continue;
            }
        /*
         350	Stržení platby selhalo
         351	Stržení platby provedeno
         352	Zrušení přeautorizace selhalo
         353	Zrušení předautorizace provedeno
         340	Provedení opakované platby selhalo
         341	Provedení opakované platby není podporováno
         342	Opakování platby zastaveno
         343	Překročen časový limit počtu provedení opakované platby
         330	Platbu nelze vrátit
         331	Platbu nelze vrátit
         332	Chybná částka
         333	Nedostatek peněz na účtu
         301	Platbu nelze vytvořit
         302	Platbu nelze provést
         303	Platba v chybném stavu
         304	Platba nebyla nalezena
         */

            JsonNode result = response.asJson();

            logger.debug("onDemandPayment: Status: {}, Result: {}", response.getStatus(), result);

            switch (response.getStatus()) {

                case 200: {

                    // Binding Json with help object
                    final Form<GoPay_Result> form = formFactory.form(GoPay_Result.class).bind(result);
                    if (form.hasErrors())
                        throw new Exception("Error while binding Json: " + form.errorsAsJson().toString());
                    GoPay_Result help = form.get();

                    invoice.gopay_id = help.id;
                    invoice.gw_url = null;
                    invoice.update();

                    invoice.getProduct().archiveEvent("On demand payment", "GoPay on demand payment number: " + invoice.gopay_id + " was successful", invoice.id);

                    return;
                }

                case 409: {

                    invoice.getProduct().archiveEvent("On demand payment", "GoPay on demand payment number: " + invoice.gopay_id + " was unsuccessful due to some validation error", invoice.id);

                    throw new ValidationException("Payment could not be made due to some validation error. Response: " + result.toString());
                }

                default: {

                    invoice.getProduct().archiveEvent("On demand payment", "GoPay on demand payment number: " + invoice.gopay_id + " was unsuccessful", invoice.id);

                    throw new Exception("Cannot take money from credit card, GoPay service returned error. Response: " + result.toString());
                }
            }
        }
    }

    /**
     * This method serves to cancel on demand payment if user asks for it.
     * The method will try 5 times to get the result.
     * @param product Model product the on demand payment is terminated for.
     * @throws Exception Exception is thrown if some error occur. (e.g. GoPay does not return status 200 OK)
     */
    public void terminateOnDemand(Model_Product product) throws Exception{

        // Token
        logger.debug("terminateOnDemand: Asking for token");
        String local_token = getToken();

        if (local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + product.gopay_id + "/void-recurrence")
                        .setContentType("application/x-www-form-urlencoded")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + local_token)
                        .setMethod("POST")
                        .setRequestTimeout(Duration.ofSeconds(10))
                        .post("");

                logger.debug("terminateOnDemand: Sending request to terminate on demand payment!");

                response = responsePromise.toCompletableFuture().get();

            } catch (Exception e) {
                logger.internalServerError(new Exception("Error getting result", e));
                continue;
            }

            JsonNode result = response.asJson();

            if (response.getStatus() == 200 && result.has("result") && result.get("result").asText().equals("FINISHED")) {

                product.archiveEvent("Terminate on demand payment", "GoPay on demand payment number: " + product.gopay_id + " was terminated", null);
                product.notificationTerminateOnDemand(true);

                return;

            } else {

                product.archiveEvent("Terminate on demand payment", "Failed to terminate GoPay on demand payment number: " + product.gopay_id, null);
                product.notificationTerminateOnDemand(false);

                logger.internalServerError(new Exception("Cannot terminate on demand payment. Response from GoPay was: " + result.toString()));
            }
        }
    }

    /**
     * This method requests the payment refund on GoPay.
     * The method will try 5 times to get the result.
     * @param invoice Model invoice to refund.
     * @param amount Long amount equal or lesser than full amount in invoice.
     * @throws Exception Exception is thrown if some error occur. (e.g. GoPay does not return status 200 OK)
     */
    private void refundPayment(Model_Invoice invoice, Long amount) throws Exception{

        // Token
        logger.debug("refundPayment: Asking for token");
        String local_token = getToken();

        if (local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        ObjectNode json = Json.newObject();
        json.put("amount", amount / 10); // přepočet na centy

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                CompletionStage<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + invoice.gopay_id + "/refund")
                        .setContentType("application/x-www-form-urlencoded")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(Duration.ofSeconds(10))
                        .post(Json.toJson(json));

                logger.debug("refundPayment: Sending request to refund payment!");

                response = responsePromise.toCompletableFuture().get();

            } catch (Exception e) {
                logger.internalServerError(new Exception("Error getting result", e));
                continue;
            }

            JsonNode result = response.asJson();

            if (response.getStatus() == 200 && result.has("result") && result.get("result").asText().equals("FINISHED")) {

                invoice.getProduct().archiveEvent("Refund payment", "Refund GoPay payment number: " + invoice.gopay_id + " was requested", invoice.id);

                return;

            } else {

                invoice.getProduct().archiveEvent("Refund payment", " Refund for GoPay payment number: " + invoice.gopay_id + " was requested, but was not successful", invoice.id);

                throw new Exception("Cannot refund payment. Response from GoPay was: status " + response.getStatus() + ", body: " + result.toString());
            }
        }
    }

    /**
     * Method gets the payment from GoPay and checks its status.
     * Adds credit to a product if it is paid and fire Fakturoid event "pay_proforma".
     * @param id Id of the given payment.
     */
    public void checkPayment(Long id) {
        try {

            String local_token = this.getToken();

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

                                if (invoice.status != PaymentStatus.PAID) {

                                    if (!fakturoid.fakturoid_post("/invoices/" + invoice.proforma_id + "/fire.json?event=pay_proforma"))
                                        logger.internalServerError(new Exception("Error changing status to paid on Fakturoid. Inconsistent state."));

                                    invoice.getProduct().credit_upload(help.amount * 10);
                                    invoice.status = PaymentStatus.PAID;
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
                                invoice.status = PaymentStatus.CANCELED;
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

                                invoice.status = PaymentStatus.CANCELED;
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
    }

// PUBLIC controllers METHOD ###########################################################################################

    /**
     * This method serves for RestApi call from Tyrion administration to refund a payment.
     * Only user with admin permissions can do this.
     * @param invoice_id String id of the invoice that is being refunded.
     * @return Result ok if request was successful.
     */
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result payment_refund(UUID invoice_id) {
        try {

            // Binding Json with help object
            final Form<Swagger_Payment_Refund> form = formFactory.form(Swagger_Payment_Refund.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Payment_Refund help = form.get();

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            invoice.getProduct().archiveEvent("Refund payment", "Request for refund for this reason: " + help.reason, null);

            if (help.whole) refundPayment(invoice, invoice.total_price());
            else if (help.amount != null) refundPayment(invoice, (long) (help.amount * 1000));
            else return badRequest("Set 'whole' parameter to true or specify amount.");

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    /**
     * Endpoint for RestApi notification from GoPay.
     * Adds payment to queue in GoPay_PaymentCheck.class.
     * @param id Id of a payment that needs to be checked.
     * @return Result ok every time.
     */
    public Result payment_notification(Long id) {
        try {

            this.checkPayment(id);

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return ok();
        }
    }

    /**
     * User is returned here if payment is complete.
     * Adds payment to queue in GoPay_PaymentCheck.class.
     * @param id Id of a payment that needs to be checked.
     * @return Result redirect to Becki every time.
     */
    public Result payment_return(Long id) {
        try {

            this.checkPayment(id);

            return redirect(Server.becki_mainUrl);

        } catch (Exception e) {
            logger.internalServerError(e);
            return redirect(Server.becki_mainUrl);
        }
    }
}