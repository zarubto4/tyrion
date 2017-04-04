package utilities.financial;

import models.Model_ProductExtension.Config;

public class Extension_Log implements Extension {

    public static final String name = "Log";
    public static final String description = "This is an extension for log.";
    public static final Double price = 0.6;
    public static final Integer count = 1;

    public Double getPrice(Config config) {

        // here some complicated logic which finds out, how much will this extension cost

        return config.price * config.count;
    }

    public Double getDefaultMonthlyPrice() {
        return price *  30;
    }

    public Double getDefaultDailyPrice() {
        return price;
    }

    public Integer getDefaultCount() {
        return count;
    }

    public boolean isActive() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
