package utilities.financial.extensions.extensions;

import models.Model_ProductExtension;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.consumptions.ResourceConsumption;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Defines common behavior of every product Extension. <br><br>
 *
 *
 * <ul>
 *     <li>Calculate <b>consumption</b> of a product extension during given time range.
 *         Consumption simply tells us, how many resources was used. Resource might be time, memory etc.
 *     </li>
 *     <li>
 *         Calculate <b>invoice item</b>, including <b>price</b>, from given consumption(s) and extension configuration.
 *         We do not want to calculate consumption all over again, therefore it might be saved and used later.
 *         To inform the user about the used resources or to create an invoice.
 *     </li>
 *     <li>
 *         <b>Activate/deactivate</b> given extension.
 *     </li>
 * </ul>
 */
public interface Extension {

    /**
     * Calculate price of the extension according to its configuration and used resources.
     *
     * @param configuration The configuration of the extension.
     * @param consumptions Consumed resources by the user.
     * @return Price based on the type of the extension, individual configuration and used resources.
     */
    default BigDecimal getPrice(Configuration configuration, List<ResourceConsumption> consumptions) {
        return getInvoiceItems(configuration, consumptions).stream()
                .map(item -> item.getPriceTotal())
                .reduce(BigDecimal::add)
                .get();
    }

    /**
     * Calculate price of the extension according to its configuration and used resources.
     *
     * @param configuration The configuration of the extension.
     * @param consumption Consumed resources by the user.
     * @return Price based on the type of the extension, individual configuration and used resources.
     */
    default BigDecimal getPrice(Configuration configuration, ResourceConsumption consumption) {
        return getPrice(configuration, Arrays.asList(consumption));
    }

    /**
     * Invoice item consists only of very simple items - name, unit, quantity, price per unit.
     * On the other hand, resource consumption can be more complicated and can even result in more
     * invoice items.<br><br>
     *
     * This class serves to covert collection of ResourceConsumption to one or more invoice items.
     *
     * param configuration The configuration of the extension.
     * @param consumptions Consumed resources by the user.
     * @return
     */
    List<ExtensionInvoiceItem> getInvoiceItems(Configuration configuration, Collection<ResourceConsumption> consumptions);

    /**
     * Invoice item consists only of very simple items - name, unit, quantity, price per unit.
     * On the other hand, resource consumption can be more complicated and can even result in more
     * invoice items.<br><br>
     *
     * This class serves to covert collection of ResourceConsumption to one or more invoice items.
     *
     * param configuration The configuration of the extension.
     * @param consumption Consumed resources by the user.
     * @return
     */
    default List<ExtensionInvoiceItem> getInvoiceItems(Configuration configuration, ResourceConsumption consumption) {
        return getInvoiceItems(configuration, Arrays.asList(consumption));
    }

    /**
     * Calculate how much of the source was consumed in the given period. <br>
     * Called periodically, result is saved into the database and history should be retrieved from there.
     *
     * @param extension Extension entity containing information about extension (configuration) and the project which are necessary to calculate the usage.
     * @param from Start time for the calculation.
     * @param to End time for the calculation.
     * @return
     */
    ResourceConsumption getConsumption(Model_ProductExtension extension, Date from, Date to);

    /**
     * @param extension Extension to be activated.
     */
    void activate(Model_ProductExtension extension);

    /**
     * @param extension Extension to be deactivated.
     */
    void deactivate(Model_ProductExtension extension);

    /**
     * Gets the name of an extension loaded from application.conf.
     *
     * @return The String name of the Extension.
     */
    ExtensionType getType();
}
