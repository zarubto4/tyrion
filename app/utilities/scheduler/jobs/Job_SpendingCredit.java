package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import models.Model_Product;
import models.Model_GeneralTariffExtensions;
import models.Model_Invoice;
import models.Model_InvoiceItem;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_status;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.goPay.helps_objects.GoPay_Recurrence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_SpendingCredit implements Job {

    public Job_SpendingCredit(){}

    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_SpendingCredit:: execute: Executing Job_SpendingCredit");

        if(!spend_credit_thread.isAlive()) spend_credit_thread.start();
    }

    private Thread spend_credit_thread = new Thread(){

        @Override
        public void run() {

            logger.debug("Job_SpendingCredit:: spend_credit_thread: concurrent thread started on {}", new Date());

            Date created = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(16));

            int total =  Model_Product.find.where().lt("date_of_create", created)
                    .isNotNull("extensions.price_in_usd")
                    .findRowCount();

            if (total != 0) {

                int page_total = total / 25;

                if (total % 25 > 0) page_total++;

                for (int page = 0; page <= page_total - 1; page++) {

                    logger.debug("Job_SpendingCredit:: spend_credit_thread: procedure for page {} from {}", (page + 1), page_total);

                    /*
                     * Filter slouží k hledání těch produktů, kde by mělo dojít ke stržení kreditu
                     * Kredit se strhává u všech produktů, starších než 16 hodin (Aby někdo před půlnocí nezaložil produkt a hned se mu nestrhla raketa)
                     *
                     */
                    List<Model_Product> products = Model_Product.find.where()
                            .isNotNull("extensions.price_in_usd")
                            .lt("date_of_create", created)
                            .order("date_of_create")
                            .findPagedList(page, 25)
                            .getList();

                    products.forEach(Job_SpendingCredit::spending_credit);
                }
            }

            logger.debug("Job_SpendingCredit:: spend_credit_thread: thread stopped on {}", new Date());
        }
    };

    /**
     * Vezmou se všechny připojené Extensions sečte se jejich cena a odečte se z účtu. Pak se IFem porovná co sá má dít dál.
     */
    private static void spending_credit(Model_Product product){

        logger.info("Job_SpendingCredit:: spending_credit: Product ID: ", product.id );
        double total_spending = 0.0;

        for(Model_GeneralTariffExtensions extension : product.extensions){
                total_spending += extension.price_in_usd;
        }

        logger.debug("Job_SpendingCredit:: spending_credit: Product ID: {} total spending: {}", product.id, total_spending);
        logger.debug("Job_SpendingCredit:: spending_credit: Product ID: " + product.id + " state before: " + product.remaining_credit );
        product.remaining_credit -= total_spending;
        product.update();

        logger.debug("Job_SpendingCredit:: spending_credit: Product ID: " + product.id + " actual state: " + product.remaining_credit );


        // Režim bankovního převodu - vše musí být ve výrazném přehstihnu
        if(product.method == Enum_Payment_method.bank_transfer) {

            logger.debug("Job_SpendingCredit:: spending_credit: Product ID:: " +product.id + " bank transfer");

            if(product.remaining_credit < 0 && ((-product.remaining_credit)*20 > total_spending) ){
                logger.warn("Job_SpendingCredit:: spending_credit: Product ID:: bank transfer::" +product.id + " The account is in the minus 20 times the average spending");
                // Pošlu notifikaci a Email

                // Zablokuji účet
                return;
            }

            if(product.remaining_credit < 0){
                logger.warn("Job_SpendingCredit:: spending_credit: Product ID:: bank transfer:: " +product.id + " The account is in the minus");
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


        }else if(product.method == Enum_Payment_method.credit_card){

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

            if(product.remaining_credit < 5 * total_spending && product.invoices().size() > 0 && product.invoices().get(0).status == Enum_Payment_status.created_waited){

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
            invoice_item_1.unit_name = "Currency";
            invoice_item_1.currency = Enum_Currency.USD;

            invoice.invoice_items.add(invoice_item_1);
            invoice.proforma = true;
            invoice.status = Enum_Payment_status.sent;
            invoice.date_of_create = new Date();
            invoice.method = product.method;

            product.invoices.add(invoice);
            product.update();


            logger.debug("Creating Invoice on Fakturoid");
            Utilities_Fakturoid_Controller.create_proforma(product, invoice);


            logger.debug("Creating GoPay_Recurrence");
            GoPay_Recurrence recurrence = new GoPay_Recurrence();
            recurrence.amount = Math.round(product.general_tariff.price_in_usd*100);
            recurrence.currency = Enum_Currency.USD;
            recurrence.setItems(invoice.invoice_items);
            recurrence.order_number  = invoice.invoice_number;
            recurrence.order_description =  "Services for " + monthNames_en[ cal.get(Calendar.MONTH) ];

            // Token
            logger.debug("Taking Token");
            String local_token = Utilities_GoPay_Controller.getToken();

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
                if( !Utilities_Fakturoid_Controller.fakturoid_delete("/invoices/"+  invoice.facturoid_invoice_id +  ".json") )  logger.error("Error Removing proforma from Fakturoid");

                // Vytvořit fakturu
                logger.debug("Creating invoice from proforma in Fakturoid");
                Utilities_Fakturoid_Controller.create_paid_invoice(product,invoice);

                // Uhradit Fakturu
                logger.debug("Changing state on Invoice to paid");
                if(! Utilities_Fakturoid_Controller.fakturoid_post("/invoices/"+  invoice.facturoid_invoice_id +  "/fire.json?event=pay")) logger.error("Faktura nebyla změněna na uhrazenou dojde tedy k inkonzistenntímu stavu");
                invoice.proforma = false;
                invoice.update();

                Utilities_Fakturoid_Controller.send_Invoice_to_Email(invoice);

            }
            else if(response.getStatus() == 200) {

                logger.warn("Not enough money on Account");
                logger.warn("Set a time limit protection for account");
                logger.warn("Sending email with Proforma and with request for MONEY!!!! MONEY!!! ");

                Utilities_Fakturoid_Controller.send_UnPaidInvoice_to_Email(invoice);
            }
            else{
                logger.error("Unknown Error");
                logger.error("Request status: " + response.getStatus() );
                logger.error("Request Body: "   + response.getBody() );
            }

        }
    }
}