package utilities.financial.extensions.extensions;

import models.Model_ProductExtension;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.configurations.Configuration_Database;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.consumptions.Consumption_Database;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

public class Extension_Database implements Extension {

    public static final ExtensionType TYPE = ExtensionType.DATABASE;

    @Override
    public ExtensionType getType() {
        return TYPE;
    }

    @Override
    public Consumption_Database getConsumption(Model_ProductExtension extension, Date from, Date to) {
        Consumption_Database consumption = new Consumption_Database();
        consumption.minutes = 0l;
        return consumption;
    }

    @Override
    public List<ExtensionInvoiceItem> getInvoiceItems(Configuration configuration, Collection<ResourceConsumption> consumptions) {
        BigDecimal minutePrice = ((Configuration_Database) configuration).minutePrice;
        long minutes = consumptions.stream().mapToLong(c -> ((Consumption_Database) c).minutes).sum();

        // We invoice for every minute, but we can make the number seem nice!
        ExtensionInvoiceItem invoiceItem;
        if(minutes > 300) {
            BigDecimal quantity = new BigDecimal(minutes).divide(new BigDecimal(60 * 24), new MathContext(3, RoundingMode.DOWN));
            invoiceItem = new ExtensionInvoiceItem("Database xy", quantity, "day", minutePrice.multiply(new BigDecimal( 60 * 24)));

        }
        else {
            invoiceItem = new ExtensionInvoiceItem("Database xy", new BigDecimal(minutes), "min", minutePrice);
        }

        return Arrays.asList(invoiceItem);
    }

    @Override
    public void activate(Model_ProductExtension extension) {

    }

    @Override
    public void deactivate(Model_ProductExtension extension) {

    }

    public static String defaultColour = "green";
 }
