package utilities.financial.services;

import controllers.Controller_Finance;
import controllers._BaseController;
import io.ebean.Ebean;
import models.*;
import play.libs.Json;
import utilities.enums.ParticipantStatus;
import utilities.enums.ProductEventType;
import utilities.enums.ExtensionType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.fakturoid.FakturoidService;
import utilities.financial.fakturoid.helps_objects.Fakturoid_Subject;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Contact_Update;
import utilities.swagger.input.Swagger_ProductExtension_New;
import utilities.swagger.input.Swagger_Product_New;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for manipulation of product extensions. It logs all the operation and calls the given Extension class
 * which handles the event.
 * <br><br>
 * This class should be used for product extension manipulation all the time.
 */
@Singleton
public class ProductService {

    // LOGGER ##########################################################################################################

    private static final Logger logger = new Logger(Controller_Finance.class);

    // CONSTRUCTOR #####################################################################################################
    private final FakturoidService fakturoidService;

    @Inject
    public ProductService(FakturoidService fakturoidService) {
        this.fakturoidService = fakturoidService;
    }

    // SERVICE METHODS #################################################################################################

    public Model_Product createAndActivateProduct(Swagger_Product_New help) throws Exception {
        logger.debug("product_create: Creating new product");

        // Kontrola Objektu
        Model_Tariff tariff = Model_Tariff.find.byId(help.tariff_id);

        Model_Customer customer = null;
        Model_Person person = _BaseController.person();

        Ebean.beginTransaction();

        // If we do not an owner id, new customer is created. Contact might be empty.
        if (help.owner_id != null) {
            customer = Model_Customer.find.byId(help.owner_id);
        } else {
            customer = new Model_Customer();

            Model_Employee employee = new Model_Employee();
            employee.state = ParticipantStatus.OWNER;
            employee.person = person;

            employee.customer = customer;
            customer.employees.add(employee);

            if (help.owner_new_contact != null) {
                // We save new contact even if the Fakturoid contact is not created.
                // Field for Fakturoid contact id will be empty, we can try later and user will not be annoyed.
                customer.contact = setContact(help.owner_new_contact, null);
            }

            customer.save();
        }

        // Payment details are always created. Event if user did not fill them - we need them to save our own data,
        // payments methods for now
        Model_PaymentDetails paymentDetails = new Model_PaymentDetails();
        paymentDetails.payment_methods = tariff.payment_methods;
        if(help.payment_details != null) {
            paymentDetails.payment_method = help.payment_details.payment_method;
        }
        paymentDetails.save();

        Model_Product product   = new Model_Product();
        product.name            = help.name;
        product.description     = help.description;
        product.active          = true;
        product.business_model  = tariff.business_model;
        product.credit          = tariff.credit_for_beginning;
        product.payment_details = paymentDetails;
        if (customer != null) {
            product.owner = customer;
        }
        product.save();


//        if (payment_details.isComplete()) {
//            logger.trace("product_create:: contact.isComplete()");
//        }
//
//        if (payment_details.isCompleteCompany()) {
//            logger.trace("product_create:: contact.isCompleteCompany()");
//            logger.trace("product_create:: contact::" + Json.toJson(payment_details).toString());
//
//        }

//
//        if (product.contact.fakturoid_subject_id == null) {
//            logger.trace("product_create:: fakturoid_subject_id == null");
//        }


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
                extension.configuration = ext.configuration;
                extension.product = product;
                extension.save();

                try {
                    extension.setActive(true);
                }
                catch (Exception e) {
                    // We do not throw any error, but calling class should check if the extension is active.
                    // We want the extension to be saved because whatever problem occurred, we want to have the full information about what was happening.
                    // In case we do not want to show the extension to the user, we can mark it as deleted.
                    logger.error("Error while activating an extension " + extension.id + ".", e);
                }
            }
        }

        Ebean.commitTransaction();

        product.refresh();

        product.saveEvent(product.created, ProductEventType.PRODUCT_CREATED, null);

        if(product.owner.contact != null) {
            new Thread(() -> {
                updateContactFakturoid(product.owner.contact);
            }).start();
        }

        return product;
    }

    public Model_ProductExtension createAndActivateExtension(Model_Product product, Swagger_ProductExtension_New extensionData) {
        Model_ProductExtension extension = new Model_ProductExtension();

        try {
            ExtensionType type = ExtensionType.valueOf(extensionData.extension_type);
            extension.type = type;
        } catch (Exception e) {
            throw new Result_Error_NotFound(ExtensionType.class);
        }

        extension.name = extensionData.name;
        extension.description = extensionData.description;
        extension.color = extensionData.color;
        extension.product = product;
        extension.order_position = product.getExtensionCount();

        Configuration config = Configuration.getConfiguration(extension.type, extensionData.config);
        extension.configuration = Json.toJson(config).toString();

        extension.save();

        try {
            extension.setActive(true);
        }
        catch (Exception e) {
            // We do not throw any error, but calling class should check if the extension is active.
            // We want the extension to be saved because whatever problem occurred, we want to have the full information about what was happening.
            // In case we do not want to show the extension to the user, we can mark it as deleted.
            logger.error("Error while activating an extension " + extension.id + ".", e);
        }

        return extension;
    }

    public Model_Contact setContact(Swagger_Contact_Update help, Model_Contact contact) {
        boolean update = contact != null;
        if(contact == null) {
            contact = new Model_Contact();
        }

        contact.company_account = help.company_account;
        contact.name            = help.name;
        contact.street          = help.street;
        contact.street_number   = help.street_number;
        contact.city            = help.city;
        contact.zip_code        = help.zip_code;
        contact.country         = help.country;
        contact.invoice_email   = help.invoice_email;

        contact.company_registration_no  = help.company_account ? help.company_registration_no : null;
        contact.company_authorized_email = help.company_account ? help.company_authorized_email : null;
        contact.company_authorized_phone = help.company_account ? help.company_authorized_phone : null;
        contact.company_web              = help.company_account ? help.company_web : null;

        // TODO check in EU
        contact.company_registration_no  = help.company_account ? help.company_registration_no : null;
        if (contact.company_account && help.company_vat_number != null) {
            if (!Model_Contact.control_vat_number(help.company_vat_number)) {
                throw new IllegalArgumentException("Prefix code in VatNumber is not valid.");
            }
        }

        if(update) {
            contact.update();
        }
        else {
            contact.save();
        }

        return contact;
    }

    public void updateContactFakturoid(Model_Contact contact) {
        try {
            if (contact.fakturoid_subject_id == null) {
                Fakturoid_Subject createdSubj = fakturoidService.createSubject(contact);
                if (createdSubj == null) {
                    logger.error("Fakturoid contact for contact id {} cannot not created.", contact.id);
                }

                contact.fakturoid_subject_id = createdSubj.id;
            } else {
                if (!fakturoidService.updateSubject(contact)) {
                    contact.fakturoid_subject_id = null;
                    logger.error("Fakturoid contact for contact id {} cannot not updated. Fakturoid id removed.", contact.id);
                }
            }
        }
        catch (Exception e) {
            contact.fakturoid_subject_id = null;
            logger.error("Error while creating / updating Fakturoid contact for contact id " + contact.id
                    + ". If present, Fakturoid id removed.", e);
        }

        contact.update();
    }
}