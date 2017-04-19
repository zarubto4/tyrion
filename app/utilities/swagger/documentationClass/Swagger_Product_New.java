package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model if user wants to create new Product for projects",
        value = "Product_New")
public class Swagger_Product_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Required: always, Tariff id")
    public String tariff_id;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value =  "Required: always. The name length must be between 4 and 60 characters")
    public String name;


    @ApiModelProperty(required = false, value =  "Required: only in if payment_mode_required is true")
    public Enum_Payment_mode payment_mode;

    @ApiModelProperty(required = false, value =  "Required: only in if payment_method_required is true  values =>[bank_transfer, credit_card]")
    public Enum_Payment_method payment_method;

    @Constraints.MinLength(value = 4, message = "The full_name must have at least 4 characters")
    @ApiModelProperty(required = false, value =  "Can be null")
    public String full_name;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" +
            "The street must have at least 4 characters")
    public String street;

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Required: always" +
            "But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The city must have at least 2 characters")
    @ApiModelProperty(required = true, value =  "Required: always" +
            "The city must have at least 2 characters")
    public String city;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The zip_code must have at least 5 digits")
    @ApiModelProperty(required = true, value =  "Required: always" +
            "The zip_code must have at least 5 digits")
    public String zip_code;


    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The country must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" +
            "The country must have at least 4 characters")
    public String country;


    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_registration_no must have at least 4 characters - FOR non-EU countries" +
            "for Business account is required registration_no OR vat_number")
    public String registration_no;

    @ApiModelProperty(required = false, value =  "Required: only if account is business & from EU!!! CZ28496639 " +
            "The VAT_number must have at least 4 characters" +
            "for Business account is required registration_no OR vat_number")
    public String vat_number;


    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_name must have at least 4 characters")
    public String company_name;


    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_authorized_email must have at least 4 characters")
    public String company_authorized_email;

    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_authorized_phone must have at least 4 characters")
    public String company_authorized_phone;

    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_web must have at least 4 characters")
    public String company_web;

    @Constraints.Required
    @Constraints.Email
    @ApiModelProperty(required = true, value =  "Required: always, Email must be valid")
    public String invoice_email;


    @ApiModelProperty(required = false, value = "List of Ids of Extensions")
    public List<String> extension_ids = new ArrayList<>();

    @ApiModelProperty(hidden = true)
    public String person_id;      // For administration, when creating product for another user
}
