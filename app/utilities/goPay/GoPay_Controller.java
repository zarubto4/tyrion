package utilities.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import models.project.global.Product;
import models.project.global.financial.Invoice;
import models.project.global.financial.Invoice_item;
import models.project.global.financial.Payment_Details;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.enums.Payment_mode;
import utilities.enums.Payment_status;
import utilities.enums.Recurrence_cycle;
import utilities.fakturoid.Fakturoid_Controller;
import utilities.goPay.helps_objects.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GoPay_Controller  extends Controller {

     // Loger
     static play.Logger.ALogger logger = play.Logger.of("Loggy");


// PRIVATE METHOTD #####################################################################################################

    public static JsonNode provide_payment(String payment_description ,Product product,  Invoice invoice){

            //Rozhodnutí jestli jendnorázové nebo měsíční!

            logger.debug("Providing new Payment");
            GoPay_Payment payment = new GoPay_Payment();

            payment.setItems(invoice.invoice_items);
            payment.order_number = invoice.invoice_number;
            payment.currency = product.currency;
            payment.order_description = payment_description;

            GoPay_Payer payer = new GoPay_Payer();


                // Pouze
                if(!product.on_demand_active) {
                    if (product.mode.name().equals(Payment_mode.monthly.name())) {
                        payment.recurrence = new Recurrence();

                        payment.recurrence.recurrence_cycle = Recurrence_cycle.ON_DEMAND;



                        Calendar cal = Calendar.getInstance();
                        product.monthly_day_period = (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7) > 28 ? 28 : (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7);
                        product.on_demand_active = true;

                            // Nastavím čas, kdy dojde k vypršení služby
                            cal.setTime(new Date());
                            cal.add(Calendar.MONTH, 1);
                            product.paid_until_the_day = cal.getTime() ;

                        product.update();
                    } else if (product.mode.name().equals(Payment_mode.annual.name())) {
                        payment.recurrence = new Recurrence();
                        payment.recurrence.recurrence_cycle = Recurrence_cycle.ON_DEMAND;

                        Calendar cal = Calendar.getInstance();
                        product.monthly_day_period = (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7) > 28 ? 28 : (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7);
                        product.on_demand_active = true;


                            // Nastavím čas, kdy dojde k vypršení služby
                            cal.setTime(new Date());
                            cal.add(Calendar.YEAR, 1);
                            product.paid_until_the_day = cal.getTime() ;

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


            Payment_Details details = product.payment_details;

            GoPay_Contact payerContact = new GoPay_Contact();
                payerContact.first_name = details.person.full_name;

                if(details.company_account) {
                    payerContact.email = details.company_invoice_email;
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

// AUTOMATICKY KAŽDÉ RÁNO VYVOLANÉ PLATBY v režimu ON_DEMAND

    public static void do_on_Demand_payment(){

        logger.debug("Starting with procedure ON_DEMAND - taking money from Credit-Card");

        Calendar cal = Calendar.getInstance();
        List<Product> products_with_on_Demands = Product.find.where().eq("on_demand_active", true).where().eq("monthly_day_period", (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7)  ).findList();

        logger.debug("Founded " + products_with_on_Demands.size() + " procedures with 4 days to end of Account");


       // String[] monthNames_cz = {"Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"};
        String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


        for(Product product : products_with_on_Demands){

            logger.debug("Updating procedure on user product " + product.product_individual_name + " id " + product.id);

            logger.debug("Creating Invoice");

            // Vytovřím fakturu
            Invoice invoice = new Invoice();


            logger.debug("Creating Invoice in Database");
            Invoice_item invoice_item_1 = new Invoice_item();

                invoice_item_1.name = "Services for " + monthNames_en[ cal.get(Calendar.MONTH) ];
                invoice_item_1.unit_price = product.get_price_general_fee();
                invoice_item_1.quantity = (long) 1;
            invoice_item_1.unit_name = "Service";

            invoice.invoice_items.add(invoice_item_1);
            invoice.proforma = true;
            invoice.status = Payment_status.sent;
            invoice.date_of_create = new Date();
            invoice.method = product.method;

            product.invoices.add(invoice);
            product.update();


            logger.debug("Creating Invoice on Fakturoid");
            Fakturoid_Controller.create_proforma(product, invoice);


            logger.debug("Creating GoPay_Recurrence");
            GoPay_Recurrence recurrence = new GoPay_Recurrence();
                recurrence.amount = Math.round(product.get_all_monthly_fees()*100);
                recurrence.currency = product.currency;
                recurrence.setItems(invoice.invoice_items);
                recurrence.order_number  = invoice.invoice_number;
                recurrence.order_description =  "Services for " + monthNames_en[ cal.get(Calendar.MONTH) ];

            // Token
            logger.debug("Taking Token");
            String local_token = getToken();

            if(local_token == null) {
                logger.error("Local Token is null!!!");
                break;
            }


            // Odeslánížádosti o stržení platby
            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
            F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url +  "/payments/payment/" + product.gopay_id + "/create-recurrence")
                    .setContentType("application/json")
                    .setHeader("Accept", "application/json")
                    .setHeader("Authorization" , "Bearer " + local_token)
                    .setRequestTimeout(2500)
                    .post(Json.toJson(recurrence));

            logger.debug("Sending request for new payment!");
            WSResponse response = responsePromise.get(1000);

            /**
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

            // Platba byla úspěšná
            if(response.getStatus() ==  500 ) {

                JsonNode json_response =  response.asJson();
                logger.debug("Request was successful");

                invoice.gopay_id = json_response.get("parent_id").asLong();
                invoice.gopay_order_number = json_response.get("order_number").asText();

                logger.debug("Removing proforma from Fakturoid");
                if( !Fakturoid_Controller.fakturoid_delete("/invoices/"+  invoice.facturoid_invoice_id +  ".json") )  logger.error("Error Removing proforma from Fakturoid");

                // Vytvořit fakturu
                logger.debug("Creating invoice from proforma in Fakturoid");
                Fakturoid_Controller.create_paid_invoice(product,invoice);

                // Uhradit Fakturu
                logger.debug("Changing state on Invoice to paid");
                if(! Fakturoid_Controller.fakturoid_post("/invoices/"+  invoice.facturoid_invoice_id +  "/fire.json?event=pay")) logger.error("Faktura nebyla změněna na uhrazenou dojde tedy k inkonzistenntímu stavu");
                invoice.proforma = false;
                invoice.update();

                Fakturoid_Controller.send_Invoice_to_Email(invoice);

            }
            else if(response.getStatus() == 200) {

                logger.warn("Not enough money on Account");
                logger.warn("Set a time limit protection for account");
                logger.warn("Sending email with Proforma and with request for MONEY!!!! MONEY!!! ");

                Fakturoid_Controller.send_UnPaidInvoice_to_Email(invoice);
            }
            else{
                logger.error("Unknown Error");
                logger.error("Request status: " + response.getStatus() );
                logger.error("Request Body: "   + response.getBody() );
            }

        }
    }

// PUBLIC controllers METHOTD #####################################################################################################

    public Result call_back_Notification(Long id){


        System.out.println("Dostala se mi sem notifikace");

            return new Todo();
        }

    public Result call_back_Return_Url(Long gopay_id){

            logger.debug("Return URL from GoPay on " + gopay_id +" gopay_id");

            Invoice invoice = Invoice.find.where().eq("gopay_id", gopay_id).findUnique();

            if(invoice == null){
                logger.error("Invoice not exist after payment - redirect to fail page");
                return ok();
            }

            if(!invoice.proforma){
                logger.warn("Invoice is already complete");
                return redirect("localhost:8890/paid/success/" + gopay_id);
            }

             Product product = Product.find.where().eq("invoices.id", invoice.id).findUnique();

            // Smazat proformu
            logger.debug("Removing proforma from Fakturoid");
            if( !Fakturoid_Controller.fakturoid_delete("/invoices/"+  invoice.facturoid_invoice_id +  ".json") )  logger.error("Error Removing proforma from Fakturoid");


            // Vytvořit fakturu
            logger.debug("Creating invoice from proforma in Fakturoid");
            Fakturoid_Controller.create_paid_invoice(product,invoice);


            // Uhradit Fakturu
            logger.debug("Changing state on Invoice to paid");
            if(! Fakturoid_Controller.fakturoid_post("/invoices/"+  invoice.facturoid_invoice_id +  "/fire.json?event=pay")) logger.error("Faktura nebyla změněna na uhrazenou dojde tedy k inkonzistenntímu stavu");
            invoice.proforma = false;
            invoice.update();


            // úspěšně zaplacený? Není ověřeno!!!
            product.active = true;
            product.update();

            return redirect("localhost:8890/paid/success/" + gopay_id);
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
