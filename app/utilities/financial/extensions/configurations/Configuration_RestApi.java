package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

public class Configuration_RestApi implements Configuration {

    @Constraints.Required
    @Constraints.Min(0)
    public Long available_requests;

    @Constraints.Required
    @Constraints.Min(0)
    public Long price;
}
