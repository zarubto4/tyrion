package utilities.financial.extensions.extensions;

import play.Configuration;
import utilities.Server;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.configurations.Configuration_Database;

public class Extension_Database implements Extension {

    public static final ExtensionType enum_type = ExtensionType.database;
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

        Configuration_Database database = ((Configuration_Database) configuration);

        return database.price;
    }

    public Long getConfigPrice(Object configuration) {

        Configuration_Database database = ((Configuration_Database) configuration);

        return database.price;
    }

    public ExtensionType getType() {
        return enum_type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
