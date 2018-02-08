package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.PaymentMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model if user wants to create new Product for projects",
        value = "Product_New")
public class Swagger_Product_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Id of a selected tariff.")
    public UUID tariff_id;

    @ApiModelProperty(required = false, value =  "If null new customer is created. Customer can be company or single person.")
    public UUID customer_id;

    @ApiModelProperty(required = false, value =  "If product is for someone else and you are an integrator.")
    public boolean integrator_registration = false;

    @ApiModelProperty(required = false, value = "Allowable values =>[BANK_TRANSFER, CREDIT_CARD], default: CREDIT_CARD")
    public PaymentMethod payment_method = PaymentMethod.CREDIT_CARD;

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
    public String company_registration_no;

    @Constraints.MinLength(value = 3, message = "The vat_number must have at least 3 characters")
    @ApiModelProperty(required = false, value = "Required: only if account is business & from EU!!! CZ28496639 " +
            "The vat_number must have at least 3 characters" +
            "for Business account is required registration_no OR vat_number")
    public String company_vat_number;

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
    public List<UUID> extension_ids = new ArrayList<>();

}