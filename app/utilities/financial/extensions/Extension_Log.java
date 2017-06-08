package utilities.financial.extensions;

import models.Model_ProductExtension.Config;
import play.Configuration;
import utilities.Server;

public class Extension_Log implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.log.name");
    public static final String description = Configuration.root().getString("Financial.extensions.log.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.log.price") / Server.financial_spendDailyPeriod;
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
