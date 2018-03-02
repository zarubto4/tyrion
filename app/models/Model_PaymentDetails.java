package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Payment_Details",
        value = "Payment_Details")
@Table(name="PaymentDetails")
public class Model_PaymentDetails extends BaseModel {

    // Logger
    private static final Logger logger = new Logger(Model_PaymentDetails.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToOne                                                                           public Model_Customer customer;
    @JsonIgnore @OneToOne() @JoinColumn(name="productidpaymentdetails")                             public Model_Product product;

                                                                                                    public boolean company_account; // Rozhoduji se zda jde o detaily firemní nebo osobní

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_name;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_email;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_phone;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_web;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_registration_no;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_vat_number;

    @ApiModelProperty(required = false,value = "Used for billing and invoices") public String full_name;        // Když nejde o firemní účet
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
    public static boolean control_vat_number(String vat_number) {

            // Jestli je přítomné VAT number - musí dojít ke kontrole validity Vat number!
            switch (vat_number.substring(0,2)) {
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
    public Model_PaymentDetails copy() {

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
    public boolean isComplete() {
        return full_name != null  && !full_name.equals("")
                && street != null  && !street.equals("")
                && street_number != null  && !street_number.equals("")
                && city != null  && !city.equals("")
                && zip_code != null  && !zip_code.equals("")
                && country != null  && !country.equals("")
                && invoice_email != null  && !invoice_email.equals("");
    }

    @JsonIgnore
    public boolean isCompleteCompany() {
        return street != null
                && street_number != null && !street_number.equals("")
                && city != null && !city.equals("")
                && zip_code != null && !zip_code.equals("")
                && country != null && !country.equals("")
                && invoice_email != null && !invoice_email.equals("")
                && company_name != null && !company_name.equals("")
                && company_authorized_email != null && !company_authorized_email.equals("")
                && company_authorized_phone != null && !company_authorized_phone.equals("")
                && (company_vat_number != null || company_registration_no != null);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.PaymentDetail_crete.name())) return;
        customer.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.PaymentDetail_read.name())) return;
        customer.check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_update_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.PaymentDetail_update.name())) return;
        customer.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.PaymentDetail_delete.name())) return;
        customer.check_update_permission();
    }

    public enum Permission {PaymentDetail_crete, PaymentDetail_update, PaymentDetail_read, PaymentDetail_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_PaymentDetails.class, duration = CacheField.DayCacheConstant)
    public static Cache<UUID, Model_PaymentDetails> cache;

    public static Model_PaymentDetails getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_PaymentDetails getById(UUID id) throws _Base_Result_Exception {

        Model_PaymentDetails paymentDetails = cache.get(id);

        if (paymentDetails == null) {

            paymentDetails = Model_PaymentDetails.find.byId(id);
            if (paymentDetails == null) throw new Result_Error_NotFound(Model_PaymentDetails.class);

            cache.put(id, paymentDetails);
        }

        // Check Permission
        paymentDetails.check_read_permission();
        return paymentDetails;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_PaymentDetails> find = new Finder<>(Model_PaymentDetails.class);


}
