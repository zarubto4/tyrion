package utilities.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import models.Model_Product;
import models.Model_Invoice;
import models.Model_PaymentDetails;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_mode;
import utilities.enums.Enum_Recurrence_cycle;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.helps_objects.GoPay_Contact;
import utilities.goPay.helps_objects.GoPay_Payer;
import utilities.goPay.helps_objects.GoPay_Payment;
import utilities.goPay.helps_objects.Recurrence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck", hidden = true)
public class Utilities_GoPay_Controller extends Controller {

     // Loger
     static play.Logger.ALogger logger = play.Logger.of("Loggy");


// PRIVATE METHOTD #####################################################################################################

    public static JsonNode provide_payment(String payment_description , Model_Product product, Model_Invoice invoice){

            //Rozhodnutí jestli jendnorázové nebo měsíční!

            logger.debug("Providing new Payment");
            GoPay_Payment payment = new GoPay_Payment();

            payment.setItems(invoice.invoice_items);
            payment.order_number = invoice.invoice_number;
            payment.currency = Enum_Currency.CZK;
            payment.order_description = payment_description;

            GoPay_Payer payer = new GoPay_Payer();


                // Pouze
                if(!product.on_demand) {
                    if (product.mode.name().equals(Enum_Payment_mode.monthly.name())) {
                        payment.recurrence = new Recurrence();

                        payment.recurrence.recurrence_cycle = Enum_Recurrence_cycle.ON_DEMAND;



                        Calendar cal = Calendar.getInstance();
                        product.monthly_day_period = (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7) > 28 ? 28 : (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7);
                        product.on_demand = true;
                        product.update();
                    } else if (product.mode.name().equals(Enum_Payment_mode.annual.name())) {
                        payment.recurrence = new Recurrence();
                        payment.recurrence.recurrence_cycle = Enum_Recurrence_cycle.ON_DEMAND;
                        product.update();
                    }
                }

            List<GoPay_Payer.PaymentInstrument> paymentInstruments = new ArrayList<>();
                paymentInstruments.add(GoPay_Payer.PaymentInstrument.PAYMENT_CARD);
                paymentInstruments.add(GoPay_Payer.PaymentInstrument.PAYSAFECARD);
                paymentInstruments.add(GoPay_Payer.PaymentInstrument.PAYPAL);
            payer.allowed_payment_instruments.addAll(paymentInstruments);
            payer.payment_instrument         =  GoPay_Payer.PaymentInstrument.PAYMENT_CARD;
            payer.default_payment_instrument = GoPay_Payer.PaymentInstrument.PAYMENT_CARD;


            Model_PaymentDetails details = product.payment_details;

            GoPay_Contact payerContact = new GoPay_Contact();
                payerContact.first_name = details.person.full_name;
                payerContact.email = details.invoice_email;

                if(details.company_account) {
                    payerContact.phone_number = details.company_authorized_phone;
                }

                payerContact.street         = details.street + " " +details.street_number;
                payerContact.postal_code    = details.zip_code;
                payerContact.country_code   = details.country;
                payerContact.city           = details.city;


                payer.contact               = payerContact;
                payment.payer               = payer;
                payment.lang                = GoPay_Payer.Lang.EN;


            String local_token = getToken();

            if(local_token == null) {
                logger.error("Token for API in GoPay_Controller in Provide_payment is null");
                throw new NullPointerException("Token for API in GoPay_Controller in Provide_payment is null");
            }

            logger.debug("Sending Request for new Payment to GoPay with object: " + Json.toJson(payment).toString());

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
            F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url +  "/payments/payment")
                    .setContentType("application/json")
                    .setHeader("Accept", "application/json")
                    .setHeader("Authorization" , "Bearer " + local_token)
                    .setRequestTimeout(2500)
                    .post(Json.toJson(payment));

            JsonNode response = responsePromise.get(1000).asJson();
            logger.debug("Response from GoPay: " + response.toString());

            return response;
        }


// PUBLIC controllers METHOTD #####################################################################################################

    public Result call_back_Notification(Long id){


        System.out.println("Dostala se mi sem notifikace");

            return new Todo();
        }

    public Result call_back_Return_Url(Long gopay_id){

            logger.debug("Return URL from GoPay on " + gopay_id +" gopay_id");

            Model_Invoice invoice = Model_Invoice.find.where().eq("gopay_id", gopay_id).findUnique();

            if(invoice == null){
                logger.error("Invoice not exist after payment - redirect to fail page");
                return ok();
            }

            if(!invoice.proforma){
                logger.warn("Invoice is already complete");
                return redirect(Server.becki_mainUrl + "/financial/"+ invoice.getProduct().id + "/paid/success/" + invoice.id);
            }

            Model_Product product = Model_Product.get_byInvoice(invoice.id);

            // Smazat proformu
            logger.debug("Removing proforma from Fakturoid");
            if( !Utilities_Fakturoid_Controller.fakturoid_delete("/invoices/"+  invoice.facturoid_invoice_id +  ".json") )  logger.error("Error Removing proforma from Fakturoid");

            // Vytvořit fakturu
            logger.debug("Creating invoice from proforma in Fakturoid");
            Utilities_Fakturoid_Controller.create_paid_invoice(product,invoice);

            // Uhradit Fakturu
            logger.debug("Changing state on Invoice to paid");
            if(! Utilities_Fakturoid_Controller.fakturoid_post("/invoices/"+  invoice.facturoid_invoice_id +  "/fire.json?event=pay")) logger.error("Faktura nebyla změněna na uhrazenou dojde tedy k inkonzistenntímu stavu");
            invoice.proforma = false;
            invoice.update();

            // úspěšně zaplacený? Není ověřeno!!!
            product.active = true;
            product.remaining_credit += invoice.total_price();
            product.update();

            return redirect(Server.becki_mainUrl + "/paid/success/" + gopay_id);
        }

// PRIVATE METHOTD services #####################################################################################################

    public static String token;     // GoPay Connector - hash token sloužící k volání API k bráně GoPay
    public static Date last_refresh;  // 4as, který hodnotí obnovu bezpečnostního tokenu - jeho živostnost je totiž 30 minut a šetří se tím dotazy!

    // Získání Tokenu od goPay - Ten se přidává do všech hlaviček
    public static String getToken(){
        try {

            logger.debug("Asking for GoPay Token");
            if( token != null && last_refresh != null &&( new Date().getTime() - last_refresh.getTime() >= 28*60*1000) ) {
                logger.debug("Return Token");
                return token;
            }

            logger.debug("GoPay Token expedite or not yet registred");
            last_refresh = new Date();


            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/oauth2/token")
                    .setContentType("application/x-www-form-urlencoded")
                    .setAuth(Server.GoPay_client_id, Server.GoPay_client_secret)
                    .setRequestTimeout(5000)
                    .setBody("grant_type=client_credentials&scope=payment-create")
                    .post("grant_type=client_credentials&scope=payment-create");


            JsonNode result = responsePromise.get(5000).asJson();

            logger.debug("Result for token:" + result.toString() );

            if(result.has("access_token")){
                token = result.get("access_token").asText();
                return token;

            }else {
                logger.error("incoming Json in GoPay GetToken not contains access_token!");
                logger.error(responsePromise.get(5000).toString());
                token = null;
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

}
