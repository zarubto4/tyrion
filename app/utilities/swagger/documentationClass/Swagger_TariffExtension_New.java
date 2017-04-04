package utilities.swagger.documentationClass;

import play.data.validation.Constraints;
import utilities.enums.Enum_ExtensionType;

public class Swagger_TariffExtension_New {

    @Constraints.Required
    public String id;

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String color;

    @Constraints.Required
    public boolean included;

    @Constraints.Required
    public Enum_ExtensionType type;

    @Constraints.Required
    public Double price;

    @Constraints.Required
    public Integer count;
}
