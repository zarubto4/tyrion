package utilities.financial.extensions;

import models.Model_ProductExtension.Config;
import play.Configuration;
import utilities.Server;

public class Extension_Project implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.project.name");
    public static final String description = Configuration.root().getString("Financial.extensions.project.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.project.price") / Server.financial_spendDailyPeriod;
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
