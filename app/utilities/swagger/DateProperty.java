package utilities.swagger;

import io.swagger.models.properties.DateTimeProperty;

public class DateProperty extends DateTimeProperty {

    public DateProperty() {
        super();
        this.type = "integer";
        this.format = "int32";
    }

    public static boolean isType(String type, String format) {
        if ("integer".equals(type) && "int32".equals(format)) {
            return true;
        } else {
            return false;
        }
    }
}
