package utilities.financial.extensions;

import play.Configuration;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_RestApi;

public class Extension_RestApi implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.rest_api.name");
    public static final String description = Configuration.root().getString("Financial.extensions.rest_api.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.rest_api.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_RestApi restApi = ((Configuration_RestApi) configuration);

        return restApi.price * restApi.available_requests;
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
