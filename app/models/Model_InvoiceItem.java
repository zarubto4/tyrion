package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.Cached;
import utilities.enums.Currency;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
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
                                                                         public Long   quantity; // Počet položek
                                                                         public String unit_name; // Piece,
                                                            @JsonIgnore  public Long unit_price; // Cena / Musí být public - zasílá se do fakturoidu

    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)    public Currency currency;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_invoice_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String vat_rate() {
        try {


            return vat.toString();

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @Transient public Double unit_price_without_vat() {
        try {
            return ((double) (unit_price - (unit_price * (vat / (100 + vat))))) / 1000;
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty public Double unit_price() {
        try {
            return ((double) unit_price);

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient public Long vat = (long) 21000;  // TODO je to hardcodované - Asi b bylo lepší to přenést na nějakou proměnou v Configu! LEVEL: HARD  TIME: LONGTERM

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
