package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderCustomer;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@ApiModel(description = "Model of Contact",
        value = "Contact")
@Table(name="Contact")
public class Model_Contact extends BaseModel implements Permissible, UnderCustomer {

    // Logger
    private static final Logger logger = new Logger(Model_Contact.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    // because of the rights
    @JsonIgnore @OneToOne(mappedBy = "contact",cascade = CascadeType.ALL)                           public Model_Customer customer;
    @JsonIgnore @OneToOne(mappedBy = "contact",cascade = CascadeType.ALL)                           public Model_IntegratorClient integrator_client;

    @JsonIgnore                                                                                     public Long fakturoid_subject_id; // ID účtu ve fakturoidu
    @JsonIgnore                                                                                     public Long gopay_id;

                                                                                                    public boolean company_account; // Rozhoduji se zda jde o detaily firemní nebo osobní

       @ApiModelProperty(required = true) @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String name; // company or customer name

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_email;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_phone;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_web;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_registration_no;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_vat_number;


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
    public Model_Contact copy() {

        Model_Contact details = new Model_Contact();
        details.name            = this.name;
        details.street          = this.street;
        details.street_number   = this.street_number;
        details.city            = this.city;
        details.zip_code        = this.zip_code;
        details.country         = this.country;
        details.invoice_email   = this.invoice_email;

        if (company_account) {
            details.company_account             = true;
            details.company_web                 = this.company_web;
            details.company_authorized_email    = this.company_authorized_email;
            details.company_authorized_phone    = this.company_authorized_phone;
            details.company_vat_number          = this.company_vat_number;
            details.company_registration_no     = this.company_registration_no;
        }

        return details;
    }

    @JsonIgnore
    public boolean isValid() {
        // check required field for both private and company contact
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(street_number) || StringUtils.isEmpty(city)
                || StringUtils.isEmpty(zip_code) || StringUtils.isEmpty(country) || StringUtils.isEmpty(invoice_email)) {
            return false;
        }

        // if this is not a company account, we do not need more information
        if (!company_account) {
            return true;
        }

        // check company required fields
        if (StringUtils.isEmpty(company_authorized_email) || StringUtils.isEmpty(company_authorized_phone)
                || (StringUtils.isEmpty(company_vat_number) && StringUtils.isEmpty(company_registration_no))) {
            return false;
        }

        return true;
    }

    @JsonIgnore @Override
    public Model_Customer getCustomer() {
        return customer != null ? customer : integrator_client.getCustomer();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.CONTACT;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Contact.class)
    public static CacheFinder<Model_Contact> find = new CacheFinder<>(Model_Contact.class);
}
