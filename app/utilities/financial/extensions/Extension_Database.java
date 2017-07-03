package utilities.financial.extensions;

import play.Configuration;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_Database;

public class Extension_Database implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.database.name");
    public static final String description = Configuration.root().getString("Financial.extensions.database.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.database.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_Database database = ((Configuration_Database) configuration);

        return database.price;
    }

    public Long getConfigPrice(Object configuration) {

        Configuration_Database database = ((Configuration_Database) configuration);

        return database.price;
    }

    public Long getDefaultMonthlyPrice() {
        return price * 30;
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
