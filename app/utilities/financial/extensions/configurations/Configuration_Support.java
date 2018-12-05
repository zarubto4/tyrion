package utilities.financial.extensions.configurations;

import play.data.validation.Constraints;
import play.libs.Json;

public class Configuration_Support implements Configuration {

    public boolean nonstop;

    @Constraints.Required
    @Constraints.Min(0)
    public Long price;

    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }
}
