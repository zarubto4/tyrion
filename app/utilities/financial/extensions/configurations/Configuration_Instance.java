package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

public class Configuration_Instance implements Configuration {

    @Constraints.Required
    @Constraints.Min(0)
    public Long count;

    @Constraints.Required
    @Constraints.Min(0)
    public Long price;
}
