package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

public class Configuration_Support implements Configuration {

    public boolean nonstop;

    @Constraints.Required
    @Constraints.Min(0)
    public Long price;
}
