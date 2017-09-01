package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;

import javax.persistence.*;

@Entity
@ApiModel(description = "Model of Payment_Details",
        value = "Payment_Details")
@Table(name="PaymentDetails")
public class Model_PaymentDetails extends Model {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Model_PaymentDetails.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                           @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @OneToOne                                                                           public Model_Customer customer;
    @JsonIgnore @OneToOne() @JoinColumn(name="productidpaymentdetails")                             public Model_Product product;

                                                                                                    public boolean company_account; // Rozhoduji se zda jde o detaily firemní nebo osobní

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_name;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_email;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_phone;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_web;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_registration_no;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_vat_number;

    @ApiModelProperty(required = false,value = "Used for billing and invoices") public String full_name;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String street;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String street_number;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String city;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String zip_code;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String country;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String invoice_email;
    @ApiModelProperty(required = false,value = "Used for billing and invoices") public String bank_account;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static boolean control_vat_number(String vat_number){

            // Jestli je přítomné VAT number - musí dojít ke kontrole validity Vat number!
            switch (vat_number.substring(0,2)){
                case "BE" : {return true;}
                case "BG" : {return true;}
                case "CZ" : {return true;}
                case "DK" : {return true;}
                case "EE" : {return true;}
                case "FI" : {return true;}
                case "FR" : {return true;}
                case "IE" : {return true;}
                case "IT" : {return true;}
                case "CY" : {return true;}
                case "LT" : {return true;}
                case "LV" : {return true;}
                case "LU" : {return true;}
                case "HU" : {return true;}
                case "MT" : {return true;}
                case "DE" : {return true;}
                case "NL" : {return true;}
                case "PT" : {return true;}
                case "AT" : {return true;}
                case "RO" : {return true;}
                case "EL" : {return true;}
                case "SK" : {return true;}
                case "SI" : {return true;}
                case "GB" : {return true;}
                case "ES" : {return true;}
                case "SE" : {return true;}
                default: {return false;}
            }
    }

    @JsonIgnore
    public Model_PaymentDetails copy(){

        Model_PaymentDetails details = new Model_PaymentDetails();
        details.full_name       = this.full_name;
        details.street          = this.street;
        details.street_number   = this.street_number;
        details.city            = this.city;
        details.zip_code        = this.zip_code;
        details.country         = this.country;
        details.invoice_email   = this.invoice_email;

        if (company_account) {

            details.company_account             = true;
            details.company_name                = this.company_name;
            details.company_web                 = this.company_web;
            details.company_authorized_email    = this.company_authorized_email;
            details.company_authorized_phone    = this.company_authorized_phone;
            details.company_vat_number          = this.company_vat_number;
            details.company_registration_no     = this.company_registration_no;
        }

        return details;
    }

    @JsonIgnore
    public boolean isComplete(){
        return full_name != null && street != null && street_number != null && city != null && zip_code != null && country != null && invoice_email != null;
    }

    @JsonIgnore
    public boolean isCompleteCompany(){
        return street != null && street_number != null && city != null && zip_code != null && country != null && invoice_email != null
                && company_name != null && company_authorized_email != null && company_authorized_phone != null && (company_vat_number != null || company_registration_no != null);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore     public boolean create_permission()  { return true; }
    @JsonProperty   public boolean edit_permission()    { return true; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_PaymentDetails get_byId(Long id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<Long,Model_PaymentDetails> find = new Finder<>(Model_PaymentDetails.class);


}
