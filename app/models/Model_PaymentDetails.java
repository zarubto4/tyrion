package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.PaymentMethod;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
import java.math.BigDecimal;
import java.util.*;

@Entity
@ApiModel(value = "PaymentDetails", description = "Details about product payment")
@Table(name="Payment_Details")
public class Model_PaymentDetails extends BaseModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_IntegratorClient.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

           @JsonIgnore @OneToOne(mappedBy = "payment_details") public Model_Product product;

                                                   @JsonIgnore public String payment_methods; // PaymentMethod values separated by |

                                  @Enumerated(EnumType.STRING) public PaymentMethod payment_method;

                                                   @JsonIgnore public boolean on_demand;            // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                                   /* If invoice price is more than this limit, wait for admin to approve it
                                                      Use getter getMonthlyLimit() to receive the proper value!
                                                    */
                                                   @JsonIgnore public BigDecimal monthly_limit;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/
    @JsonProperty
    public List<PaymentMethod> payment_methods() {
        try{
            return  PaymentMethod.fromString(payment_methods);

        } catch (Exception e) {
            logger.error("Cannot get payment methods", e);
        }

        return Collections.emptyList();
    }

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/
    /**
     * @return true if payment details are complete and valid
     */
    @JsonIgnore
    public boolean isValid() {
        return payment_method != null;
    }

    /**
     * If invoice price is more than this limit, wait for admin to approve it. <br />
     * If there is not value set for this payment details, takes default server value. TODO
     *
     * @return
     */
    @JsonIgnore
    public BigDecimal getMonthlyLimit() {
        return monthly_limit == null ? BigDecimal.ZERO : monthly_limit;
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        product.check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        // no create permissions
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        product.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        product.check_delete_permission();
    }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_PaymentDetails.class)
    public static CacheFinder<Model_PaymentDetails> find = new CacheFinder<>(Model_PaymentDetails.class);
}
