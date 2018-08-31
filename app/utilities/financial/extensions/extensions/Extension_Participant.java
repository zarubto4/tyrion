package utilities.financial.extensions.extensions;

import models.Model_ProductExtension;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.configurations.Configuration_Participant;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.consumptions.Consumption_Participant;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Extension_Participant implements Extension {

    public static final ExtensionType TYPE = ExtensionType.PARTICIPANT;

    @Override
    public ExtensionType getType() {
        return TYPE;
    }

    @Override
    public Consumption_Participant getConsumption(Model_ProductExtension extension, Date from, Date to) {
        Consumption_Participant consumption = new Consumption_Participant();
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
