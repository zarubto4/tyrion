package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
@ApiModel(description = "Json Model with List of Board ID",
          value = "ContactUpdate")
public class Swagger_Contact_Update implements Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" + "The name must have at least 4 characters")
    public String name;

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Required: always")
    public String street;

    @ApiModelProperty(required = true, value =  "Required: always" + "But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Required: always")
    public String city;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The zip_code must have at least 5 digits")
    @ApiModelProperty(required = true, value =  "Required: always" + "The zip_code must have at least 5 digits")
    public String zip_code;


    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The country must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" + "The country must have at least 4 characters")
    public String country;


    @ApiModelProperty(required = false, value =  "Required: only if user want have business account")
    public boolean company_account;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_registration_no must have at least 4 characters")
    public String company_registration_no;


    @ApiModelProperty(required = false, example = "CZ12345678", value = "Required: only if company_account = true And VAT_number is required only for EU Customers." + "The VAT_number must have at least 4 characters")
    public String company_vat_number;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The name must have at least 4 characters")
    public String company_name;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_authorized_email must have at least 4 characters")
    public String company_authorized_email;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_authorized_phone must have at least 4 characters")
    public String company_authorized_phone;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_web url must be valid ")
    public String company_web;

    @Constraints.Required
    @Constraints.Email
    @ApiModelProperty(required = true, value =  "Required: always. Email must be valid")
    public String invoice_email;

    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (company_account) {
            if (company_registration_no == null && company_vat_number == null) {
                errors.add(new ValidationError("registration_no","Required if company_account = true and vat_number = null."));
                errors.add(new ValidationError("vat_number","Required if company_account = true and registration_no = null."));
            }
            if (company_name == null) errors.add(new ValidationError("name","Required if company_account = true."));
            if (company_authorized_email == null) errors.add(new ValidationError("company_authorized_email","Required if company_account = true."));
            if (company_authorized_phone == null) errors.add(new ValidationError("company_authorized_phone","Required if company_account = true."));
            if (company_web == null) errors.add(new ValidationError("company_web","Required if company_account = true."));
            if (company_name == null) errors.add(new ValidationError("name","Required if company_account = true."));
        }

        return errors.isEmpty() ? null : errors;
    }
}
