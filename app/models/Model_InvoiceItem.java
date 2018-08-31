package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.cache.Cached;
import utilities.enums.Currency;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@ApiModel(value = "InvoiceItem", description = "Model of InvoiceItem")
@Table(name="InvoiceItem")
public class Model_InvoiceItem extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/
private static final Logger logger = new Logger(Model_InvoiceItem.class);
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                       public Model_Invoice invoice;    // TODO Cache jako v ostatních cache objektech [MARTIN TODO] - jestli to někde někdo blbě nevolá

                                                                         public String name; // Jméno položky
                                                                         public BigDecimal   quantity; // Počet položek
                                                                         public String unit_name; // Jednotka
                                                                         public BigDecimal unit_price;  // Cena za položku

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_invoice_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient public int vat_rate = 21;  // TODO je to hardcodované - Asi b bylo lepší to přenést na nějakou proměnou v Configu! LEVEL: HARD  TIME: LONGTERM

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public double  total_price_without_vat() {
        return (unit_price.multiply(quantity))
                .setScale(Server.financial_price_scale, Server.financial_price_rounding)
                .doubleValue();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore     // TODO Cache jako v ostatních cache objektech [MARTIN TODO]
    public UUID get_invoice_id() throws _Base_Result_Exception {

        if (cache_invoice_id == null) {

            Model_Invoice invoice = Model_Invoice.find.query().where().eq("invoice_items.id", id).select("id").findOne();
            if (invoice != null) {
                cache_invoice_id = invoice.id;
            } else {
                cache_invoice_id = null;
            }
        }

        return cache_invoice_id;
    }

    @JsonIgnore    // TODO Cache jako v ostatních cache objektech [MARTIN TODO]
    public Model_Invoice get_invoice() throws _Base_Result_Exception {

        if (get_invoice_id() != null) {
            return Model_Invoice.getById(cache_invoice_id);
        }

        return null;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { invoice.check_update_permission();}
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { get_invoice().check_read_permission();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { get_invoice().check_update_permission();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { get_invoice().check_update_permission();}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InvoiceItem> find = new Finder<>(Model_InvoiceItem.class);
}
