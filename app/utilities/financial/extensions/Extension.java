package utilities.financial.extensions;

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
     * Gets the default monthly price based on the type of the extension.
     * Method should not serve for calculating the price,
     * on the contrary it should be called just for information purposes.
     * @return Long default daily price multiplied by thirty.
     */
    Long getDefaultMonthlyPrice();

    /**
     * Gets the default daily price based on the type of the extension.
     * Called when creating new ProductExtension.
     * @return Long default daily price.
     */
    Long getDefaultDailyPrice();

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
