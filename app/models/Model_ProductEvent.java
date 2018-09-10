package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.ProductEventReferenceType;
import utilities.enums.ProductEventType;
import utilities.enums.ProductEventTypeReadPermission;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel("ProductEvent")
@Table(name="ProductEvent")
public class Model_ProductEvent extends BaseModel {
    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProductEvent.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ManyToOne public Model_Product product;

    public UUID reference;

    @ApiModelProperty(required = true) public ProductEventType event_type;

    @ApiModelProperty(required = true) public ProductEventTypeReadPermission read_permission;

    @JsonIgnore @Column(columnDefinition = "TEXT")
    public String detail;

    /* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @Override
    public void save() throws _Base_Result_Exception {
        if(event_type != null && read_permission == null) {
           read_permission = event_type.getDefaultReadPermission();
        }

        super.save();
    }

    @JsonProperty
    public ProductEventReferenceType reference_type() {
        return event_type == null ? null : event_type.getReferenceType();
    }

    @JsonProperty
    public String event_type_name() {
        return event_type.getText();
    }

    @JsonProperty
    public String reference_name() {
        if(reference == null) {
            return "";
        }

        if(event_type.getReferenceType() == ProductEventReferenceType.INVOICE) {
            Model_Invoice invoice = Model_Invoice.find.byId(reference);
            if(invoice == null) {
                return null;
            }

            String invoiceNumber = Model_Invoice.find.byId(reference).invoice_number;
            return StringUtils.isEmpty(invoiceNumber) ? "proforma" : invoiceNumber;
        }

        if(event_type.getReferenceType() == ProductEventReferenceType.EXTENSION) {
            Model_ProductExtension extension = Model_ProductExtension.find.byId(reference);
            if(extension == null) {
                return null;
            }

            return extension.name;
        }

        return reference.toString();
    }

    @JsonProperty
    public String detail() {
        return detail;
    }

    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore
    public List<Model_ProductEvent> getEvents(ProductEventType type, Instant from, boolean ascending) {
        return Model_ProductEvent.find.query()
                .where()
                .eq("event_type", type)
                .and()
                .ge("time", from)
                .orderBy("time " + (ascending ? "ASC" : "DESC"))
                .findList();
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

    @CacheFinderField(Model_ProductEvent.class)
    public static CacheFinder<Model_ProductEvent> find = new CacheFinder<>(Model_ProductEvent.class);
}
