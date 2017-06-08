package utilities.financial.extensions;

import models.Model_ProductExtension.Config;
import play.Configuration;
import utilities.Server;

public class Extension_Log implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.log.name");
    public static final String description = Configuration.root().getString("Financial.extensions.log.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.log.price");
    public static final Integer count = 1;

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Config config) {

        return getDailyPrice(config) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Config config) {

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
