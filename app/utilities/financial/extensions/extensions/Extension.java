package utilities.financial.extensions.extensions;

import utilities.enums.Enum_ExtensionType;

/**
 * Defines common behavior of every product Extension.
 */
public interface Extension {

    /**
     * Should only divide the calculated price by Server.financial_spendDailyPeriod.
     * This method should be called when the credit is spent.
     * @param configuration The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getActualPrice(Object configuration);

    /**
     * Calculates the price of the extension for one day.
     * Typically used for invoice items prices.
     * @param configuration The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getDailyPrice(Object configuration);

    /**
     * Gets the plain price from configuration without any calculation.
     * @param configuration The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getConfigPrice(Object configuration);


    /**
     * Gets the name of an extension loaded from application.conf.
     * @return The String name of the Extension.
     */
    Enum_ExtensionType getType();

    /**
     * Gets the name of an extension loaded from application.conf.
     * @return The String name of the Extension.
     */
    String getName();

    /**
     * Gets the description of an extension loaded from application.conf.
     * @return The String description of the Extension.
     */
    String getDescription();


}
