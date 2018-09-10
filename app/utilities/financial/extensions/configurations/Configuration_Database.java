package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;
import play.db.Database;

import java.math.BigDecimal;

public class Configuration_Database implements Configuration {

    @Constraints.Required
    @Constraints.Min(0)
    public BigDecimal minutePrice;
}
