package utilities.financial.extensions;

import play.Configuration;
import play.libs.Json;
import utilities.Server;
import utilities.financial.extensions.configurations.Configuration_Project;

public class Extension_Project implements Extension {

    public static final String name = Configuration.root().getString("Financial.extensions.project.name");
    public static final String description = Configuration.root().getString("Financial.extensions.project.description");
    public static final Long price = Configuration.root().getLong("Financial.extensions.project.price");

    /*
     !!!Important!!!
     Final calculated price must be divided by Server.financial_spendDailyPeriod.
      */
    public Long getActualPrice(Object configuration) {

        return getDailyPrice(configuration) / Server.financial_spendDailyPeriod;
    }

    public Long getDailyPrice(Object configuration) {

        Configuration_Project project = (Configuration_Project) configuration;

        if (project == null) {
            System.out.println("getDailyPrice: configuration is null");
            return null;
        }
        else System.out.println("getDailyPrice: configuration: " + Json.toJson(configuration).toString());

        System.out.println("getDailyPrice: price: " + project.price);
        System.out.println("getDailyPrice: count: " + project.count);
        System.out.println("getDailyPrice: result: " + (project.price * project.count));

        return project.price * project.count;
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