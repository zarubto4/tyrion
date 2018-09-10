package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum ProductEventType {
    @EnumValue("PRODUCT_CREATED")             PRODUCT_CREATED(null, "Product created"),
    @EnumValue("PRODUCT_DELETED")             PRODUCT_DELETED(null, "Product deleted"),

    @EnumValue("INVOICE_CREATED")             INVOICE_CREATED(ProductEventReferenceType.INVOICE, "Invoice created",  ProductEventTypeReadPermission.ADMIN),
    @EnumValue("INVOICE_CONFIRMED")           INVOICE_CONFIRMED(ProductEventReferenceType.INVOICE, "Invoice confirmed",  ProductEventTypeReadPermission.ADMIN),
    @EnumValue("INVOICE_ISSUED")              INVOICE_ISSUED(ProductEventReferenceType.INVOICE, "Invoice issued"),
    @EnumValue("INVOICE_PAYMENT_RECEIVED")    INVOICE_PAYMENT_RECEIVED(ProductEventReferenceType.INVOICE, "Payment received"),

    @EnumValue("EXTENSION_CREATED")           EXTENSION_CREATED(ProductEventReferenceType.EXTENSION, "Extension created"),
    @EnumValue("EXTENSION_ACTIVATED")         EXTENSION_ACTIVATED(ProductEventReferenceType.EXTENSION, "Extension activated"),
    @EnumValue("EXTENSION_DEACTIVATED")       EXTENSION_DEACTIVATED(ProductEventReferenceType.EXTENSION, "Extension deactivated"),
    @EnumValue("EXTENSION_DELETED")           EXTENSION_DELETED(ProductEventReferenceType.EXTENSION, "Extension deleted");

    private final String text;

    private final ProductEventReferenceType referenceType;

    private final ProductEventTypeReadPermission defaultReadPermission;

    ProductEventType(ProductEventReferenceType referenceType, String text) {
        this(referenceType, text, ProductEventTypeReadPermission.USER);
    }

    ProductEventType(ProductEventReferenceType referenceType, String text, ProductEventTypeReadPermission defaultReadPermission) {
        this.defaultReadPermission = defaultReadPermission;
        this.referenceType = referenceType;
        this.text = text;
    }

    /**
     * // TODO i18n
     *
     * @return human readable event description
     */
    public String getText() {
        return text;
    }

    public ProductEventReferenceType getReferenceType() {
        return referenceType;
    }

    public ProductEventTypeReadPermission getDefaultReadPermission() {
        return defaultReadPermission;
    }
}
