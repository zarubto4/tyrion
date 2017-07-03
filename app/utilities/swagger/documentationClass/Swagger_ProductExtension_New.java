package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.Enum_ExtensionType;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for creating new extension of product.",
        value = "ProductExtension_New")
public class Swagger_ProductExtension_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Id of product to extend")
    public String product_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Name of extension")
    public String name;

    @Constraints.MaxLength(value = 255)
    @ApiModelProperty(required = false, value = "Description must not have more than 255 characters")
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Color of extension")
    public String color;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public Enum_ExtensionType type;

    @ApiModelProperty(required = false, value = "For extensions 'project', 'rest_api', 'instance'. Count of projects or available requests etc.")
    public Long count;

    @ApiModelProperty(required = false, value = "For 'support' extension, if it is 24/7.")
    public boolean nonstop;

    @ApiModelProperty(hidden = true, value = "Whether the extension should be included in tariff or not. Only for Tyrion front-end.")
    public boolean included;

    @ApiModelProperty(hidden = true)
    public Double price;

    public List<ValidationError> validate(){

        List<ValidationError> errors = new ArrayList<>();

        switch (type) {

            case project:{

                if (count == null) errors.add(new ValidationError("count", "This field is required, if the type is 'project'."));

                break;
            }

            case database:{

                break;
            }

            case log:{

                break;
            }

            case rest_api:{

                if (count == null) errors.add(new ValidationError("count", "This field is required, if the type is 'rest_api'."));

                break;
            }

            case support:{

                break;
            }

            case instance:{

                if (count == null) errors.add(new ValidationError("count", "This field is required, if the type is 'instance'."));

                break;
            }

            case homer_server:{

                break;
            }

            case participant:{

                if (count == null) errors.add(new ValidationError("count", "This field is required, if the type is 'instance'."));

                break;
            }

            default: errors.add(new ValidationError("type","Extension type is unknown."));
        }

        return errors.isEmpty() ? null : errors;
    }
}