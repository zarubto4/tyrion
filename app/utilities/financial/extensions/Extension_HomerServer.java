package utilities.financial.extensions;

import play.Configuration;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_HomerServer;

public class Extension_HomerServer implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.homer_server.name");
    public static final String description = Configuration.root().getString("Financial.extensions.homer_server.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.homer_server.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_HomerServer homerServer = ((Configuration_HomerServer) configuration);

        return homerServer.price;
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
