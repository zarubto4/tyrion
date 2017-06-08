package utilities.financial.extensions;

import models.Model_ProductExtension.Config;
import play.Configuration;
import utilities.Server;

public class Extension_Database implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.database.name");
    public static final String description = Configuration.root().getString("Financial.extensions.database.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.database.price") / Server.financial_spendDailyPeriod;
    public static final Integer count = 1;

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
