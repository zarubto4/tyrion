package utilities.financial;

import models.Model_ProductExtension.Config;

public interface Extension {

    Double getPrice(Config config);

    Double getMonthlyPrice();

    boolean isActive();

    String getName();

    String getDescription();
}
