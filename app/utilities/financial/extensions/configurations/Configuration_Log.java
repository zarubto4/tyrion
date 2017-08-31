package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

public class Configuration_Log {

    @Constraints.Required
    @Constraints.Min(0)
    public Long count;

    @Constraints.Required
    @Constraints.Min(0)
    public Long price;
}
