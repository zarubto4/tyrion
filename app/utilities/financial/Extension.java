package utilities.financial;

import models.Model_ProductExtension.Config;

public interface Extension {

    Double getPrice(Config config);

    Double getDefaultMonthlyPrice();

    Double getDefaultDailyPrice();

    Integer getDefaultCount();

    boolean isActive();

    String getName();

    String getDescription();
}
