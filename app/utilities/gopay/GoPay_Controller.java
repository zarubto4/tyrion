package utilities.gopay;

import com.google.inject.Inject;
import cz.gopay.api.v3.GPClientException;
import cz.gopay.api.v3.IGPConnector;
import cz.gopay.api.v3.impl.apacheclient.HttpClientGPConnector;
import cz.gopay.api.v3.model.common.Currency;
import cz.gopay.api.v3.model.payment.*;
import cz.gopay.api.v3.model.payment.support.Payer;
import cz.gopay.api.v3.model.payment.support.PayerContact;
import cz.gopay.api.v3.model.payment.support.PaymentInstrument;
import models.project.global.financial.Invoice;
import models.project.global.financial.Invoice_item;
import models.project.global.financial.Payment_Details;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;

import java.util.ArrayList;
import java.util.List;

public class GoPay_Controller  extends Controller {

        // Rest Api call client
        @Inject WSClient ws;

        // Loger
        static play.Logger.ALogger logger = play.Logger.of("Loggy");

        // GoPay Connector
        private static IGPConnector connector = null;

        public static void set_token_thread(){

                // Toto vlákno má nastarosti obnovu komunikačního tokenu jelikož je jeho platnost pouze 30 minut,
                // toto obnovování je méně náročné, než si token získat kdykoliv to je potřeba pro zadání nové platby
                Thread GoPayToken_Thread = new Thread() {

                    @Override
                    public void run() {

                        while(true){
                            try {

                                connector = HttpClientGPConnector.build(Server.GoPay_api_url);
                                connector.getAppToken(Server.GoPay_client_id, Server.GoPay_client_credentials);

                                logger.debug("Thread for GoPay token is going to sleep for next 28 minutes");
                                sleep(1000*60*28); // 29 minut

                            }catch (Exception e){
                                e.printStackTrace();
                                connector = null;
                            }
                        }
                    }
                };

            GoPayToken_Thread.start();
        }


        public static Payment provide_payment(Currency currency, String payment_description, Invoice invoice) throws GPClientException{

            Long amount = (long) 0;

            BasePaymentBuilder payment_builder = PaymentFactory.createBasePaymentBuilder();

            for(Invoice_item item : invoice.invoice_items){
                amount += item.unit_price;
                payment_builder.addItem(item.name, item.unit_price, (long) 0 , item.quantity);
            }

            payment_builder.order(invoice.invoice_number, amount , currency , payment_description);
            payment_builder.withCallback( Server.GoPay_successfullUrl, Server.GoPay_failedUrl, Server.GoPay_returnUrl, Server.GoPay_notificationUrl );


            Payer payer = new Payer();
                List<PaymentInstrument> paymentInstruments = new ArrayList<>();
                    paymentInstruments.add(PaymentInstrument.PAYMENT_CARD);
                    paymentInstruments.add(PaymentInstrument.PAYSAFECARD);
                    paymentInstruments.add(PaymentInstrument.PAYPAL);
                payer.setAllowedPaymentInstruments(paymentInstruments);


                Payment_Details details = Payment_Details.find.where().eq("product.payment_records.invoice.id", invoice.id).findUnique();

                PayerContact payerContact = new PayerContact();

                    payerContact.setFirstName(details.person.full_name);

                    if(details.company_account) {
                       payerContact.setEmail(details.company_invoice_email);
                       payerContact.setPhoneNumber(details.company_authorized_phone);
                    }

                    payerContact.setStreet(details.street + " " + details.street_number);
                    payerContact.setPostalCode(details.zip_code);
                    payerContact.setCountryCode(details.country);
                    payerContact.setCity(details.city);
                payer.setContact(payerContact);

            payment_builder.payer(payer);
            payment_builder.inLang(Lang.EN);
            payment_builder.toEshop(Server.GoPay_go_id);
            payment_builder.addAdditionalParameter("invoice_number", "");

            BasePayment payment = payment_builder.build();

            Payment result = connector.createPayment(payment);
            return result;

        }


        public Result call_back_from_GoPay(){


        return ok();
        }

}
