package controllers;


import io.swagger.annotations.Api;
import models.Model_Invoice;
import models.Model_InvoiceItem;
import models.Model_Product;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_method;
import utilities.fakturoid.Utilities_Fakturoid_Controller;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

     public Result test1(){
         try {

             Model_Product product = Model_Product.find.all().get(0);

             Model_Invoice invoice = new Model_Invoice();
             invoice.product = product;
             invoice.method = Enum_Payment_method.bank_transfer;

             Model_InvoiceItem invoice_item = new Model_InvoiceItem();
             invoice_item.name = "TEST TEST";
             invoice_item.unit_price = 100.0;
             invoice_item.quantity = (long) 1;
             invoice_item.unit_name = "Currency";
             invoice_item.currency = Enum_Currency.USD;

             invoice.invoice_items.add(invoice_item);

             invoice = Utilities_Fakturoid_Controller.create_proforma(invoice);
             if (invoice == null) return badRequest("Failed to make an invoice, check your provided payment information");

             Utilities_Fakturoid_Controller.fakturoid_post("/invoices/" + invoice.fakturoid_id + "/fire.json?event=pay_proforma");

             Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, "tyls.alexandr@gmail.com");

             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }





}
