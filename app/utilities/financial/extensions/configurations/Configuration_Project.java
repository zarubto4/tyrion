package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;

public class Configuration_Project {

    @Constraints.Required
    public Long count;

    @Constraints.Required
    public Long price;
}
