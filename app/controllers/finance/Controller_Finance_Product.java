package controllers.finance;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.ebean.Ebean;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.Currency;
import utilities.enums.ParticipantStatus;
import utilities.enums.PaymentMethod;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_NameAndDescription;
import utilities.swagger.input.Swagger_PaymentDetails_New;
import utilities.swagger.input.Swagger_Product_Credit;
import utilities.swagger.input.Swagger_Product_New;
import utilities.swagger.output.Swagger_Product_Active;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance_Product extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance_Product.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private Fakturoid fakturoid;
    private GoPay goPay;

    @Inject
    public Controller_Finance_Product(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, Fakturoid fakturoid, GoPay goPay) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
        this.fakturoid = fakturoid;
        this.goPay = goPay;
    }

// CONTROLLER CONTENT ##################################################################################################

    @ApiOperation(value = "create Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "It is the base object. Peak of Pyramid :). This Api is used for its creation. You can get two kind of response: \n\n" +
                    "First(201):  System create new Object - Product \n\n" +
                    "Second(200): The product requires payment - The server creates the object, but returns an Invoice \n\n" +
                    "If the user choose credit card payment, the invoice will contain gw_url, which is a link to the payment gate, you can redirect him there. " +
                    "If bank transfer is chosen, server will return an Invoice, but the user will pay it independently via his bank account.",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Product_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created successfully",      response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result product_create() {
        try {

            logger.debug("product_create: Creating new product");

            // Get and Validate Object
            Swagger_Product_New help  = formFromRequestWithValidation(Swagger_Product_New.class);

            // Kontrola Objektu
            Model_Tariff tariff = Model_Tariff.getById(help.tariff_id);

            Model_Customer customer = null;
            Model_Person person = _BaseController.person();

            Ebean.beginTransaction();

            if (help.customer_id != null) {

                customer = Model_Customer.getById(help.customer_id);

            } else {

                if (help.integrator_registration) {
                    return badRequest("Create Integrator Company First");
                }

                customer = new Model_Customer();

                Model_PaymentDetails customer_payment_details = new Model_PaymentDetails();
                if (help.company_name != null && help.company_name.length() > 0) {

                    customer_payment_details.company_account = true;

                    if (help.company_name != null)              customer_payment_details.company_name = help.company_name;
                    if (help.company_authorized_email != null)  customer_payment_details.company_authorized_email = help.company_authorized_email;
                    if (help.company_authorized_phone != null)  customer_payment_details.company_authorized_phone = help.company_authorized_phone;
                    if (help.company_web != null)               customer_payment_details.company_web = help.company_web;
                    if (help.street != null)                    customer_payment_details.street = help.street;
                    if (help.street_number != null)             customer_payment_details.street_number = help.street_number;
                    if (help.city != null)                      customer_payment_details.city = help.city;
                    if (help.zip_code != null)                  customer_payment_details.zip_code = help.zip_code;
                    if (help.country != null)                   customer_payment_details.country = help.country;
                    if (help.zip_code != null)                  customer_payment_details.company_vat_number = help.company_vat_number;
                    if (help.country != null)                   customer_payment_details.company_registration_no = help.company_registration_no;
                    if (help.invoice_email != null)             customer_payment_details.invoice_email = help.invoice_email;
                }

                Model_Employee employee = new Model_Employee();
                employee.state = ParticipantStatus.OWNER;
                employee.person = person;

                employee.customer = customer;
                customer.payment_details = customer_payment_details;
                customer.employees.add(employee);
                customer.save();
            }


            Model_Product product   = new Model_Product();
            product.name            = help.name;
            product.description     = help.description;
            product.active          = true;
            product.method          = help.payment_method;
            product.business_model  = tariff.business_model;
            product.credit          = tariff.credit_for_beginning;

            if (customer != null) product.customer = customer;


            Model_PaymentDetails payment_details = new Model_PaymentDetails();

            if (help.integrator_registration) {

                product.client_billing = true;
                payment_details.company_name = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web = help.company_web;
                payment_details.street = help.street;
                payment_details.street_number = help.street_number;
                payment_details.city = help.city;
                payment_details.zip_code = help.zip_code;
                payment_details.country = help.country;
                payment_details.company_vat_number = help.company_vat_number;
                payment_details.company_registration_no = help.company_registration_no;
                payment_details.invoice_email = help.invoice_email;


            } else {

                product.client_billing = false;

                if (customer.payment_details != null)       payment_details = customer.payment_details.copy();
                if (customer.fakturoid_subject_id != null)  product.fakturoid_subject_id = customer.fakturoid_subject_id;

                if (help.company_name != null)              payment_details.company_name = help.company_name;
                if (help.company_authorized_email != null)  payment_details.company_authorized_email = help.company_authorized_email;
                if (help.company_authorized_phone != null)  payment_details.company_authorized_phone = help.company_authorized_phone;
                if (help.company_web != null)               payment_details.company_web = help.company_web;
                if (help.street != null)                    payment_details.street = help.street;
                if (help.street_number != null)             payment_details.street_number = help.street_number;
                if (help.city != null)                      payment_details.city = help.city;
                if (help.zip_code != null)                  payment_details.zip_code = help.zip_code;
                if (help.country != null)                   payment_details.country = help.country;
                if (help.zip_code != null)                  payment_details.company_vat_number = help.company_vat_number;
                if (help.country != null)                   payment_details.company_registration_no = help.company_registration_no;
                if (help.invoice_email != null)             payment_details.invoice_email = help.invoice_email;

            }


            payment_details.save();

            product.payment_details = payment_details;
            product.save();

            payment_details.product = product;
            payment_details.update();

            payment_details.refresh();

            if (payment_details.isComplete()) {
                logger.trace("product_create:: payment_details.isComplete()");
            }

            if (payment_details.isCompleteCompany()) {
                logger.trace("product_create:: payment_details.isCompleteCompany()");
                logger.trace("product_create:: payment_details::" + Json.toJson(payment_details).toString());

            }


            if (product.fakturoid_subject_id == null) {
                logger.trace("product_create:: fakturoid_subject_id == null");
            }


            if ((payment_details.isComplete() || payment_details.isCompleteCompany()) && product.fakturoid_subject_id == null) {

                product.fakturoid_subject_id = fakturoid.create_subject(payment_details);

                if (product.fakturoid_subject_id == null) return badRequest("Payment details are invalid.");

                product.update();

            }

            product.refresh();

            logger.debug("product_create: Adding extensions");

            List<Model_TariffExtension> extensions = new ArrayList<>();

            if (help.extension_ids.size() > 0) extensions.addAll( Model_TariffExtension.find.query().where().in("id", help.extension_ids).findList());
            extensions.addAll(tariff.extensions_included);

            for (Model_TariffExtension ext : extensions) {
                if (ext.active) {
                    Model_ProductExtension extension = new Model_ProductExtension();
                    extension.name = ext.name;
                    extension.description = ext.description;
                    extension.color = ext.color;
                    extension.type = ext.type;
                    extension.active = true;
                    extension.deleted = false;
                    extension.configuration = ext.configuration;
                    extension.product = product;
                    extension.save();
                }
            }

            Ebean.commitTransaction();

            product.refresh();

            return created(product);

        } catch (Exception e) {
            Ebean.endTransaction();
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Products user Own List",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",     //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of users Products",    response = Model_Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getAll() {
        try {
            // Kontrola objektu
            List<Model_Product> products = Model_Product.getByOwner(personId());

            // Vrácení seznamu
            return ok(products); // todo

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of users Products",    response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_get(UUID product_id) {
        try {

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);

            // Vrácení seznamu
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Product details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit basic details of Product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_update(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);

            // úpravy objektu
            product.name = help.name;
            product.description = help.description;

            // Updatování do databáze
            product.update();

            // Vrácení objektu
            return  ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "deactivate Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "deactivate product Tariff and deactivate all stuff under it",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Deactivating was successful",   response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",            response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",          response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",      response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",              response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",             response = Result_InternalServerError.class)
    })
    public Result product_deactivate(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Kontrola oprávnění
            product.check_act_deactivate_permission();

            if (!product.active) return badRequest("Product is already deactivated");

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = false;
            product.update();

            for(UUID id : product.get_projects_ids()){
                Model_Project.cache.remove(id);
            }

            product.notificationDeactivation();

            // Vrácení potvrzení
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Activate product Tariff and deactivate all staff around that",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Activating was successful", response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_activate(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Kontrola oprávnění
            product.check_act_deactivate_permission();

            if (product.active) return badRequest("Product is already activated");

            // Aktivování
            product.active = true;
            product.update();

            product.notificationActivation();

            // Vrácení potvrzení
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "buy Credit for given product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "creates invoice - credit will be added after payment if payment method is bank transfer or if getting money from credit card is successful",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Product_Credit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_credit(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_Product_Credit help  = formFromRequestWithValidation(Swagger_Product_Credit.class);

            if (!(help.credit > 0)) return badRequest("Credit must be positive double number");

            // Find object
            Model_Product product = Model_Product.getById(product_id);

            Model_Invoice invoice = new Model_Invoice();
            invoice.product = product;
            invoice.method = product.method;

            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
            invoice_item.name = "Credit upload";
            invoice_item.unit_price = 1L;
            invoice_item.quantity = (long) (help.credit * 1000);
            invoice_item.unit_name = "Credit";
            invoice_item.currency = Currency.USD;

            invoice.invoice_items.add(invoice_item);

            invoice = fakturoid.create_proforma(invoice);
            if (invoice == null) return badRequest("Failed to make an invoice, check your provided payment information");

            if (product.method == PaymentMethod.CREDIT_CARD) {

                invoice = goPay.singlePayment("Credit upload payment", product, invoice);
            }

            // Return serialized object
            return  ok(invoice);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "delete Product Tariff",
            tags = {"Admin"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_delete(UUID product_id) {
        try {

            // URČENO POUZE PRO ADMINISTRÁTORY S OPRÁVNĚNÍM MAZAT!

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Trvalé odstranění produktu!
            product.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Product payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "create payments details in Product",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_PaymentDetails_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result paymentDetails_create(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_PaymentDetails_New help  = formFromRequestWithValidation(Swagger_PaymentDetails_New.class);

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);
            if (product.payment_details != null) return badRequest("Product already has Payment Details");


            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.street        = help.street;
            payment_details.street_number = help.street_number;
            payment_details.city          = help.city;
            payment_details.zip_code      = help.zip_code;
            payment_details.country       = help.country;
            payment_details.invoice_email = help.invoice_email;

            // Pokud je účet business - jsou vyžadovány následující informace
            if (payment_details.company_account) {

                if (help.company_vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.company_vat_number))
                        return badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number   = help.company_vat_number;
                }

                payment_details.company_registration_no  = help.company_registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            product.check_update_permission();

            product.fakturoid_subject_id = fakturoid.create_subject(payment_details);
            if (product.fakturoid_subject_id == null) return badRequest("Unable to create your payment details, check provided information.");

            product.method = help.method;
            product.update();

            payment_details.save();

            // Vrácení objektu
            return created(payment_details);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Product payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_PaymentDetails_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result paymentDetails_update(UUID payment_details_id) {
        try {

            // Get and Validate Object
            Swagger_PaymentDetails_New help  = formFromRequestWithValidation(Swagger_PaymentDetails_New.class);

            // Kontrola Objektu
            Model_PaymentDetails payment_details = Model_PaymentDetails.getById(payment_details_id);

            payment_details.street          = help.street;
            payment_details.street_number   = help.street_number;
            payment_details.city            = help.city;
            payment_details.zip_code        = help.zip_code;
            payment_details.country         = help.country;
            payment_details.invoice_email   = help.invoice_email;
            payment_details.product.method  = help.method;

            // Pokud se změní nastavení na true (tedy jde o business účet změní se i objekt v databázi
            if (help.company_account && !payment_details.company_account) {
                payment_details.company_account = true;
            }

            if (!help.company_account && payment_details.company_account) {
                payment_details.company_account          = false;
                payment_details.company_registration_no  = null;
                payment_details.company_name             = null;
                payment_details.company_authorized_email = null;
                payment_details.company_authorized_phone = null;
                payment_details.company_web              = null;
                payment_details.invoice_email            = null;
            }

            // Pokud je účet business - jsou vyžadovány následující informace
            if (payment_details.company_account) {

                if (help.company_vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.company_vat_number))
                        return badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number   = help.company_vat_number;
                }

                payment_details.company_registration_no  = help.company_registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            if (payment_details.product.fakturoid_subject_id == null) {

                payment_details.product.fakturoid_subject_id = fakturoid.create_subject(payment_details);
                if (payment_details.product.fakturoid_subject_id == null) return badRequest("Unable to update your payment details, check provided information.");

                payment_details.update();

            } else {

                if (!fakturoid.update_subject(payment_details))
                    return badRequest("Unable to update your payment details, check provided information.");
            }

            payment_details.product.method = help.method;

            payment_details.product.update();

            // Vrácení objektu
            return  ok(payment_details);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Products user can used",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all the products that the user can use when creating new projects",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Product_Active.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getActive() {
        try {

            List<Swagger_Product_Active> products = new ArrayList<>();

            for (Model_Product product : Model_Product.getApplicableByOwner(_BaseController.personId())) {
                Swagger_Product_Active help = new Swagger_Product_Active();
                help.id = product.id;
                help.name = product.name;

                products.add(help);
            }

            // Vrácení objektu
            return ok(products);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "terminate on demand",
            tags = {"Price & Invoice & Tariffs"},
            notes = "cancel automatic payments in Product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_terminateOnDemand(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            if (product.gopay_id == null) return badRequest("Product has on demand payments turned off.");

            // Zrušení automatického strhávání z kreditní karty
            try {

                goPay.terminateOnDemand(product);

                product.gopay_id = null;
                product.on_demand = false;
                product.update();

                return ok("Successfully terminated on demand payment.");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return badRequest("Request was unsuccessful.");

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
