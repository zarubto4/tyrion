package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.Enum_Payment_method;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
          value = "PaymentDetails_New")
public class Swagger_PaymentDetails_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" + "The street must have at least 4 characters")
    public String street;

    @ApiModelProperty(required = true, value =  "Required: always" + "But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The city must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" + "The city must have at least 4 characters")
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
    public String registration_no;


    @ApiModelProperty(required = false, example = "CZ12345678", value = "Required: only if company_account = true And VAT_number is required only for EU Customers." + "The VAT_number must have at least 4 characters")
    public String vat_number;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_name must have at least 4 characters")
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

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Payment method.")
    public Enum_Payment_method method;


    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (company_account) {
            if(registration_no == null && vat_number == null) {
                errors.add(new ValidationError("registration_no","Required if company_account = true and vat_number = null."));
                errors.add(new ValidationError("vat_number","Required if company_account = true and registration_no = null."));
            }
            if (company_name == null) errors.add(new ValidationError("company_name","Required if company_account = true."));
            if (company_authorized_email == null) errors.add(new ValidationError("company_authorized_email","Required if company_account = true."));
            if (company_authorized_phone == null) errors.add(new ValidationError("company_authorized_phone","Required if company_account = true."));
            if (company_web == null) errors.add(new ValidationError("company_web","Required if company_account = true."));
            if (company_name == null) errors.add(new ValidationError("company_name","Required if company_account = true."));
        }

        if (method != Enum_Payment_method.credit_card && method != Enum_Payment_method.bank_transfer)
            errors.add(new ValidationError("method","Allowable values: 'credit_card', 'bank_transfer'"));

        return errors.isEmpty() ? null : errors;
    }
}
