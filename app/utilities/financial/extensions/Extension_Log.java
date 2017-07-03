package utilities.financial.extensions;

import play.Configuration;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_Log;

public class Extension_Log implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.log.name");
    public static final String description = Configuration.root().getString("Financial.extensions.log.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.log.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_Log log = ((Configuration_Log) configuration);

        return log.price * log.count;
    }

    public Long getConfigPrice(Object configuration) {

        Configuration_Log log = ((Configuration_Log) configuration);

        return log.price;
    }

    public Long getDefaultMonthlyPrice() {
        return price *  30;
    }

    public Long getDefaultDailyPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
