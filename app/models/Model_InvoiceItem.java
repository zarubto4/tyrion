package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@ApiModel(value = "InvoiceItem", description = "Model of InvoiceItem")
@Table(name="InvoiceItem")
public class Model_InvoiceItem extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/
private static final Logger logger = new Logger(Model_InvoiceItem.class);
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                       public Model_Invoice invoice;

                                                                         public String name; // Jméno položky
                                                                         public BigDecimal   quantity; // Počet položek
                                                                         public String unit_name; // Jednotka
                                                                         public BigDecimal unit_price;  // Cena za položku

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient public int vat_rate = 21;  // TODO je to hardcodované - Asi b bylo lepší to přenést na nějakou proměnou v Configu! LEVEL: HARD  TIME: LONGTERM

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public double  total_price_without_vat() {
        return (unit_price.multiply(quantity))
                .setScale(Server.financial_price_scale, Server.financial_price_rounding)
                .doubleValue();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Invoice getInvoice() throws _Base_Result_Exception {
        return isLoaded("invoice") ? invoice : Model_Invoice.find.query().where().eq("invoice_items.id", id).findOne();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InvoiceItem> find = new Finder<>(Model_InvoiceItem.class);
}
