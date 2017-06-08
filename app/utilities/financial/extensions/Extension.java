package utilities.financial.extensions;

import models.Model_ProductExtension.Config;

/**
 * Defines common behavior of every product Extension
 */
public interface Extension {

    /**
     * Should only divide the calculated price by Server.financial_spendDailyPeriod.
     * @param config The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getActualPrice(Config config);

    /**
     * Calculates the price of the extension for one day.
     * @param config The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getDailyPrice(Config config);

    /**
     * Gets the default monthly price based on the type of the extension.
     * @return Long default daily price multiplied by thirty.
     */
    Long getDefaultMonthlyPrice();

    /**
     * Gets the default daily price based on the type of the extension.
     * @return Long default daily price.
     */
    Long getDefaultDailyPrice();

    Integer getDefaultCount();

    String getName();

    String getDescription();
}
