package utilities.financial;

import models.Model_ProductExtension.Config;
import utilities.enums.Enum_ExtensionType;

public class Extension_Project implements Extension {

    public static final String name = Enum_ExtensionType.Project.name();
    public static final String description = "This is an extension for project.";
    public static final Long price = (long) 200;
    public static final Integer count = 5;

    public Long getPrice(Config config) {

        return config.price * config.count;
    }

    public Long getDefaultMonthlyPrice() {
        return price * 30;
    }

    public Long getDefaultDailyPrice() {
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
