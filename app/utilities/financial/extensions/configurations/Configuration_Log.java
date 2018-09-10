package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

import java.math.BigDecimal;

public class Configuration_Log implements Configuration {

    @Constraints.Required
    @Constraints.Min(0)
    public Long count;

    @Constraints.Required
    @Constraints.Min(0)
    public BigDecimal price;
}
