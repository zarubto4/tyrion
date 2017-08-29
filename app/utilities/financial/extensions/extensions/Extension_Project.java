package utilities.financial.extensions.extensions;

import play.Configuration;
import utilities.Server;
import utilities.enums.Enum_ExtensionType;
import utilities.financial.extensions.configurations.Configuration_Project;

public class Extension_Project implements Extension {

    public static final Enum_ExtensionType enum_type = Enum_ExtensionType.project;
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

        Configuration_Project project = (Configuration_Project) configuration;

        return project.price;
    }

    public Long getConfigPrice(Object configuration) {

        Configuration_Project project = (Configuration_Project) configuration;

        return project.price;
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