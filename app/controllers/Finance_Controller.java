package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.person.Person;
import models.project.global.Product;
import models.project.global.financial.Invoice;
import models.project.global.financial.Invoice_item;
import models.project.global.financial.Payment_Details;
import play.Configuration;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.UtilTools;
import utilities.fakturoid.Fakturoid_Controller;
import utilities.goPay.GoPay_Controller;
import utilities.goPay.helps_objects.enums.Currency;
import utilities.goPay.helps_objects.enums.Payment_method;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_BadRequest;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Swagger_Tariff_Details_Edit;
import utilities.swagger.documentationClass.Swagger_Tariff_General_Edit;
import utilities.swagger.documentationClass.Swagger_Tariff_Register;
import utilities.swagger.outboundClass.Swagged_Applicable_Product;
import utilities.swagger.outboundClass.Swagger_Financial_Summary;
import utilities.swagger.outboundClass.Swagger_GoPay_Url;
import utilities.swagger.outboundClass.Swagger_Tariff;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Finance_Controller extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");
    static Swagger_Tariff swagger_tariff;

    // GENERAL PRODUCT_TARIFF ##########################################################################################

    @ApiOperation(value = "get all Product Tariffs",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Tariffs - required for every else action in system. For example: Project is created under the Product tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_Tariff.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_poducts_tariffs(){
        try{


            if(swagger_tariff != null ) return GlobalResult.result_ok(Json.toJson(swagger_tariff));
            else  swagger_tariff = new Swagger_Tariff();

            // Vytvořím seznam tarifu
            List<String> product_tariffs = Configuration.root().getStringList("Byzance.tariff.tariffs");


             //Tak beru a načítám z konfiguračního souboru application.conf
            for (String tariff_name : product_tariffs) {
               try {

                   Swagger_Tariff.Individuals_Tariff tariff = swagger_tariff.get_new_Tariff();


                   tariff.identificator = tariff_name;
                   tariff.tariff_name = Configuration.root().getString("Byzance.tariff." + tariff_name + ".name");

                   tariff.company_details_required = Configuration.root().getBoolean("Byzance.tariff." + tariff_name + ".company_details_required");
                   tariff.required_payment_mode    = Configuration.root().getBoolean("Byzance.tariff." + tariff_name + ".required_payment_mode");
                   tariff.price                    = swagger_tariff.get_new_Price( Configuration.root().getDouble("Byzance.tariff." + tariff_name + ".price_list.general_fee.monthly.CZK"),
                                                                                   Configuration.root().getDouble("Byzance.tariff." + tariff_name + ".price_list.general_fee.monthly.EUR"));

                   tariff.labels                   = swagger_tariff.get_new_Label( Configuration.root().getString("Byzance.tariff." + tariff_name + ".public_labels"));

                   swagger_tariff.tariffs.add(tariff);

               } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Tyrion try to get Tariffs from Configuration file. But probably enum value (\"enums\") \"" + product_tariffs + "\" do not correspond or missing some value in configuration");
                        e.printStackTrace();
                    }
               }


            List<String> product_packages = Configuration.root().getStringList("Byzance.tariff.packages");
            for (String packages_identificator : product_packages) {
                try {

                    Swagger_Tariff.Additional_package aditional_package = swagger_tariff.get_new_Additional_package();

                    // Defaultní hodnoty služeb
                    aditional_package.identificator = packages_identificator;
                    aditional_package.package_name  = Configuration.root().getString("Byzance.tariff.package." + packages_identificator + ".name");

                    aditional_package.price = swagger_tariff.get_new_Price( Configuration.root().getDouble("Byzance.tariff.package." + packages_identificator + ".CZK"),
                                                                            Configuration.root().getDouble("Byzance.tariff.package." + packages_identificator + ".EUR"));

                    aditional_package.labels = swagger_tariff.get_new_Label( Configuration.root().getString("Byzance.tariff.package." + packages_identificator + ".public_labels")  );

                    swagger_tariff.packages.add(aditional_package);

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Tyrion try to get Additional Packages from Configuration file. But probably enum value (\"enums\") \"" + packages_identificator + "\" do not correspond or missing some value in configuration");
                    e.printStackTrace();
                }
            }


            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(swagger_tariff));

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
                            dataType = "utilities.swagger.documentationClass.Swagger_Tariff_Register",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created successfully - payment not required",    response =  Product.class),
            @ApiResponse(code = 200, message = "Created successfully - but payment is required", response =  Swagger_GoPay_Url.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result set_tariff_with_account(){
        try{

            // Zpracování Json
            final Form<Swagger_Tariff_Register> form = Form.form(Swagger_Tariff_Register.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_Register help = form.get();

            Product product = new Product();

            if(help.currency_type.equals( Currency.EUR.name())) product.currency = Currency.EUR;
            else if(help.currency_type.equals( Currency.CZK.name())) product.currency = Currency.CZK;
            else { return GlobalResult.result_BadRequest("currency is invalid. Use only (EUR, CZK)");}



            if(help.tariff_type.equals( Product.Product_Type.alpha.name() )){

                product.type =  Product.Product_Type.alpha;
                product.product_individual_name = help.product_individual_name;
                product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času

                product.mode = Product.Payment_mode.free;
                product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();

                Person person = SecurityController.getPerson();

                Payment_Details payment_details = new Payment_Details();
                payment_details.person = person;
                payment_details.company_account = false;


                payment_details.street = help.street;
                payment_details.street_number = help.street_number;
                payment_details.city = help.city;
                payment_details.zip_code = help.zip_code;
                payment_details.country = help.country;

                payment_details.product = product;
                product.payment_details = payment_details;

                // V oprávnění proběhne kontrola
                if(!product.create_permission()) return GlobalResult.forbidden_Permission();
                product.save();

                return GlobalResult.created( Json.toJson(product));
            }


            if(help.tariff_type.equals( Product.Product_Type.free.name() )){
                product.type =  Product.Product_Type.free;
                product.product_individual_name = help.product_individual_name;
                product.active = true;  // Produkt jelikož je free je aktivní - Alpha nebo Trial dojedou kvuli omezení času
                product.mode = Product.Payment_mode.free;


                product.paid_until_the_day = new GregorianCalendar(2025, 12, 30).getTime();

                Payment_Details payment_details = new Payment_Details();
                payment_details.person = SecurityController.getPerson();
                payment_details.company_account = false;
                payment_details.street = help.street;
                payment_details.street_number = help.street_number;
                payment_details.city = help.city;
                payment_details.zip_code = help.zip_code;
                payment_details.country = help.country;

                payment_details.product = product;
                product.payment_details = payment_details;

                // V oprávnění proběhne kontrola
                if(!product.create_permission()) return GlobalResult.forbidden_Permission();
                product.save();

                return GlobalResult.created( Json.toJson(product));
            }


            if(help.tariff_type.equals( Product.Product_Type.business.name() )){
                product.active = true; // Produkt se aktivuje okamžitě ale nenastaví se tam jeho čas do kdy je funkční
                product.product_individual_name = help.product_individual_name;

                // payment_mode
                if(help.payment_mode == null) return GlobalResult.result_BadRequest("payment_mode is required with this tariff");

                if(help.payment_mode.equals( Product.Payment_mode.monthly.name()))           product.mode = Product.Payment_mode.monthly;
                else if(help.payment_mode.equals( Product.Payment_mode.annual.name()))       product.mode = Product.Payment_mode.annual;
                else if(help.payment_mode.equals( Product.Payment_mode.per_credit.name()))   product.mode = Product.Payment_mode.per_credit;
                else { return GlobalResult.result_BadRequest("payment_mode is invalid. Use only (monthly, annual, per_credit)");}


                // payment_method
                if(help.payment_method == null) return GlobalResult.result_BadRequest("payment_method is required with this tariff");

                if(help.payment_method.equals( Payment_method.bank.name()))                product.method = Payment_method.bank;
                else if(help.payment_method.equals( Payment_method.credit_card.name()))    product.method = Payment_method.credit_card;
                else { return GlobalResult.result_BadRequest("payment_mode is invalid. Use only (bank, credit_card)");}


                product.type  =  Product.Product_Type.business;

                Payment_Details payment_details = new Payment_Details();
                payment_details.person = SecurityController.getPerson();
                payment_details.company_account = true;

                payment_details.street        = help.street;
                payment_details.street_number = help.street_number;
                payment_details.city          = help.city;
                payment_details.zip_code      = help.zip_code;
                payment_details.country       = help.country;

                if(help.registration_no == null)            return GlobalResult.result_BadRequest("company_registration_no is required with this tariff");

                if(help.company_name == null)               return GlobalResult.result_BadRequest("company_name is required with this tariff");
                if(help.company_authorized_email == null)   return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if(help.company_authorized_phone == null)   return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if(help.company_web == null)                return GlobalResult.result_BadRequest("company_web is required with this tariff");
                if(help.company_invoice_email == null)      return GlobalResult.result_BadRequest("company_invoice_email is required with this tariff");

                if(help.vat_number != null) {
                    if (!UtilTools.controll_vat_number(help.vat_number))return GlobalResult.badRequest("Prefix code in VatNumber is not valid");
                     payment_details.company_vat_number = help.vat_number;
                }


                payment_details.company_registration_no = help.registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
                payment_details.company_invoice_email    = help.company_invoice_email;

                product.payment_details = payment_details;

                Invoice invoice = new Invoice();
                invoice.date_of_create = new Date();
                invoice.proforma = true;
                invoice.status = Invoice.Payment_status.sent;


                Invoice_item invoice_item_1 = new Invoice_item();
                    invoice_item_1.name = product.type.name() + " Mode(" +  product.mode.name() + ")";
                    invoice_item_1.unit_price = product.get_price_general_fee();
                    invoice_item_1.quantity = (long) 1;
                    invoice_item_1.unit_name = "Service";

                invoice.invoice_items.add(invoice_item_1);
                invoice.method = product.method;

                product.invoices.add(invoice);
                product.save();

                invoice.refresh();

                logger.debug("Creating Proforma in fakturoid");
                Fakturoid_Controller.create_proforma(product, invoice);

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


                Swagger_GoPay_Url swager_goPay_url = new Swagger_GoPay_Url();
                swager_goPay_url.gw_url = result.get("gw_url").asText();

                return GlobalResult.result_ok(Json.toJson(swager_goPay_url));
            }

            // Vrácení objektu
            return GlobalResult.result_BadRequest("Tariff_name {" + help.tariff_type  + "} not found or not supported now! Use only " + Configuration.root().getStringList("Byzance.tariff.tariffs").toString() );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "delete Product Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "delete product and deactivate all staff around that",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleting was successful",  response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result deactivate_product(Long product_id){
        try{

            // Kontrola objektu
            Product product = Product.find.byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.read_permission()) return GlobalResult.forbidden_Permission();

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = false;
            product.update();

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

            Invoice invoice = Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            if(!invoice.status.equals(Invoice.Payment_status.sent)) return GlobalResult.result_BadRequest("Invoice is already paid");


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
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Product.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_general_product_details(Long product_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Tariff_General_Edit> form = Form.form(Swagger_Tariff_General_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Edit help = form.get();

            // Kontrola Objektu
            Product product = Product.find.byId(product_id);
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Tariff_Details_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Payment_Details.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_payment_details(Long payment_details_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Tariff_Details_Edit> form = Form.form(Swagger_Tariff_Details_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_Details_Edit help = form.get();

            // Kontrola Objektu
            Payment_Details payment_details = Payment_Details.find.byId(payment_details_id);
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
                    if (!UtilTools.controll_vat_number(help.vat_number))
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
            List<Product> list = Product.find.where().eq("payment_details.person.id",SecurityController.getPerson().id).findList();

            // Zjednodušení objektů, aby se nezasílalo tolik informací
            List<Swagged_Applicable_Product> applicable_products = new ArrayList<>();
            for(Product product : list) {

                Swagged_Applicable_Product applicable_product = new Swagged_Applicable_Product();

                    applicable_product.product_id = product.id;
                    applicable_product.product_individual_name = product.product_individual_name;
                    applicable_product.product_type = product.product_type();

                applicable_products.add(applicable_product);
            }

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(applicable_products));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Financial Summary for logged User",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Financial Summary for logged User",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Swagger_Financial_Summary.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_financial_summary(){
        try {

            // Kompletní datový přehled pro stránku finančního přehledu
            List<Product> products = Product.find.where().eq("payment_details.person.id", SecurityController.getPerson().id).findList();

            // Pomocný objekt, který pomůže zabalit další informace (třeba i do budoucna)
            Swagger_Financial_Summary summary = new Swagger_Financial_Summary();
            summary.products = products;

            // Vrácení objetu
            return GlobalResult.result_ok(Json.toJson(summary));

        }catch (Exception e){
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
            Product product = Product.find.byId(product_id);
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


    public Result add_new_package(Long product_id){
        return TODO;
    }

    public Result change_tariff(Long product_id){
        return TODO;
    }


    public Result send_remainder_to_custumer(Long product_id){
        return TODO;
    }

    public Result remove_invoice(Long product_id){
        return TODO;
    }

    public Result set_invoice_as_Paid(Long invoice_id){
        try{

            List<Invoice> invoices = Invoice.find.all();
            return GlobalResult.result_ok(Json.toJson(invoices) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
