package utilities.financial.extensions;

import play.Configuration;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_Participant;

public class Extension_Participant implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.participant.name");
    public static final String description = Configuration.root().getString("Financial.extensions.participant.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.participant.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_Participant participant = ((Configuration_Participant) configuration);

        return participant.price * participant.count;
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
