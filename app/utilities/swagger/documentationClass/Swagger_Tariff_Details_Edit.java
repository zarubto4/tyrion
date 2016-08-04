package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_Tariff_Details_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" + "The street must have at least 4 characters")
    public String street;

    @ApiModelProperty(required = true, value =  "Required: always" + "But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The city must have at least 4 characters")
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


    @ApiModelProperty(required = true, value =  "Required: only if user want have business account")
    public boolean company_account;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_registration_no must have at least 4 characters")
    public String registration_no;


    @ApiModelProperty(required = false, example = "CZ28496639", value = "Required: only if company_account = true And VAT_number is required only for EU Customers." + "The VAT_number must have at least 4 characters")
    public String vat_number;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_name must have at least 4 characters")
    public String company_name;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_authorized_email must have at least 4 characters")
    public String company_authorized_email;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_authorized_phone must have at least 4 characters")
    public String company_authorized_phone;


    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "The company_web url must be valid ")
    public String company_web;


    @Constraints.Email()
    @ApiModelProperty(required = false, value =  "Required: only if company_account = true" + "Email must be valid")
    public String company_invoice_email;

}
