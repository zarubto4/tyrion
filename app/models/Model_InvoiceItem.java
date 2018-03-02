package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.Cached;
import utilities.enums.Currency;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "InvoiceItem", description = "Model of InvoiceItem")
@Table(name="InvoiceItem")
public class Model_InvoiceItem extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                       public Model_Invoice invoice;

                                                                         public String name; // Jméno položky
                                                                         public Long   quantity; // Počet položek
                                                                         public String unit_name; // Piece,
                                                            @JsonIgnore  public Long unit_price; // Cena / Musí být public - zasílá se do fakturoidu

    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)    public Currency currency;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_invoice_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String vat_rate() {  return vat.toString(); }

    @JsonProperty @Transient public Double unit_price_without_vat() { return  ((double) (unit_price  - (unit_price * (vat / (100 + vat))))) / 1000;}

    @JsonProperty public Double unit_price() { return ((double) unit_price);}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient public Long vat = (long) 21000;  // TODO je to hardcodované - Asi b bylo lepší to přenést na nějakou proměnou v Configu!

    @JsonIgnore
    public UUID get_invoice_id() throws _Base_Result_Exception {

        if (cache_invoice_id == null) {

            Model_Widget widget = Model_Widget.find.query().where().eq("versions.id", id).select("id").findOne();
            if (widget != null) {
                cache_invoice_id = widget.id;
            } else {
                cache_invoice_id = null;
            }
        }

        return cache_invoice_id;
    }

    @JsonIgnore
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

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { get_invoice().check_update_permission();}
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { get_invoice().check_read_permission();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { get_invoice().check_update_permission();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { get_invoice().check_update_permission();}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InvoiceItem> find = new Finder<>(Model_InvoiceItem.class);
}
