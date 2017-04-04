package utilities.financial;

import models.Model_ProductExtension.Config;
import utilities.enums.Enum_ExtensionType;

public class Extension_Database implements Extension {

    public static final String name = Enum_ExtensionType.Database.name();
    public static final String description = "This is an extension for database.";
    public static final Double price = 0.7;
    public static final Integer count = 1;

    public Double getPrice(Config config) {

        return config.price * config.count;
    }

    public Double getDefaultMonthlyPrice() {
        return price * 30;
    }

    public Double getDefaultDailyPrice() {
        return price;
    }

    public Integer getDefaultCount() {
        return count;
    }

    public boolean isActive() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
