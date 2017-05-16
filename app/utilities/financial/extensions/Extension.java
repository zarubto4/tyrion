package utilities.financial.extensions;

import models.Model_ProductExtension.Config;

/**
 * Defines common behavior of every product Extension
 */
public interface Extension {

    /**
     * Counts the actual price of the extension.
     * @param config The configuration of given extension.
     * @return Long price based on the type of the extension and individual configuration.
     */
    Long getPrice(Config config);

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
