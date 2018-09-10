package utilities.financial.extensions;

import java.math.BigDecimal;

public class ExtensionInvoiceItem {
    private String name;
    private BigDecimal   quantity;
    private String unitName;
    private BigDecimal unitPrice;

    public ExtensionInvoiceItem(String name, BigDecimal quantity, String unitName, BigDecimal unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitName = unitName;
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getUnitName() {
        return unitName;
    }

    public BigDecimal getPriceTotal() {
        return quantity.multiply(unitPrice);
    }
}
