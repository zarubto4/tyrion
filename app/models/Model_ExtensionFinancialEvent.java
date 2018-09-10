package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.ExpressionList;
import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.extensions.Extension;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel("ExtensionFinancialEvent")
@Table(name="ExtensionFinancialEvent")
public class Model_ExtensionFinancialEvent extends BaseModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ExtensionFinancialEvent.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                        @ManyToOne public Model_ProductExtension product_extension;

                                                   public Date event_start;

                                                   public Date event_end;

    @JsonIgnore @Column(columnDefinition = "TEXT") public String consumption;

                @ManyToOne                         public Model_Invoice invoice;

    /* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/



    /**
     * FOR FRONTEND, NOT CALCULATIONS! For calculations, use getPrice() method.
     * @return
     */
    @JsonProperty
    public double price() {
        return getPrice().setScale(Server.financial_price_scale, Server.financial_price_rounding).doubleValue();
    }

    /**
     * @return human readable description of resource consumption
     */
    @JsonProperty
    public String resource_consumption() {
        return getConsumption().toReadableString();
    }

    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public BigDecimal getPrice() {
        try {
            Extension extension = product_extension.createExtension();
            return extension.getPrice(product_extension.getConfiguration(), getConsumption());
        }
        catch (Exception e) {
            logger.internalServerError(e);
        }

        return BigDecimal.ZERO;
    }

    @JsonIgnore
    /**
     * @return Used resources.
     */
    public ResourceConsumption getConsumption() {
        return ResourceConsumption.getConsumption(product_extension.type, consumption);
    }

    public static BigDecimal getTotalPrice(Collection<Model_ExtensionFinancialEvent> events) {
        return events.stream()
                     .map(item -> item.getPrice())
                     .reduce(BigDecimal::add)
                     .get();
    }


    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Transient @Override public void check_create_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { }

    public enum Permission {} // Not Required here

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/


    /* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_ExtensionFinancialEvent.class)
    public static CacheFinder<Model_ExtensionFinancialEvent> find = new CacheFinder<>(Model_ExtensionFinancialEvent.class);

    public static List<Model_ExtensionFinancialEvent> getFinancialEvents(UUID product_id, UUID invoice_id, UUID extension_id, Date from, Date to, boolean ascending) {
        ExpressionList<Model_ExtensionFinancialEvent> expressionList = Model_ExtensionFinancialEvent.find.query().where();

        if(product_id != null) {
            expressionList.eq("product_extension.product.id", product_id);
        }
        if(invoice_id != null) {
            expressionList.eq("invoice.id", invoice_id);
        }
        if(extension_id != null) {
            expressionList.eq("product_extension.id", extension_id);
        }
        if(from != null) {
            expressionList.ge("event_start", from);
        }
        if(to != null) {
            expressionList.le("event_end", from);
        }

        return expressionList.orderBy("event_start " + (ascending ? "ASC" : "DESC")).findList();
    }
}
