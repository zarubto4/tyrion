package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;
import utilities.enums.Enum_Payment_status;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagged_Applicable_Product;
import utilities.swagger.outboundClass.Swagger_GoPay_Url;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;
import utilities.swagger.outboundClass.Swagger_ProductExtension_Type;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
            general_tariff.credit_for_beginning    = help.credit_for_beginning;

            general_tariff.company_details_required = help.company_details_required;
            general_tariff.required_payment_mode    = help.required_payment_mode;
            general_tariff.required_payment_method  = help.required_payment_method;

            general_tariff.credit_card_support      = help.credit_card_support;
            general_tariff.bank_transfer_support    = help.bank_transfer_support;

            general_tariff.mode_annually            = help.mode_annually;
            general_tariff.mode_credit              = help.mode_credit;
            general_tariff.free_tariff              = help.free_tariff;

            general_tariff.price_in_usd = help.price_in_usd;


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

            general_tariff.price_in_usd = help.price_in_usd;


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

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_up(String label_id){
        try{

            Model_GeneralTariff tariff =  Model_GeneralTariff.find.byId(label_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_down(String label_id){
        try{

            Model_GeneralTariff tariff =  Model_GeneralTariff.find.byId(label_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.down();

            return GlobalResult.result_ok();

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

    @ApiOperation(value = "get all Product Extension types",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_ProductExtension_Type.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_getAllTypes(){
        try{

            return GlobalResult.result_ok(Json.toJson(Model_ProductExtension.getExtensionTypes()));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ProductExtension_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result productExtension_create(){
        try{

            final Form<Swagger_ProductExtension_New> form = Form.form(Swagger_ProductExtension_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ProductExtension_New help = form.get();

            Model_Product product = Model_Product.find.byId(help.product_id);
            if(product == null) return GlobalResult.notFoundObject("Product not found");

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.type = help.type;
            extension.active = true;
            extension.config = Json.toJson(help.config).toString();
            extension.product = product;

            extension.create_permission();

            extension.save();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_create(){
        try{

            final Form<Swagger_Tariff_General_Extension_Create> form = Form.form(Swagger_Tariff_General_Extension_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Extension_Create help = form.get();

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(help.tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.type = help.type;
            extension.active = true;
            extension.config = Json.toJson(help.config).toString();

            if (help.included){
                extension.general_tariff_included = tariff;
            } else {
                extension.general_tariff_optional = tariff;
            }

            extension.create_permission();

            extension.save();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }
/*
    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_edit(){
        try{

            final Form<Swagger_Tariff_General_Extension_Create> form = Form.form(Swagger_Tariff_General_Extension_Create.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_General_Extension_Create help = form.get();

            Model_ProductExtension extension = Model_ProductExtension.find.byId(help.id);
            if(extension == null) return GlobalResult.notFoundObject("Extensions not found");

            extension.color = help.color;

            extension.price_in_usd = help.price_in_usd;

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }
*/
    @ApiOperation(value = "up label from general Tariffs", hidden = true)
    public Result tariff_general_extension_edit_up(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");
            extension.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_edit_down(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_delete(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_deactivate(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.active = false;
            extension.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariff_general_extension_activate(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.active = true;
            extension.update();

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
            List<Model_GeneralTariff> general_tariffs = Model_GeneralTariff.find.where().eq("active", true).order().asc("order_position").findList();

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

            if(Model_Product.get_byNameAndOwner(help.product_individual_name) != null) return GlobalResult.result_BadRequest("You cannot use same Product name twice!");


            Model_Product product = new Model_Product();


                product.general_tariff =  tariff;
                product.product_individual_name = help.product_individual_name;
                product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času

                product.mode = Enum_Payment_mode.free;

                Model_Person person = Controller_Security.getPerson();

                Model_PaymentDetails payment_details = new Model_PaymentDetails();
                    payment_details.person = person;
                    payment_details.company_account = false;

                    if (help.full_name != null) payment_details.full_name = help.full_name;
                    else payment_details.full_name = person.full_name;

                    payment_details.street = help.street;
                    payment_details.street_number = help.street_number;
                    payment_details.city = help.city;
                    payment_details.zip_code = help.zip_code;
                    payment_details.country = help.country;
                    payment_details.invoice_email = help.invoice_email;


                    if(tariff.company_details_required){

                        if((help.registration_no == null)&&(help.vat_number == null)) return GlobalResult.result_BadRequest("company_registration_no or vat_number is required with this tariff");
                        if(help.company_name == null)               return GlobalResult.result_BadRequest("company_name is required with this tariff");

                        if(help.company_authorized_email == null)   return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                        if(help.company_authorized_phone == null)   return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                        if(help.company_web == null)                return GlobalResult.result_BadRequest("company_web is required with this tariff");

                        try {
                            new URL(help.company_web);
                        } catch (MalformedURLException malformedURLException) {
                            return GlobalResult.result_BadRequest("company_web invalid value");
                        }

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

                    }




                logger.debug("Financial_Controller:: product_create:: Payment details done");

                // payment_mode
                if(tariff.required_payment_mode) {

                    logger.debug("Financial_Controller:: product_create:: Payment mode Required");

                    if(help.payment_mode == null) return GlobalResult.result_BadRequest("Payment_mode is required!");

                    if(help.payment_mode.equals( Enum_Payment_mode.free.name()))              product.mode = Enum_Payment_mode.free;
                    else if(help.payment_mode.equals( Enum_Payment_mode.annual.name()))       product.mode = Enum_Payment_mode.annual;
                    else if(help.payment_mode.equals( Enum_Payment_mode.monthly.name()))      product.mode = Enum_Payment_mode.monthly;
                    else if(help.payment_mode.equals( Enum_Payment_mode.per_credit.name()))   product.mode = Enum_Payment_mode.per_credit;
                    else { return GlobalResult.result_BadRequest("payment_mode is invalid. Use only (free, monthly, annual, per_credit)");}

                }


                if(tariff.required_payment_method) {

                    logger.debug("Financial_Controller:: product_create:: Payment method Required");

                    if(help.payment_method == null) return GlobalResult.result_BadRequest("payment_method is required with this tariff");

                         if(help.payment_method.equals( Enum_Payment_method.bank_transfer.name()))  product.method = Enum_Payment_method.bank_transfer;
                    else if(help.payment_method.equals( Enum_Payment_method.credit_card.name()))    product.method = Enum_Payment_method.credit_card;
                    else if(help.payment_method.equals( Enum_Payment_method.free.name()))           product.method = Enum_Payment_method.free;
                    else { return GlobalResult.result_BadRequest("payment_method is invalid. Use only (bank_transfer, credit_card, free)");}

                }

                // Přidám ty, co vybral uživatel
                if(help.extension_ids.size() > 0) {
                    List<Model_ProductExtension> list = Model_ProductExtension.find.where().in("id", help.extension_ids).eq("general_tariff_optional.id", tariff.id).findList();
                    product.extensions.addAll(list);
                }

            // Přidám všechny, které má Tarrif už v sobě - Ale jen ty aktivní
                for ( Model_ProductExtension extension : tariff.extensions_included){
                    if(extension.active) product.extensions.add(extension);
                }



                payment_details.save();
                product.save();

                payment_details.product = product;
                payment_details.update();

                product.payment_details = payment_details;
                product.remaining_credit += tariff.credit_for_beginning;
                product.update();

                if(!tariff.required_paid_that) {
                    logger.debug("Financial_Controller:: product_create:: Its not required pay that!");
                    return GlobalResult.created(Json.toJson(product));
                }


                logger.debug("Financial_Controller:: product_create:: Creating invoice");

                Model_Invoice invoice = new Model_Invoice();
                invoice.date_of_create = new Date();
                invoice.proforma = true;

                if(product.method == Enum_Payment_method.credit_card) {
                    invoice.status = Enum_Payment_status.sent;
                }
                if(product.method == Enum_Payment_method.bank_transfer) {
                    invoice.status = Enum_Payment_status.created_waited;
                }

                Model_InvoiceItem invoice_item_1 = new Model_InvoiceItem();
                invoice_item_1.name = product.general_tariff.tariff_name + " in Mode(" + product.mode.name() + ")";
                invoice_item_1.unit_price = product.general_tariff.price_in_usd;
                invoice_item_1.quantity = (long) 1;
                invoice_item_1.unit_name = "Currency";
                invoice_item_1.currency = Enum_Currency.USD;

                invoice.invoice_items.add(invoice_item_1);
                invoice.method = product.method;

                invoice.product = product;

                logger.debug("Financial_Controller:: product_create:: Saving invoice");
                invoice.save();

                Model_Invoice test = Model_Invoice.find.byId(invoice.id);
                logger.debug("Financial_Controller:: product_create:: " + Json.toJson(test));


                logger.debug("Financial_Controller:: product_create::  Creating Proforma in fakturoid");
                Utilities_Fakturoid_Controller.create_proforma(product, invoice);
                logger.debug("Financial_Controller:: product_create::  Proforma done");


                if(product.method == Enum_Payment_method.credit_card){

                    logger.debug("Financial_Controller:: product_create::  User want pay it with credit card!");

                    logger.debug("Financial_Controller:: product_create::  Preparing for providing payment");
                    JsonNode result = Utilities_GoPay_Controller.provide_payment("First Payment", product, invoice);


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
            e.printStackTrace();
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
            List<Model_Product> products = Model_Product.get_byOwner(Controller_Security.getPerson().id);

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
    public Result product_deactivate(String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
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
    public Result product_activate(String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
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
    public Result product_delete(String product_id){
        try{

            // URČENO POUZE PRO ADMINISTRÁTORY S OPRÁVNĚNÍM MAZAT!

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
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
    public Result pay_send_invoice(String invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            if(!invoice.status.equals(Enum_Payment_status.sent)) return GlobalResult.result_BadRequest("Invoice is already paid");


            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury


            JsonNode result = Utilities_GoPay_Controller.provide_payment("First Payment", invoice.product, invoice);

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
    public Result edit_general_product_details(String product_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Tariff_User_Edit> form = Form.form(Swagger_Tariff_User_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_User_Edit help = form.get();

            // Kontrola Objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            // úpravy objektu
            product.product_individual_name  = help.product_individual_name;

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
                payment_details.invoice_email = null;
            }

            // Pokud je účet business - jsou vyžadovány následující informace
            if(payment_details.company_account) {

                if (help.registration_no == null)           return GlobalResult.result_BadRequest("company_registration_no is required with this tariff");
                if (help.company_name == null)              return GlobalResult.result_BadRequest("company_name is required with this tariff");
                if (help.company_authorized_email == null)  return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if (help.company_authorized_phone == null)  return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if (help.company_web == null)               return GlobalResult.result_BadRequest("company_web is required with this tariff");
                if (help.company_invoice_email == null)     return GlobalResult.result_BadRequest("invoice_email is required with this tariff");

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
                payment_details.invoice_email = help.company_invoice_email;

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
            List<Model_Product> list = Model_Product.get_applicableByOwner(Controller_Security.getPerson().id);

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
    public Result temrinate_On_Demance_Payment(String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
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
    public Result invoice_get(String invoice_id){
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


    @ApiOperation(value = "re-send Invoice to specific email",
            tags = {"Price & Invoice & Tariffs"},
            notes = "re-send Invoice to specific email",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Resend_Email",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values - values in Json is not requierd"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result resend_invoice(String invoice_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Resend_Email> form = Form.form(Swagger_Resend_Email.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Resend_Email help = form.get();


            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");
            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();


            // Email na který se faktura zašle
            String email = null;

            if(help.mail == null || help.mail.length() < 1) email = invoice.product.payment_details.invoice_email;
            else email = help.mail;

            System.out.println("Email na který přeposílám fakturu je:: " + email);

            Utilities_Fakturoid_Controller.send_invoice_to_Email(invoice, email);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }





    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result send_remainder_to_custumer(String invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.send_reminder()) return GlobalResult.forbidden_Permission();
            Utilities_Fakturoid_Controller.send_UnPaidInvoice_to_Email(invoice);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_remove(String invoice_id){
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
    public Result synchonize_invoice_with_fakutoid(String invoice_id){
        return TODO;
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_set_as_paid(String invoice_id){
        try{

            //TODO
            List<Model_Invoice> invoices = Model_Invoice.find.all();
            return GlobalResult.result_ok(Json.toJson(invoices) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
