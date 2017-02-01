package utilities.schedules_activities;

import com.fasterxml.jackson.databind.JsonNode;
import models.project.global.Model_Product;
import models.project.global.financial.Model_GeneralTariffExtensions;
import models.project.global.financial.Model_Invoice;
import models.project.global.financial.Model_InvoiceItem;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Currency;
import utilities.enums.Payment_method;
import utilities.enums.Payment_status;
import utilities.fakturoid.Fakturoid_Controller;
import utilities.goPay.GoPay_Controller;
import utilities.goPay.helps_objects.GoPay_Recurrence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Spending_Credit_Every_Day implements Job {


    public Spending_Credit_Every_Day(){ /** do nothing */ }
    static play.Logger.ALogger logger = play.Logger.of("CRON-Spending_Credit_Every_Day");


    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Spending_Credit_Every_Day:: Starting with CRON procedure");
        logger.info("Spending_Credit_Every_Day:: Time" + new Date().toString() );

        filter_do();


    }


    /**
     * Filter slouží k hledání těch produktů, kde by mělo dojít ke stržení kreditu
     * Kredit se strhává u všech produktů, starších než 16 hodin (Aby někdo před půlnocí nezaložil produkt a hned se mu nestrhla raketa) 
     * 
     */
    public void filter_do(){

        logger.info("Spending_Credit_Every_Day:: filter_do");
        Calendar now = Calendar.getInstance();
        long time_16_hours_back = now.getTimeInMillis() - 16*60*1000;

        logger.info("Time" + new Date().toString() );



        int page_actual = 0;
        int all_page =  Model_Product.find.where().lt("date_of_create", time_16_hours_back)
                            .disjunction()
                                .isNotNull("extensions.price_in_usd")
                            .findRowCount();

        while (true){

            logger.info("Spending_Credit_Every_Day:: procedure fo page " + page_actual + " from " + all_page);

           List<Model_Product> products = Model_Product.find.where()
                                                .disjunction()
                                                    .isNotNull("extensions.price_in_usd")
                                                .endJunction()
                                                .lt("date_of_create", time_16_hours_back)
                                                .order("date_of_create")
                                                .findPagedList(page_actual,25)
                                                .getList();

            for(Model_Product product: products){
                 this.spending_credit(product);
            }

        }
        
    }

    /**
     * Vezmou se všechny připojené Extensions sečte se jejich cena a odečte se z účtu. Pak se IFem porovná co sá má dít dál.
     */
    public void spending_credit(Model_Product product){

        logger.info("Spending_Credit_Every_Day:: Product ID: " +product.id );
        double total_spending = 0.0;

        for(Model_GeneralTariffExtensions extension : product.extensions){
                total_spending += extension.price_in_usd;
        }

        logger.debug("Spending_Credit_Every_Day:: Product ID: " +product.id + " total spending: " + total_spending );
        logger.debug("Spending_Credit_Every_Day:: Product ID: " +product.id + " state before: " + product.remaining_credit );
        product.remaining_credit -= total_spending;
        product.update();

        logger.debug("Spending_Credit_Every_Day:: Product ID: " +product.id + " actual state: " + product.remaining_credit );


        // Režim bankovního převodu - vše musí být ve výrazném přehstihnu
        if(product.method == Payment_method.bank_transfer) {

            logger.debug("Spending_Credit_Every_Day:: Product ID:: " +product.id + " bank transfer");

            if(product.remaining_credit < 0 && ((-product.remaining_credit)*20 > total_spending) ){
                logger.warn("Spending_Credit_Every_Day:: Product ID:: bank transfer::" +product.id + " The account is in the minus 20 times the average spending");
                // Pošlu notifikaci a Email

                // Zablokuji účet
                return;
            }

            if(product.remaining_credit < 0){
                logger.warn("Spending_Credit_Every_Day:: Product ID:: bank transfer:: " +product.id + " The account is in the minus");
                // Pošlu notifikaci a Email

                return;
            }

            if(product.remaining_credit < 10 * total_spending){
                logger.warn("Spending_Credit_Every_Day:: Product ID::  bank transfer:: " +product.id + " The Product is close to zero in financial balance");
                // Pošlu notifikaci a Email

                return;
            }


            // Pokud mi zbývá kreditu na méně než 14 dní - vytvořím fakturu
            if (product.remaining_credit < 14 * total_spending) {

                logger.warn("Spending_Credit_Every_Day:: Product ID::  bank transfer:: " +product.id + " It is time to send an invoice");
                // Vytvořím zálohovou fakturu

                // Pošlu notifikaci

                // Pošlu email

                return;
            }

            logger.warn("Spending_Credit_Every_Day:: Product ID::  bank transfer:: " +product.id + " The financial reserves are sufficient. Just send a notification");


        }else if(product.method == Payment_method.credit_card){

            logger.debug("Spending_Credit_Every_Day:: Product ID: " +product.id + " credit card");

            if(product.remaining_credit < 0 && ((-product.remaining_credit)*10 > total_spending) ){
                logger.warn("Spending_Credit_Every_Day:: Product ID:: credit card::" +product.id + " The account is in the minus 10 times the average spending");
                // Pošlu notifikaci a Email

                // Zablokuji účet
                return;
            }

            if(product.remaining_credit < 0){
                logger.warn("Spending_Credit_Every_Day:: Product ID:: credit card:: " +product.id + " The account is in the minus");
                // Pošlu notifikaci a Email

                return;
            }

            if(product.remaining_credit < 5 * total_spending && product.invoices().size() > 0 && product.invoices().get(0).status == Payment_status.created_waited){

                logger.warn("Spending_Credit_Every_Day:: Product ID:: credit card::" +product.id + " Close to zero. Invoice created - but failed to pay the credit card before");

                // pošlu email

                // pošlu notifikaci

                return;
            }

            if(product.remaining_credit < 5 * total_spending){

                logger.warn("Spending_Credit_Every_Day:: Product ID:: credit card::" +product.id + " The Product is close to zero in financial balance");

                // Vytvořím zálohovou fakturu

                // Strhnu prachy

                // Pokud se povede

                // Pokud se nepovede

                // Pošlu notifikaci

                // Pošlu email

                return;
            }

        }

    }
    
    
    public void do_on_Demand_payment(){
        logger.debug("Starting with procedure ON_DEMAND - taking money from Credit-Card");

        Calendar cal = Calendar.getInstance();
        List<Model_Product> products_with_on_Demands = Model_Product.find.where().eq("on_demand_active", true).where().eq("monthly_day_period", (cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) + cal.get(Calendar.WEEK_OF_MONTH)*7)  ).findList();

        logger.debug("Founded " + products_with_on_Demands.size() + " procedures with 4 days to end of Account");


        // String[] monthNames_cz = {"Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"};
        String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


        for(Model_Product product : products_with_on_Demands){

            logger.debug("Updating procedure on user product " + product.product_individual_name + " id " + product.id);

            logger.debug("Creating Invoice");

            // Vytovřím fakturu
            Model_Invoice invoice = new Model_Invoice();


            logger.debug("Creating Invoice in Database");
            Model_InvoiceItem invoice_item_1 = new Model_InvoiceItem();

            invoice_item_1.name = "Services for " + monthNames_en[ cal.get(Calendar.MONTH) ];
            invoice_item_1.unit_price = product.general_tariff.price_in_usd;
            invoice_item_1.quantity = (long) 1;
            invoice_item_1.unit_name = "Service";
            invoice_item_1.currency = Currency.USD;

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
            recurrence.amount = Math.round(product.general_tariff.price_in_usd*100);
            recurrence.currency = Currency.USD;
            recurrence.setItems(invoice.invoice_items);
            recurrence.order_number  = invoice.invoice_number;
            recurrence.order_description =  "Services for " + monthNames_en[ cal.get(Calendar.MONTH) ];

            // Token
            logger.debug("Taking Token");
            String local_token = GoPay_Controller.getToken();

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





}
