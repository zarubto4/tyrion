package utilities.financial;

import models.Model_ProductExtension.Config;

public interface Extension {

    Long getPrice(Config config);

    Long getDefaultMonthlyPrice();

    Long getDefaultDailyPrice();

    Integer getDefaultCount();

    boolean isActive();

    String getName();

    String getDescription();
}
