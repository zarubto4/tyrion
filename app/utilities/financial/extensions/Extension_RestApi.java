package utilities.financial.extensions;

import models.Model_ProductExtension.Config;
import utilities.enums.Enum_ExtensionType;

public class Extension_RestApi implements Extension {

    public static final String name = Enum_ExtensionType.RestApi.name();
    public static final String description = "This is an extension for REST API requests from Blocko.";
    public static final Long price = (long) 100;
    public static final Integer count = 5;

    public Long getPrice(Config config) {

        return config.price * config.count;
    }

    public Long getDefaultMonthlyPrice() {
        return price * 30;
    }

    public Long getDefaultDailyPrice() {
        return price;
    }

    public Integer getDefaultCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
