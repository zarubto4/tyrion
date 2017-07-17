package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Customer_New", description = "Json Model for creating new company.")
public class Swagger_Customer_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value = "The street must have at least 4 characters")
    public String street;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required: always. But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The city must have at least 2 characters")
    @ApiModelProperty(required = true, value = "The city must have at least 2 characters")
    public String city;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The zip_code must have at least 5 digits")
    @ApiModelProperty(required = true, value = "The zip_code must have at least 5 digits")
    public String zip_code;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The country must have at least 4 characters")
    @ApiModelProperty(required = true, value = "The country must have at least 4 characters")
    public String country;

    @ApiModelProperty(required = false, value = "The company_registration_no must have at least 4 characters - FOR non-EU countries, " +
            "for Business account is required registration_no OR vat_number")
    public String registration_no;

    @ApiModelProperty(required = false, value =  "Required: only if account is business & from EU!!! CZ28496639 " +
            "for Business account is required registration_no OR vat_number")
    public String vat_number;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "The company_name must have at least 4 characters")
    public String company_name;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "The company_authorized_email must have at least 4 characters")
    public String company_authorized_email;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "The company_authorized_phone must have at least 4 characters")
    public String company_authorized_phone;

    @ApiModelProperty(required = false)
    public String company_web;

    @Constraints.Required
    @Constraints.Email
    @ApiModelProperty(required = true, value = "Email must be valid")
    public String invoice_email;

    public List<ValidationError> validate(){

        List<ValidationError> errors = new ArrayList<>();

        if (vat_number == null && registration_no == null) {

            errors.add(new ValidationError("vat_number","Required if registration_no is null. Fill at least one."));
            errors.add(new ValidationError("registration_no","Required if vat_number is null. Fill at least one."));
        }

        if (company_web != null) {

            if(!company_web.contains("www.")) {
                if (!company_web.contains("http://"))
                    company_web = "http://www." + company_web;
            } else if (!company_web.contains("http://")) {
                company_web = "http://" + company_web;
            }

            try {
                new URL(company_web);
            } catch (MalformedURLException e) {
                errors.add(new ValidationError("company_web","URL: " + company_web + "is invalid"));
            }
        }

        return errors.isEmpty() ? null : errors;
    }
}
