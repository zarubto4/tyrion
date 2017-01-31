package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.person.Model_Person;
import models.project.global.Model_Product;
import models.project.global.financial.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Currency;
import utilities.enums.Payment_method;
import utilities.enums.Payment_mode;
import utilities.enums.Payment_status;
import utilities.fakturoid.Fakturoid_Controller;
import utilities.goPay.GoPay_Controller;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_BadRequest;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagged_Applicable_Product;
import utilities.swagger.outboundClass.Swagger_GoPay_Url;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Finance extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    // ADMIN - GENERAL PRODUCT TARIFF SETTINGS ##########################################################################

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_create(){
        try {
            final Form<Swagger_Tariff_General_Create> form = Form.form(Swagger_Tariff_General_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Create help = form.get();

            Model_GeneralTariff general_tariff = new Model_GeneralTariff();

            general_tariff.tariff_name              = help.tariff_name;
            general_tariff.identificator            = help.identificator;
            general_tariff.tariff_description       = help.tariff_description;

            general_tariff.color                    = help.color;

            general_tariff.required_paid_that       = help.required_paid_that;
            general_tariff.number_of_free_months    = help.number_of_free_months;

            general_tariff.company_details_required = help.company_details_required;
            general_tariff.required_payment_mode    = help.required_payment_mode;
            general_tariff.required_payment_method  = help.required_payment_method;

            general_tariff.credit_card_support      = help.credit_card_support;
            general_tariff.bank_transfer_support    = help.bank_transfer_support;

            general_tariff.mode_annually            = help.mode_annually;
            general_tariff.mode_credit              = help.mode_credit;
            general_tariff.free_tariff              = help.free_tariff;

            general_tariff.usd = help.usd;
            general_tariff.eur = help.eur;
            general_tariff.czk = help.czk;

            general_tariff.save();

            return GlobalResult.result_ok(Json.toJson(general_tariff));
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_edit(){
        try {

            final Form<Swagger_Tariff_General_Create> form = Form.form(Swagger_Tariff_General_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Create help = form.get();

            Model_GeneralTariff general_tariff = Model_GeneralTariff.find.byId(help.id);
            if(general_tariff == null) return GlobalResult.notFoundObject("GeneralTariff general_tariff_id not found");

            general_tariff.tariff_name      = help.tariff_name;
            general_tariff.identificator    = help.identificator;
            general_tariff.tariff_description = help.tariff_description;

            general_tariff.color            = help.color;

            general_tariff.required_paid_that = help.required_paid_that;

            general_tariff.company_details_required  = help.company_details_required;
            general_tariff.required_payment_mode     = help.required_payment_mode;
            general_tariff.required_payment_method   = help.required_payment_method;

            general_tariff.credit_card_support      = help.credit_card_support;
            general_tariff.bank_transfer_support    = help.bank_transfer_support;

            general_tariff.mode_annually    = help.mode_annually;
            general_tariff.mode_credit      = help.mode_credit;
            general_tariff.free_tariff      = help.free_tariff;

            general_tariff.usd = help.usd;
            general_tariff.eur = help.eur;
            general_tariff.czk = help.czk;

            general_tariff.update();

            return GlobalResult.result_ok(Json.toJson(general_tariff));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend ", hidden = true)
    public Result tariff_general_deactivate(String general_tariff_id){
        try {

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(general_tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.active = false;

            tariff.update();

            return GlobalResult.result_ok(Json.toJson(tariff));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend ", hidden = true)
    public Result tariff_general_activate(String general_tariff_id){
        try {

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(general_tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.active = true;

            tariff.update();

            return GlobalResult.result_ok(Json.toJson(tariff));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    // USER GENERAL_TARIFF LABEL #######################################################################################

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_label_tariff_create(){
        try {

            final Form<Swagger_Tariff_General_Label> form = Form.form(Swagger_Tariff_General_Label.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Label help = form.get();

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(help.id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_GeneralTariffLabel label = new Model_GeneralTariffLabel();
            label.general_tariff = tariff;
            label.description = help.description;
            label.label = help.label;
            label.icon = help.icon;
            label.save();

            return GlobalResult.result_ok(Json.toJson(label));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_label_extension_create(){
        try {

            final Form<Swagger_Tariff_General_Label> form = Form.form(Swagger_Tariff_General_Label.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Label help = form.get();

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(help.id);
            if(extensions == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_GeneralTariffLabel label = new Model_GeneralTariffLabel();
            label.extensions = extensions;
            label.description = help.description;
            label.label = help.label;
            label.icon = help.icon;
            label.save();

            return GlobalResult.result_ok(Json.toJson(label));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_label_edit(){
        try {

            final Form<Swagger_Tariff_General_Label> form = Form.form(Swagger_Tariff_General_Label.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Label help = form.get();

            Model_GeneralTariffLabel label = Model_GeneralTariffLabel.find.byId(help.id);
            if(label == null) return GlobalResult.notFoundObject("GeneralTariffLabel label not found");

            label.description = help.description;
            label.label = help.label;
            label.icon = help.icon;
            label.update();

            return GlobalResult.result_ok(Json.toJson(label));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_label_edit_up(String label_id){
        try{

            Model_GeneralTariffLabel label =  Model_GeneralTariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("Label not found");

            label.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_label_edit_down(String label_id){
        try{

            Model_GeneralTariffLabel label =  Model_GeneralTariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("Label not found");

            label.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove label from general Tariffs", hidden = true)
    public Result tariff_general_label_remove(String label_id){
        try{

            Model_GeneralTariffLabel label =  Model_GeneralTariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("Label not found");

            label.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    // USER GENERAL_TARIFF EXTENSION PACKAGES #########################################################################


    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_create(){
        try{

            final Form<Swagger_Tariff_General_Extension_Create> form = Form.form(Swagger_Tariff_General_Extension_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Extension_Create help = form.get();

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(help.id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_GeneralTariffExtensions extensions = new Model_GeneralTariffExtensions();
            extensions.general_tariff = tariff;
            extensions.description = help.description;
            extensions.name = help.name;
            extensions.color = help.color;

            extensions.czk = help.czk;
            extensions.eur = help.eur;
            extensions.usd = help.usd;


            extensions.save();

            return GlobalResult.result_ok(Json.toJson(extensions));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_edit(){
        try{

            final Form<Swagger_Tariff_General_Extension_Create> form = Form.form(Swagger_Tariff_General_Extension_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Extension_Create help = form.get();

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(help.id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");

            extensions.description = help.description;
            extensions.name = help.name;
            extensions.color = help.color;

            extensions.czk = help.czk;
            extensions.eur = help.eur;
            extensions.usd = help.usd;

            extensions.update();

            return GlobalResult.result_ok(Json.toJson(extensions));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "up label from general Tariffs", hidden = true)
    public Result tariff_general_extension_edit_up(String extension_id){
        try{

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");
            extensions.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_edit_down(String extension_id){
        try{

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");

            extensions.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_delete(String extension_id){
        try{

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");

            extensions.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_deactivate(String extension_id){
        try{

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");

            extensions.active = false;
            extensions.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_activate(String extension_id){
        try{

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);
            if(extensions == null) return GlobalResult.notFoundObject("Extensions not found");

            extensions.active = true;
            extensions.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }



    // USER PRODUCT_TARIFF #############################################################################################

    @ApiOperation(value = "get all Product Tariffs",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Tariffs - required for every else action in system. For example: Project is created under the Product tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_GeneralTariff.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_products_tariffs(){
        try{

            // Vytvořím seznam tarifu
            List<Model_GeneralTariff> general_tariffs = Model_GeneralTariff.find.where().eq("active", true).findList();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(general_tariffs));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Product under Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Its basic object. Peak of Pyramid :). This Api is used for its creation. You can get two kind of response: \n\n" +
                    "First(201):  System create new Object - Object Product \n\n" +
                    "Second(200): The product requires payment - The server creates an object, but returns the payment details - payment go_url for GoPay Terminal!",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Tariff_User_Register",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Created successfully - but payment is required by Credit Card", response =  Swagger_GoPay_Url.class),
            @ApiResponse(code = 201, message = "Created successfully - payment not required",    response =  Model_Product.class),
            @ApiResponse(code = 202, message = "Created successfully - but payment is required by Bank Transfer", response =  Model_Invoice.class),

            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result product_create(){
        try{

            logger.debug("Financial_Controller:: product_create:: Creating new product:: ");
            // Zpracování Json
            final Form<Swagger_Tariff_User_Register> form = Form.form(Swagger_Tariff_User_Register.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_User_Register help = form.get();


            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(help.tariff_id);
            if(tariff == null) return GlobalResult.result_BadRequest("Tariff identificator iD: {" + help.tariff_id  + "} not found or not supported now! Use only supported");

            logger.debug("Financial_Controller:: product_create:: On Tariff:: " +  tariff.tariff_name);

            if(Model_Product.find.where().eq("product_individual_name", help.product_individual_name).eq("payment_details.person.id", Controller_Security.getPerson().id).findRowCount() > 0) return GlobalResult.result_BadRequest("You cannot use same Product name twice!");


            Model_Product product = new Model_Product();

                if(help.currency_type.equals( Currency.EUR.name())) product.currency = Currency.EUR;
                else if(help.currency_type.equals( Currency.CZK.name())) product.currency = Currency.CZK;
                else if(help.currency_type.equals( Currency.USD.name())) product.currency = Currency.USD;
                else { return GlobalResult.result_BadRequest("currency is invalid. Use only (EUR, USD, CZK)");}

                product.general_tariff =  tariff;
                product.product_individual_name = help.product_individual_name;
                product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času

                product.mode = Payment_mode.free;
                product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();

                Model_Person person = Controller_Security.getPerson();

                Model_PaymentDetails payment_details = new Model_PaymentDetails();
                    payment_details.person = person;
                    payment_details.company_account = false;


                    payment_details.street = help.street;
                    payment_details.street_number = help.street_number;
                    payment_details.city = help.city;
                    payment_details.zip_code = help.zip_code;
                    payment_details.country = help.country;
                    payment_details.product = product;

                    if(tariff.company_details_required){

                        if((help.registration_no == null)&&(help.vat_number == null)) return GlobalResult.result_BadRequest("company_registration_no or vat_number is required with this tariff");
                        if(help.company_name == null)               return GlobalResult.result_BadRequest("company_name is required with this tariff");

                        if(help.company_authorized_email == null)   return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                        if(help.company_authorized_phone == null)   return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                        if(help.company_web == null)                return GlobalResult.result_BadRequest("company_web is required with this tariff");
                        if(help.company_invoice_email == null)      return GlobalResult.result_BadRequest("company_invoice_email is required with this tariff");

                        if(help.vat_number != null) {
                            payment_details.company_vat_number = help.vat_number;
                        }

                        if(help.registration_no != null) {
                            payment_details.company_registration_no  = help.registration_no;
                        }

                        payment_details.company_account = true;
                        payment_details.company_name             = help.company_name;
                        payment_details.company_authorized_email = help.company_authorized_email;
                        payment_details.company_authorized_phone = help.company_authorized_phone;
                        payment_details.company_web              = help.company_web;
                        payment_details.company_invoice_email    = help.company_invoice_email;
                    }

                    product.payment_details = payment_details;

                logger.debug("Financial_Controller:: product_create:: Payment details done");

                // payment_mode
                if(tariff.required_payment_mode) {

                    logger.debug("Financial_Controller:: product_create:: Payment mode Required");

                    if(help.payment_mode == null) return GlobalResult.result_BadRequest("Payment_mode is required!");

                    if(help.payment_mode.equals( Payment_mode.free.name()))              product.mode = Payment_mode.free;
                    else if(help.payment_mode.equals( Payment_mode.annual.name()))       product.mode = Payment_mode.annual;
                    else if(help.payment_mode.equals( Payment_mode.monthly.name()))      product.mode = Payment_mode.monthly;
                    else if(help.payment_mode.equals( Payment_mode.per_credit.name()))   product.mode = Payment_mode.per_credit;
                    else { return GlobalResult.result_BadRequest("payment_mode is invalid. Use only (free, monthly, annual, per_credit)");}

                }


                if(tariff.required_payment_method) {

                    logger.debug("Financial_Controller:: product_create:: Payment method Required");

                    if(help.payment_method == null) return GlobalResult.result_BadRequest("payment_method is required with this tariff");

                         if(help.payment_method.equals( Payment_method.bank_transfer.name()))  product.method = Payment_method.bank_transfer;
                    else if(help.payment_method.equals( Payment_method.credit_card.name()))    product.method = Payment_method.credit_card;
                    else if(help.payment_method.equals( Payment_method.free.name()))           product.method = Payment_method.free;
                    else { return GlobalResult.result_BadRequest("payment_method is invalid. Use only (bank_transfer, credit_card, free)");}

                }

                if(help.extensions_ids.size() > 0){
                    List<Model_GeneralTariffExtensions> list = Model_GeneralTariffExtensions.find.where().in("id", help.extensions_ids ).eq("general_tariff.id",tariff.id).findList();
                    product.extensions = list;
                }

                if(!tariff.required_paid_that) {
                    logger.debug("Financial_Controller:: product_create:: Its not required pay that!");
                    product.save();
                    return GlobalResult.created(Json.toJson(product));
                }


                logger.debug("Financial_Controller:: product_create:: Creating invoice");

                Model_Invoice invoice = new Model_Invoice();
                invoice.date_of_create = new Date();
                invoice.proforma = true;

                if(product.method == Payment_method.credit_card) {
                    invoice.status = Payment_status.sent;
                }
                if(product.method == Payment_method.bank_transfer) {
                    invoice.status = Payment_status.created_waited;
                }

                Model_InvoiceItem invoice_item_1 = new Model_InvoiceItem();
                invoice_item_1.name = product.general_tariff.tariff_name + " in Mode(" + product.mode.name() + ")";
                invoice_item_1.unit_price = product.get_price_general_fee();
                invoice_item_1.quantity = (long) 1;
                invoice_item_1.unit_name = "Service";
                invoice_item_1.currency = product.currency;

                invoice.invoice_items.add(invoice_item_1);
                invoice.method = product.method;

                invoice.product = product;

                logger.debug("Financial_Controller:: product_create:: Saving invoice");
                invoice.save();

                Model_Invoice test = Model_Invoice.find.byId(invoice.id);
                logger.error("Financial_Controller:: product_create::  test: " + Json.toJson(test));


                logger.debug("Financial_Controller:: product_create::  Creating Proforma in fakturoid");
                Fakturoid_Controller.create_proforma(product, invoice);
                logger.debug("Financial_Controller:: product_create::  Proforma done");


                if(product.method == Payment_method.credit_card){

                    logger.debug("Financial_Controller:: product_create::  User want pay it with credit card!");

                    logger.debug("Financial_Controller:: product_create::  Preparing for providing payment");
                    JsonNode result = GoPay_Controller.provide_payment("First Payment", product, invoice);


                    if(result.has("id")){

                        logger.debug("Set GoPay ID to Invoice");

                        invoice.refresh();
                        invoice.gopay_id = result.get("id").asLong();
                        invoice.gopay_order_number = result.get("order_number").asText();
                        invoice.update();

                        if(product.on_demand_active) {

                            logger.debug("Set GoPay ID to Product because Product has ON_DEMAND - TRUE");

                            product.gopay_id = invoice.gopay_id;
                            product.update();
                        }

                    }else {
                        logger.error("Result from GoPay not contains id for invoice!");
                    }

                    Swagger_GoPay_Url swagger_goPay_url = new Swagger_GoPay_Url();
                    swagger_goPay_url.gw_url = result.get("gw_url").asText();

                    return GlobalResult.result_ok(Json.toJson(swagger_goPay_url));

                }else {

                    CoreResponse.cors();
                    return Controller.status(202, Json.toJson(invoice));
                }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get all Products with all information's",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of users Products",    response =  Model_Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result product_get_all(){
        try{

            // Kontrola objektu
            List<Model_Product> products = Model_Product.find.where().eq("payment_details.person.id", Controller_Security.getPerson().id).findList();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(products));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Product Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "deactivate product and deactivate all staff around that",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deactivating was successful",  response =  Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result product_deactivate(Long product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = false;
            product.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Product Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "activate product and deactivate all staff around that",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Activateing was successful",  response =  Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result product_activate(Long product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = true;
            product.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Product Tariff",
           hidden = true
    )
    public Result product_delete(Long product_id){
        try{

            // URČENO POUZE PRO ADMINISTRÁTORY S OPRÁVNĚNÍM MAZAT!

            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.delete_permission()) return GlobalResult.forbidden_Permission();

            // Trvalé odstranění produktu!
            product.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "reimbursement of an unpaid invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "reimbursement of an unpaid invoice - with settings from creating product before",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result- payment is required",  response =  Swagger_GoPay_Url.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })  /**  Uživatel může zaplatit neúspěšně zaplacenou fakturu (službu) */
    public Result pay_send_invoice(Long invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            if(!invoice.status.equals(Payment_status.sent)) return GlobalResult.result_BadRequest("Invoice is already paid");


            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury


            JsonNode result = GoPay_Controller.provide_payment("First Payment", invoice.product, invoice);

            if(result.has("id")){

                logger.debug("Set GoPay ID to Invoice");

                invoice.refresh();
                invoice.gopay_id = result.get("id").asLong();
                invoice.gopay_order_number = result.get("order_number").asText();
                invoice.update();

                if(invoice.product.on_demand_active) {

                    logger.debug("Set GoPay ID to Product because Product has ON_DEMAND - TRUE");

                    invoice.product.gopay_id = invoice.gopay_id;
                    invoice.product.update();
                }

            }else {
                logger.error("Result from GoPay not contains id for invoice!");
            }


            // Vytváření pomocného Objektu
            Swagger_GoPay_Url swager_goPay_url = new Swagger_GoPay_Url();
            swager_goPay_url.gw_url = result.get("gw_url").asText();


            // Vrácení ID s možností uhrazení
            return GlobalResult.result_ok(Json.toJson(swager_goPay_url));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Tariff general details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit basic details on user Tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Tariff_General_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_general_product_details(Long product_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Tariff_User_Edit> form = Form.form(Swagger_Tariff_User_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_User_Edit help = form.get();

            // Kontrola Objektu
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            // úpravy objektu
            product.product_individual_name  = help.product_individual_name;

                 if(help.currency_type.equals( Currency.EUR.name()))    product.currency = Currency.EUR;
            else if(help.currency_type.equals( Currency.CZK.name()))    product.currency = Currency.CZK;
            else                                                        return GlobalResult.result_BadRequest("currency is invalid. Use only (EUR, CZK)");

            // Updatování do databáze
            product.update();

            // Vrácení objektu
            return  GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "edit Tariff payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Tariff_User_Details_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_payment_details(Long payment_details_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Tariff_User_Details_Edit> form = Form.form(Swagger_Tariff_User_Details_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_User_Details_Edit help = form.get();

            // Kontrola Objektu
            Model_PaymentDetails payment_details = Model_PaymentDetails.find.byId(payment_details_id);
            if(payment_details == null) return GlobalResult.notFoundObject("Payment_Details payment_details_id not found");

            // Oprávnění operace
            if(!payment_details.edit_permission()) return GlobalResult.forbidden_Permission();


            // úpravy objektu
            payment_details.street        = help.street;
            payment_details.street_number = help.street_number;
            payment_details.city          = help.city;
            payment_details.zip_code      = help.zip_code;
            payment_details.country       = help.country;

            // Pokud se změní nastavení na true (tedy jde o business účet změní se i objekt v databázi
            if (help.company_account & !payment_details.company_account){
                payment_details.company_account = true;
            }

            if (!help.company_account & payment_details.company_account){
                payment_details.company_account          = false;
                payment_details.company_registration_no = null;
                payment_details.company_name             = null;
                payment_details.company_authorized_email = null;
                payment_details.company_authorized_phone = null;
                payment_details.company_web              = null;
                payment_details.company_invoice_email    = null;
            }

            // Pokud je účet business - jsou vyžadovány následující informace
            if(payment_details.company_account) {

                if (help.registration_no == null)           return GlobalResult.result_BadRequest("company_registration_no is required with this tariff");
                if (help.company_name == null)              return GlobalResult.result_BadRequest("company_name is required with this tariff");
                if (help.company_authorized_email == null)  return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if (help.company_authorized_phone == null)  return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if (help.company_web == null)               return GlobalResult.result_BadRequest("company_web is required with this tariff");
                if (help.company_invoice_email == null)     return GlobalResult.result_BadRequest("company_invoice_email is required with this tariff");

                if (help.vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.vat_number))
                        return GlobalResult.badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number = help.vat_number;
                }

                payment_details.company_registration_no = help.registration_no;
                payment_details.company_name = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web = help.company_web;
                payment_details.company_invoice_email = help.company_invoice_email;

            }

            // Updatování do databáze
            payment_details.update();

            // Vrácení objektu
            return  GlobalResult.result_ok(Json.toJson(payment_details));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all the products that the User can use",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all the products that the user can use when creating new projects",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagged_Applicable_Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_applicable_products_for_creating_new_project(){
        try{
            // Slouží k získání možností pod jaký produkt lze vytvořit nějaký projekt

            // Vyhledání všech objektů, které se týkají přihlášeného uživatele
            List<Model_Product> list = Model_Product.find.where().eq("active",true).eq("payment_details.person.id", Controller_Security.getPerson().id).select("id").select("product_individual_name").select("general_tariff.tariff_name").findList();

            List<Swagged_Applicable_Product> products = new ArrayList<>();

            for(Model_Product product : list){
                Swagged_Applicable_Product help = new Swagged_Applicable_Product();
                help.product_id = product.id;
                help.product_individual_name = product.product_individual_name;
                help.product_type = product.general_tariff.tariff_name;

                products.add(help);
            }

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(products));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "cancel automatic payments in Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result temrinate_On_Demance_Payment(Long product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            // Zrušení automatického strhávání z kreditní karty
            product.on_demand_active = false;
            product.update();


            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// INVOICE #############################################################################################

    @ApiOperation(value = "get Invoice with all details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get summary information from invoice",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result invoice_get(Long invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            Swagger_Invoice_FullDetails help = new Swagger_Invoice_FullDetails();
            help.invoice = invoice;
            help.invoice_items = Model_InvoiceItem.find.where().eq("invoice.id", invoice_id).findList();

            return GlobalResult.result_ok(Json.toJson(help));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result invoice_get_pdf(Long invoice_id){

        try {

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            byte[] pdf_in_array = Fakturoid_Controller.download_PDF_invoice(invoice);

            return GlobalResult.result_pdf_file(pdf_in_array, invoice.invoice_number + ".pdf");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }



    // TODO
    public Result send_remainder_to_custumer(Long invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.send_reminder()) return GlobalResult.forbidden_Permission();
            Fakturoid_Controller.send_UnPaidInvoice_to_Email(invoice);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_remove(Long invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            // Kontrola oprávnění
            if(!invoice.delete_permission()) return GlobalResult.forbidden_Permission();

            // TODO - Chybí navázání na fakturoid - smazání faktury (nějaký proces?)

            // Vykonání operace
            invoice.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result connect_invoice_manualy_to_product(Long product_id, String fakturoid_reference_number){
        return TODO;
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result synchonize_invoice_with_fakutoid(Long invoice_id){
        return TODO;
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_set_as_paid(Long invoice_id){
        try{

            //TODO
            List<Model_Invoice> invoices = Model_Invoice.find.all();
            return GlobalResult.result_ok(Json.toJson(invoices) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
