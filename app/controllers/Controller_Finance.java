package controllers;

import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.*;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.*;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Invoice_FullDetails;

import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private Fakturoid fakturoid;
    private GoPay goPay;

    @Inject public Controller_Finance(_BaseFormFactory formFactory, Fakturoid fakturoid, GoPay goPay) {
        this.baseFormFactory = formFactory;
        this.fakturoid = fakturoid;
        this.goPay = goPay;
    }


// INVOICE #############################################################################################################

    @ApiOperation(value = "get Invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get summary information from invoice",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_get(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            Swagger_Invoice_FullDetails help = new Swagger_Invoice_FullDetails();
            help.invoice = invoice;
            help.invoice_items = Model_InvoiceItem.find.query().where().eq("invoice.id", invoice_id).findList();

            return ok(help);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "resend Invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "resend Invoice to specific email",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Resend_Email",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values - values in Json is not requierd"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong ",       response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_resend(UUID invoice_id) {
        try {

            // Get and Validate Object
            Swagger_Resend_Email help = baseFormFactory.formFromRequestWithValidation(Swagger_Resend_Email.class);

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            fakturoid.sendInvoiceEmail(invoice, help.email);

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "send Invoice reimbursement",
            tags = {"Price & Invoice & Tariffs"},
            notes = "reimbursement of an unpaid invoice - with settings from creating product before",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })  /**  Uživatel může zaplatit neúspěšně zaplacenou fakturu (službu) */
    public Result invoice_reimbursement(UUID invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            if ( invoice.status.equals(PaymentStatus.PAID)) return badRequest("Invoice is already paid");

            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury
            invoice = goPay.singlePayment("First Payment", invoice.getProduct(), invoice);

            // Vrácení ID s možností uhrazení
            return ok(invoice);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
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
    public Result invoice_getPdf(String kind, UUID invoice_id) {
        try {

            if (!kind.equals("proforma") && !kind.equals("invoice")) return badRequest("kind should be 'proforma' or 'invoice'");

            Model_Invoice invoice = Model_Invoice.getById(invoice_id);


            if (kind.equals("proforma") && invoice.proforma_pdf_url == null) return badRequest("Proforma PDF is unavailable");


            byte[] pdf_in_array = fakturoid.download_PDF_invoice(kind, invoice);

            return file(pdf_in_array, kind.equals("proforma") ? "proforma_" + invoice.invoice_number + ".pdf" : invoice.invoice_number + ".pdf");

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "send Invoice Reminder",
            tags = {"Admin-Invoice"},
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
    public Result invoice_reminder(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            fakturoid.sendInvoiceReminderEmail(invoice,"You have pending unpaid invoice.");

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Invoice",
            tags = {"Admin-Invoice"},
            notes = "remove Invoice only with permission",
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
    public Result invoice_delete(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);
          
            // TODO - Chybí navázání na fakturoid - smazání faktury (nějaký proces?)
           
            //Fakturoid_Controller.fakturoid_delete()
            logger.error("invoice_delete: Not Supported");
            throw new Result_Error_NotSupportedException();
            

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Invoice Synchronize with Fakturoid",
            tags = {"Admin"},
            notes = "remove Invoice only with permission",
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
    public Result invoice_synchronizeFakturoid(UUID invoice_id) {

        try {
            // TODO invoice_synchronizeFakturoid
            logger.error("invoice_synchronizeFakturoid: Not Supported");
            throw new Result_Error_NotSupportedException();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "edit Invoice Set As Paid",
            tags = {"Admin-Invoice"},
            notes = "remove Invoice only with permission",
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
    @Security.Authenticated(Authentication.class)
    public Result invoice_set_as_paid(UUID invoice_id) {
        try {

            // TODO invoice_set_as_paid
            logger.error("invoice_set_as_paid: Not Supported");
            throw new Result_Error_NotSupportedException();
         
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// CUSTOMER ############################################################################################################

    @ApiOperation(value = "create Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Creates new Customer (type: company), you can crate new product under Customer(company) or under Customer(person)",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_create_company() {
        try {

            // Get and Validate Object
            Swagger_Customer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_New.class);

            Model_Customer customer = new Model_Customer();
            
            customer.save();

            Model_Employee employee = new Model_Employee();
            employee.person = _BaseController.person();
            employee.state = ParticipantStatus.OWNER;
            employee.customer = customer;
            employee.save();

            customer.refresh();

            Model_PaymentDetails details = new Model_PaymentDetails();
            details.street = help.street;
            details.street_number = help.street_number;
            details.city = help.city;
            details.zip_code = help.zip_code;
            details.country = help.country;

            details.invoice_email = help.invoice_email;

            details.company_name = help.company_name;
            details.company_authorized_phone = help.company_authorized_phone;
            details.company_authorized_email = help.company_authorized_email;
            details.company_vat_number = help.company_vat_number;
            details.company_registration_no = help.company_registration_no;
            details.company_web = help.company_web;

            details.customer = customer;
            details.company_account = true;
            details.save();

            customer.fakturoid_subject_id = fakturoid.create_subject(details);
            customer.update();

            return created(customer);
        } catch (IllegalArgumentException e) {
            return badRequest("Payment details are invalid.");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Companies All",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Gets all companies by logged user.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created successfully",      response = Model_Customer.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_get_all() {
        try {

            List<Model_Customer> customers = Model_Customer.find.query().where().eq("employees.person.id", _BaseController.personId()).eq("payment_details.company_account", true).findList();

            return ok(customers);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Updates payment details of a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_update_company(UUID customer_id) {
        try {

            // Get and Validate Object
            Swagger_Customer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_New.class);

            Model_Customer customer = Model_Customer.getById(customer_id);
        
            Model_PaymentDetails details = customer.payment_details;
            details.street          = help.street;
            details.street_number   = help.street_number;
            details.city            = help.city;
            details.zip_code        = help.zip_code;
            details.country         = help.country;

            details.invoice_email   = help.invoice_email;

            details.company_name             = help.company_name;
            details.company_authorized_phone = help.company_authorized_phone;
            details.company_authorized_email = help.company_authorized_email;
            details.company_vat_number       = help.company_vat_number;
            details.company_registration_no  = help.company_registration_no;
            details.company_web              = help.company_web;

            details.update();

            if (!fakturoid.update_subject(details))
                return badRequest("Payment details are invalid.");

            return ok(customer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Employee",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Adds employee to a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_Employee",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_add_employee() {
        try {

            // Get and Validate Object
            Swagger_Customer_Employee help  = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_Employee.class);

            Model_Customer customer = Model_Customer.getById(help.customer_id);
         
            for (Model_Person person : Model_Person.find.query().where().in("email", help.mails).findList()) {

                // Abych nepřidával ty co už tam jsou
                if (customer.employees.stream().anyMatch(employee -> employee.get_person_id().equals(person.id))) continue;

                Model_Employee employee = new Model_Employee();
                employee.person     = person;
                employee.state      = ParticipantStatus.MEMBER;
                employee.customer   = customer;
                employee.save();
            }

            customer.refresh();

            return ok(customer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Employee",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Removes employee from a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Removed successfully",      response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_remove_employee(UUID employee_id) {
        try {
            
            Model_Employee employee = Model_Employee.getById(employee_id);
  
            employee.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Deletes company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Removed successfully",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_delete_company(UUID customer_id) {
        try {

            Model_Customer customer = Model_Customer.getById(customer_id);
       
            customer.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}