package utilities.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Product;
import models.Model_Invoice;
import models.Model_PaymentDetails;
import play.Logger;
import play.Logger.ALogger;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_mode;

import utilities.enums.Enum_Recurrence_cycle;
import utilities.goPay.helps_objects.*;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.swagger.documentationClass.Swagger_Payment_Refund;

import javax.validation.ValidationException;
import java.util.Calendar;
import java.util.Date;

public class Utilities_GoPay_Controller extends Controller {

    // Logger
    private static ALogger logger = Logger.of("Loggy");

    private static String token;        // GoPay Connector - hash token sloužící k volání API k bráně GoPay
    private static Date last_refresh;   // čas, který hodnotí obnovu bezpečnostního tokenu - jeho živostnost je totiž 30 minut a šetří se tím dotazy!

// PUBLIC METHODS ######################################################################################################

    // Získání Tokenu od goPay - Ten se přidává do všech hlaviček
    public static String getToken(){
        try {

            logger.debug("GoPay_Controller:: getToken: Getting Token");
            if( token != null && last_refresh != null && new Date().getTime() - last_refresh.getTime() <= 28*60*1000) {
                logger.debug("GoPay_Controller:: getToken: Returning cached token");
                return token;
            }

            logger.debug("GoPay_Controller:: getToken: Token is expired or not obtained yet.");

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            int trial = 0;

            while (trial < 5) {

                JsonNode result;

                try {

                    F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/oauth2/token")
                            .setContentType("application/x-www-form-urlencoded")
                            .setAuth(Server.GoPay_client_id, Server.GoPay_client_secret)
                            .setRequestTimeout(5000)
                            .setBody("grant_type=client_credentials&scope=payment-all")
                            .post("grant_type=client_credentials&scope=payment-all");

                    result = responsePromise.get(5000).asJson();

                } catch (Exception e){

                    trial++;
                    continue;
                }


                if (result.has("access_token")) {
                    token = result.get("access_token").asText();
                    last_refresh = new Date();

                    logger.debug("GoPay_Controller:: getToken: Returning new token");

                    return token;

                } else {
                    token = null;
                    throw new NullPointerException("Incoming Json from GoPay does not contain access_token: " + result);
                }
            }

            return null;
        }catch (Exception e){
            Loggy.internalServerError("GoPay_Controller:: getToken:", e);
            return null;
        }
    }

    public static Model_Invoice singlePayment(String payment_description , Model_Product product, Model_Invoice invoice){

        //Rozhodnutí jestli jendnorázové nebo měsíční!

        logger.debug("GoPay_Controller:: singlePayment: Creating new payment");

        GoPay_Payment payment = new GoPay_Payment();
        payment.setItems(invoice.invoice_items);
        payment.order_number = invoice.invoice_number;
        payment.currency = Enum_Currency.CZK;
        payment.order_description = payment_description;

        if (!product.on_demand && !(product.mode == Enum_Payment_mode.free || product.mode == Enum_Payment_mode.per_credit)) {

            payment.recurrence = new Recurrence();
            payment.recurrence.recurrence_cycle = Enum_Recurrence_cycle.ON_DEMAND;

            Calendar cal = Calendar.getInstance();

            if (product.mode == Enum_Payment_mode.monthly) {

                product.monthly_day_period = cal.get(Calendar.DAY_OF_MONTH) > 28 ? 28 : cal.get(Calendar.DAY_OF_MONTH);

            } else if (product.mode == Enum_Payment_mode.annual) {

                product.monthly_year_period = cal.get(Calendar.DAY_OF_YEAR);
            }
        }

        GoPay_Payer payer = new GoPay_Payer();
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYMENT_CARD);
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYSAFECARD);
        payer.allowed_payment_instruments.add(GoPay_Payer.PaymentInstrument.PAYPAL);
        payer.payment_instrument         = GoPay_Payer.PaymentInstrument.PAYMENT_CARD;
        payer.default_payment_instrument = GoPay_Payer.PaymentInstrument.PAYMENT_CARD;


        Model_PaymentDetails details = product.payment_details;

        GoPay_Contact payerContact = new GoPay_Contact();
        payerContact.first_name     = details.person.full_name;
        payerContact.email          = details.invoice_email;
        payerContact.street         = details.street + " " + details.street_number;
        payerContact.postal_code    = details.zip_code;
        payerContact.country_code   = details.country;
        payerContact.city           = details.city;

        if(details.company_account) {
            payerContact.phone_number = details.company_authorized_phone;
        }

        payer.contact               = payerContact;

        payment.payer               = payer;
        payment.lang                = GoPay_Payer.Lang.EN;

        String local_token = getToken();

        if(local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        logger.debug("GoPay_Controller:: singlePayment: Sending Request for new Payment to GoPay with object: " + Json.toJson(payment).toString());

        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        JsonNode response;

        for (int trial = 5; trial > 0; trial--) {
            try {

                F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment")
                        .setContentType("application/json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(10000)
                        .post(Json.toJson(payment));

                response = responsePromise.get(10000).asJson();

            } catch (Exception e) {
                Loggy.internalServerError("GoPay_Controller:: singlePayment: ", new Exception("Error getting result"));
                continue;
            }


            logger.debug("GoPay_Controller:: singlePayment: Response from GoPay: " + response.toString());

            final Form<GoPay_Result> form = Form.form(GoPay_Result.class).bind(response);
            if (form.hasErrors())
                Loggy.internalServerError("GoPay_Controller:: singlePayment: ", new Exception("Error while binding Json: " + form.errorsAsJson().toString()));
            else {
                GoPay_Result help = form.get();

                logger.debug("GoPay_Controller:: singlePayment: Set GoPay ID to Invoice");

                invoice.gopay_id = help.id;
                invoice.gopay_order_number = help.order_number;
                invoice.gw_url = help.gw_url;
                invoice.update();

                product.archiveEvent("Single payment", "Single GoPay payment number: " + invoice.gopay_id + " was created", invoice.id);

                if (help.recurrence != null && help.recurrence.recurrence_cycle == Enum_Recurrence_cycle.ON_DEMAND) {

                    logger.debug("GoPay_Controller:: singlePayment: Set GoPay ID to Product because it is ON_DEMAND payment");

                    product.archiveEvent("On demand payment", "On demand GoPay payment number: " + invoice.gopay_id + " was set", invoice.id);

                    product.gopay_id = invoice.gopay_id;
                    product.on_demand = true;
                    product.update();
                }
            }
            break;
        }
        return invoice;
    }

    public static void onDemandPayment(Model_Invoice invoice) throws Exception{

        logger.debug("GoPay_Controller:: onDemandPayment: Starting with procedure ON_DEMAND - taking money from Credit-Card");

        Calendar cal = Calendar.getInstance();

        // String[] monthNames_cz = {"Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"};
        String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        logger.debug("GoPay_Controller:: onDemandPayment: Creating GoPay_Recurrence");
        GoPay_Recurrence recurrence = new GoPay_Recurrence();
        recurrence.currency = Enum_Currency.USD;
        recurrence.setItems(invoice.getInvoiceItems());
        recurrence.order_number  = invoice.invoice_number;
        recurrence.order_description =  "Services for " + monthNames_en[cal.get(Calendar.MONTH)];

        // Token
        logger.debug("GoPay_Controller:: onDemandPayment: Asking for token");
        String local_token = getToken();

        if(local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + invoice.getProduct().gopay_id + "/create-recurrence")
                        .setContentType("application/json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(10000)
                        .post(Json.toJson(recurrence));

                logger.debug("GoPay_Controller:: onDemandPayment: Sending request for new payment!");
                response = responsePromise.get(10000);

            } catch (Exception e) {
                Loggy.internalServerError("GoPay_Controller:: onDemandPayment: ", new Exception("Error getting result"));
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

            logger.debug("GoPay_Controller:: onDemandPayment: Status: {}, Result: {}", response.getStatus(), result);

            switch (response.getStatus()) {

                case 200: {

                    // Binding Json with help object
                    final Form<GoPay_Result> form = Form.form(GoPay_Result.class).bind(result);
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

    public static void terminateOnDemand(Model_Product product) throws Exception{

        // Token
        logger.debug("GoPay_Controller:: terminateOnDemand: Asking for token");
        String local_token = getToken();

        if(local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + product.gopay_id + "/void-recurrence")
                        .setContentType("application/x-www-form-urlencoded")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + local_token)
                        .setMethod("POST")
                        .setRequestTimeout(10000)
                        .post("");

                logger.debug("GoPay_Controller:: terminateOnDemand: Sending request to terminate on demand payment!");

                response = responsePromise.get(10000);

            } catch (Exception e) {
                Loggy.internalServerError("GoPay_Controller:: terminateOnDemand: ", new Exception("Error getting result"));
                continue;
            }

            JsonNode result = response.asJson();

            if (response.getStatus() == 200 && result.has("result") && result.get("result").asText().equals("FINISHED")) {

                product.archiveEvent("Terminate on demand payment", "GoPay on demand payment number: " + product.gopay_id + " was terminated", null);
                // TODO notifikace

                return;

            } else {

                product.archiveEvent("Terminate on demand payment", "GoPay on demand payment number: " + product.gopay_id + " was terminated", null);

                throw new Exception("Cannot terminate on demand payment. Response from GoPay was: " + result.toString());
            }
        }
    }

    private static void refundPayment(Model_Invoice invoice, Long amount) throws Exception{

        // Token
        logger.debug("GoPay_Controller:: refundPayment: Asking for token");
        String local_token = getToken();

        if(local_token == null) throw new NullPointerException("Token for API in GoPay_Controller is null");

        ObjectNode json = Json.newObject();
        json.put("amount", amount / 10); // přepočet na centy

        // Odeslání žádosti o stržení platby
        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        for (int trial = 5; trial > 0; trial--) {

            WSResponse response;

            try {

                F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + invoice.gopay_id + "/refund")
                        .setContentType("application/x-www-form-urlencoded")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + local_token)
                        .setRequestTimeout(10000)
                        .post(Json.toJson(json));

                logger.debug("GoPay_Controller:: refundPayment: Sending request to refund payment!");

                response = responsePromise.get(10000);

            } catch (Exception e) {
                Loggy.internalServerError("GoPay_Controller:: refundPayment: ", new Exception("Error getting result"));
                continue;
            }

            JsonNode result = response.asJson();

            if (response.getStatus() == 200 && result.has("result") && result.get("result").asText().equals("FINISHED")) {

                invoice.getProduct().archiveEvent("Refund payment", "Refund GoPay payment number: " + invoice.gopay_id + " was requested", invoice.id);
                // TODO notifikace

                return;

            } else {

                invoice.getProduct().archiveEvent("Refund payment", " Refund for GoPay payment number: " + invoice.gopay_id + " was requested, but was not successful", invoice.id);

                // TODO notifikace
                throw new Exception("Cannot refund payment. Response from GoPay was: status " + response.getStatus() + ", body: " + result.toString());
            }
        }
    }

// PUBLIC controllers METHOD ###########################################################################################

    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result payment_refund(String invoice_id){
        try {

            // Binding Json with help object
            final Form<Swagger_Payment_Refund> form = Form.form(Swagger_Payment_Refund.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.formExcepting(form.errorsAsJson());
            Swagger_Payment_Refund help = form.get();

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice not found");

            invoice.getProduct().archiveEvent("Refund payment", "Request for refund for this reason: " + help.reason, null);

            if (help.whole) refundPayment(invoice, invoice.total_price());
            else if (help.amount != null) refundPayment(invoice, (long) (help.amount * 1000));
            else return GlobalResult.result_BadRequest("Set 'whole' parameter to true or specify amount.");

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result payment_notification(Long id){
        try {

            GoPay_PaymentCheck.addToQueue(id);

            return ok();

        } catch (Exception e){
            Loggy.internalServerError("GoPay_Controller:: payment_notification:", e);
            return ok();
        }
    }

    public Result payment_return(Long id){
        try {

            GoPay_PaymentCheck.addToQueue(id);

            return redirect(Server.becki_mainUrl);

        } catch (Exception e) {
            Loggy.internalServerError("GoPay_Controller:: payment_return:", e);
            return redirect(Server.becki_mainUrl);
        }
    }
}