package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;
import play.db.Database;
import play.libs.Json;

import java.math.BigDecimal;

public class Configuration_Database implements Configuration {

    @Constraints.Required
    @Constraints.Min(0)
    public BigDecimal minutePrice;

    public static Configuration_Database getDefault() {
        Configuration_Database defaultConfiguration = new Configuration_Database();
        defaultConfiguration.minutePrice = defaultMinutePrice;
        return defaultConfiguration;
    }

    private static BigDecimal defaultMinutePrice = new BigDecimal(10);

    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }
}
