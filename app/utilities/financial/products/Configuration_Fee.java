package utilities.financial.products;

import play.data.validation.Constraints;

public class Configuration_Fee {

    @Constraints.Required
    public Long fee;

    @Constraints.Min(1)
    @Constraints.Max(28)
    public Integer day_of_month;

    @Constraints.Min(1)
    @Constraints.Max(365)
    public Integer day_of_year;
}
