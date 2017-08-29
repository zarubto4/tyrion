package utilities.financial.extensions.extensions;

import play.Configuration;
import utilities.Server;
import utilities.enums.Enum_ExtensionType;
import utilities.financial.extensions.configurations.Configuration_RestApi;

public class Extension_RestApi implements Extension {

    public static final Enum_ExtensionType enum_type = Enum_ExtensionType.rest_api;
    public static final String name = Configuration.root().getString("Financial.extensions." + enum_type.name() + ".name");
    public static final String description = Configuration.root().getString("Financial.extensions." + enum_type.name() + ".description");

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

    public Long getConfigPrice(Object configuration) {

        Configuration_RestApi restApi = ((Configuration_RestApi) configuration);

        return restApi.price;
    }
    public Enum_ExtensionType getType() {
        return enum_type;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
