package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model if user want create new Tariff for projects",
        value = "Tariff_Register")
public class Swagger_Tariff_Register {


    @Constraints.Required
    public String tariff_type;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The tariff_individual_name must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always." +
                                                "The street must have at least 4 characters")
    public String product_individual_name;

    @Constraints.Required
    @Constraints.MinLength(value = 3)
    @Constraints.MaxLength(value = 3)
    public String currency_type;


    @ApiModelProperty(required = true, value =  "Required: only in if required_payment_mode is true")
    public String payment_mode;

    @ApiModelProperty(required = true, value =  "Required: only in if required_payment_mode is true  values =>[bank, credit_card]")
    public String payment_method;


    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" +
                                                "The street must have at least 4 characters")
    public String street;

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Required: always" +
                                                "But The street_number can be empty")
    public String street_number;

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The city must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always" +
                                                "The city must have at least 4 characters")
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
                                                 "The company_registration_no must have at least 4 characters")
    public String registration_no;


    @ApiModelProperty(required = false, value =  "Required: only if account is business & from EU!!! CZ28496639 " +
                                                 "The VAT_number must have at least 4 characters")
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

    @Constraints.Email()
    @ApiModelProperty(required = false, value =  "Required: only if account is business" +
                                                 "Email must be valid")
    public String company_invoice_email;

}
