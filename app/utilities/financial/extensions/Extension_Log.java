package utilities.financial.extensions;

import models.Model_ProductExtension.Config;

public class Extension_Log implements Extension {

    public static final String name = "Log";
    public static final String description = "This is an extension for log.";
    public static final Long price = (long) 600;
    public static final Integer count = 1;

    public Long getPrice(Config config) {

        // here some complicated logic which finds out, how much will this extension cost

        return config.price * config.count;
    }

    public Long getDefaultMonthlyPrice() {
        return price *  30;
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
