package utilities.financial.extensions.extensions;

import models.Model_ProductExtension;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.configurations.Configuration_Project;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.consumptions.Consumption_Project;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Extension_Project implements Extension {

    public static final ExtensionType TYPE = ExtensionType.PROJECT;

    @Override
    public ExtensionType getType() {
        return TYPE;
    }

    @Override
    public Consumption_Project getConsumption(Model_ProductExtension extension, Date from, Date to) {
        Consumption_Project consumption = new Consumption_Project();
        return consumption;
    }

    @Override
    public List<ExtensionInvoiceItem> getInvoiceItems(Configuration configuration, Collection<ResourceConsumption> consumptions) {
        return Collections.emptyList();
    }

    @Override
    public void activate(Model_ProductExtension extension) {

    }

    @Override
    public void deactivate(Model_ProductExtension extension) {

    }
}