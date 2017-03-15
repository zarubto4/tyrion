package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;


@Entity
@ApiModel(description = "Model of Payment_Details",
        value = "Payment_Details")
public class Model_PaymentDetails extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                           @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @ManyToOne()                                                                        public Model_Person person;
    @JsonIgnore @OneToOne(cascade = CascadeType.ALL)   @JoinColumn(name="productidpaymentdetails")  public Model_Product product;

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


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonProperty @Transient  public boolean edit_permission()  {  return true;  }


    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<Long,Model_PaymentDetails> find = new Finder<>(Model_PaymentDetails.class);


}
