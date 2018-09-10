package utilities.financial.extensions.consumptions;

import play.data.validation.Constraints;

public class Consumption_Database implements ResourceConsumption {

    @Constraints.Required
    @Constraints.Min(0)
    public Long minutes;

    @Override
    public boolean isEmpty() {
        return minutes == 0;
    }

    public String toReadableString() {
        return minutes + " min";
    }
}
