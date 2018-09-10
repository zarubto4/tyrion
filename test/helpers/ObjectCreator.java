package helpers;

import models.*;
import utilities.enums.BusinessModel;
import utilities.enums.ExtensionType;
import utilities.enums.ProductEventType;
import utilities.financial.services.ProductService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class ObjectCreator {

    public static Model_Product createProduct() {
        Model_PaymentDetails paymentDetails = new Model_PaymentDetails();
        paymentDetails.save();

        Model_Product product = new Model_Product();
        product.payment_details = paymentDetails;
        product.save();
        setupProduct(product);
        product.save();

        return product;
    }

    public static void setupProduct(Model_Product product) {
        product.name = "name";
        product.description = "description";
        product.active = true;
        product.business_model = BusinessModel.SAAS;
        product.credit = new BigDecimal(1000);
    }

    public static Model_Invoice createInvoice(Model_Product product) {
        Model_Invoice invoice= new Model_Invoice();
        return invoice;
    }

    /**
     * @return new product extension, without any side effects when {@link ProductService} is used, no activation
     */
    public static Model_ProductExtension createProductExtension(Model_Product product, Date time) {
        Model_ProductExtension extension = new Model_ProductExtension();
        setupExtension(extension, product, time);
        extension.save();

        return extension;
    }

    public static void setupExtension(Model_ProductExtension extension, Model_Product product, Date time) {
        extension.product = product;
        extension.name = "Extension 1";
        extension.description = "description extension 1";
        extension.type = ExtensionType.PROJECT;
        extension.deleted = false;
        extension.color = "blue-madison";
        extension.configuration = "{\"price\":1000,\"count\":100}";
        extension.created = time;
        extension.updated = time;
    }

    /**
     * @return new product extension, without any side effects when {@link ProductService} is used
     */
    public static Model_ProductExtension createAndActivateProductExtension(Model_Product product, Date time) {
        Model_ProductExtension extension = createProductExtension(product, time);

        extension.saveEvent(time, ProductEventType.EXTENSION_CREATED, null);
        extension.saveEvent(time, ProductEventType.EXTENSION_ACTIVATED, null);

        return extension;
    }

    public static Model_ExtensionFinancialEvent createFinanciaEvent(Model_ProductExtension extension, int daysAgo, boolean save) {
        Model_ExtensionFinancialEvent financialEvent = new Model_ExtensionFinancialEvent();
        financialEvent.product_extension = extension;
        financialEvent.event_start = Date.from(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        financialEvent.event_end = Date.from(Instant.now().minus(daysAgo -1, ChronoUnit.DAYS));
        financialEvent.created = financialEvent.event_end;

        if(save) {
            financialEvent.save();
        }

        return financialEvent;
    }
}
