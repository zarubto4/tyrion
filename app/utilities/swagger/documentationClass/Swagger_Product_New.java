package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;
import play.data.validation.Constraints;
import utilities.enums.Enum_Payment_method;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model if user wants to create new Product for projects",
        value = "Product_New")
public class Swagger_Product_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Id of a selected tariff.")
    public String tariff_id;

    @ApiModelProperty(required = false, value =  "If null new customer is created. Customer can be company or single person.")
    public String customer_id;

    @ApiModelProperty(required = false, value =  "If product is for someone else and you are an integrator.")
    public boolean integration;

    @ApiModelProperty(required = false, value =  "If there is an existing customer with payment details, those details will be used.")
    public boolean default_payment_details;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value =  "The name length must be between 4 and 60 characters")
    public String name;

    @ApiModelProperty(required = false, value = "Allowable values =>[bank_transfer, credit_card], default: credit_card")
    public Enum_Payment_method payment_method = Enum_Payment_method.credit_card;

    @Constraints.MinLength(value = 4, message = "The full_name must have at least 4 characters")
    @ApiModelProperty(required = false)
    public String full_name;

    @Constraints.MinLength(value = 4, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = false, value = "The street must have at least 4 characters")
    public String street;

    @ApiModelProperty(required = false)
    public String street_number;

    @Constraints.MinLength(value = 2, message = "The city must have at least 2 characters")
    @ApiModelProperty(required = false, value = "The city must have at least 2 characters")
    public String city;

    @Constraints.MinLength(value = 3, message = "The zip_code must have at least 3 characters")
    @ApiModelProperty(required = false, value = "The zip_code must have at least 3 characters")
    public String zip_code;

    @Constraints.MinLength(value = 2, message = "The country must have at least 2 characters")
    @ApiModelProperty(required = false, value = "The country must have at least 2 characters")
    public String country;

    @Constraints.MinLength(value = 3, message = "The registration_no must have at least 3 characters")
    @ApiModelProperty(required = false, value = "Required: only if account is business" +
            "The registration_no must have at least 3 characters - FOR non-EU countries" +
            "for Business account is required registration_no OR vat_number")
    public String registration_no;

    @Constraints.MinLength(value = 3, message = "The vat_number must have at least 3 characters")
    @ApiModelProperty(required = false, value = "Required: only if account is business & from EU!!! CZ28496639 " +
            "The vat_number must have at least 3 characters" +
            "for Business account is required registration_no OR vat_number")
    public String vat_number;

    @ApiModelProperty(required = false, value = "The company_name must have at least 2 characters")
    @Constraints.MinLength(value = 2, message = "The company_name must have at least 2 characters")
    public String company_name;

    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
            "The company_authorized_email must have at least 4 characters")
    public String company_authorized_email;

    @ApiModelProperty(required = false, value = "Required: only if account is business" +
            "The company_authorized_phone must have at least 4 characters")
    public String company_authorized_phone;

    @ApiModelProperty(required = false, value = "Required: only if account is business" +
            "The company_web must have at least 4 characters")
    public String company_web;

    @Constraints.Email
    @ApiModelProperty(required = false, value =  "Required: always, Email must be valid")
    public String invoice_email;

    @ApiModelProperty(required = false, value = "List of Ids of Extensions")
    public List<String> extension_ids = new ArrayList<>();

    @ApiModelProperty(hidden = true)
    public String person_id;      // For administration, when creating product for another user
}