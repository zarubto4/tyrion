package utilities.financial;

import models.Model_ProductExtension.Config;
import utilities.enums.Enum_ExtensionType;

public class Extension_Project implements Extension {

    public static final String name = Enum_ExtensionType.Project.name();
    public static final String description = "This is an extension for project.";
    public static final Double price = 0.2;

    public Double getPrice(Config config) {

        return config.price * config.count;
    }

    public Double getMonthlyPrice() {
        return price * 30;
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
