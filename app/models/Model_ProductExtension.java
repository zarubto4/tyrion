package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.db.ebean.Transactional;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.ProductEventType;
import utilities.enums.ExtensionType;
import utilities.errors.Exceptions.Result_Error_BadRequest;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.financial.extensions.configurations.*;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.extensions.Extension;
import utilities.logger.Logger;
import utilities.model.OrderedNamedModel;
import utilities.model.UnderProduct;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity
@ApiModel(value = "ProductExtension", description = "Model of ProductExtension")
@Table(name="ProductExtension")
public class Model_ProductExtension extends OrderedNamedModel implements Permissible, UnderProduct {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProductExtension.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                        @ApiModelProperty(required = true) public String color;

           @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public ExtensionType type;
                            @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;

                         /**
                          * Should never be set directly!! Use setActive() method after extension is created.
                          */
                         @JsonProperty  @ApiModelProperty(required = true) public boolean active = false;

                                                    @JsonIgnore @ManyToOne public Model_Product product;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    /* CONSTUCTOR *****-----------------------------------------------------------------------------------------------------*/
    public Model_ProductExtension() {
        super(find);
    }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @ApiModelProperty(required = false, value = "Visible only for Administrator with Special Permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String config() {
        try {

            if(configuration== null){
                throw new NullPointerException();
            }

            return Json.toJson(Configuration.getConfiguration(type, configuration)).toString();

        } catch (NullPointerException e) {
            return "{\"error\":\"configuration is not set yet\"}";
        } catch (Exception e) {
            logger.internalServerError(e);
            return "{\"error\":\"config file error, or required permission\"}";
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        boolean newExtension = id == null;
        if (newExtension && active) {
            throw new Result_Error_BadRequest("Cannot save a new product with active == true. Use setActive to activate product.");
        }

        super.save();

        if (newExtension) {
            saveEvent(this.created, ProductEventType.EXTENSION_CREATED, configuration);
        }
    }

    // todo what if some DB stuff fails?
    @Transactional
    public void setActive(boolean activeNew) throws Exception {
        if(this.active == activeNew) {
            throw new Result_Error_BadRequest("Extension is already " + (activeNew ? "activated" : "deactivated"));
        }

        try {
            createExtension().activate(this);
        }
        catch (Exception e) {
            throw e;
        }

        this.active = activeNew;
        update();

        ProductEventType event = activeNew ? ProductEventType.EXTENSION_ACTIVATED : ProductEventType.EXTENSION_DEACTIVATED;
        saveEvent(updated, event, null);
    }

    @Override
    public boolean delete() {
        if(active) {
            try {
                createExtension().deactivate(this);
            }
            catch (Exception e) {
                logger.error("Deactivation was not successful. Product cannot be deleted.", e);
                return false;
            }
        }
        saveEvent(new Date(), ProductEventType.EXTENSION_DEACTIVATED, null);

        try {
            if (!super.delete()) {
                logger.error("Product extension cannot be deleted.");
                return false;
            }
        }
        catch (Exception e) {
            logger.error("Product extension cannot be deleted.", e);
            return false;
        }

        saveEvent(removed, ProductEventType.EXTENSION_DELETED, null);
        return true;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public Model_Product getProduct() {
        return null;
    }

    @JsonIgnore
    public Extension createExtension() throws Exception {
        Class<? extends Extension> extensionClass = type.getExtensionClass();
        if(extensionClass == null) {
            throw new IllegalStateException("No extension class.");
        }

        return extensionClass.newInstance();
    }

    @JsonIgnore
    public Configuration getConfiguration() {
        try {

            return  Configuration.getConfiguration(type, configuration);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public BigDecimal getPrice(String consumptionString) throws Exception {
        ResourceConsumption consumption = ResourceConsumption.getConsumption(type, consumptionString);
        return getPrice(consumption);
    }

    @JsonIgnore
    public BigDecimal getPrice(ResourceConsumption consumption) throws Exception {
        Extension extension = createExtension();
        return extension.getPrice(getConfiguration(), consumption);
    }

    @JsonIgnore
    public ResourceConsumption getResourceConsumption(Date from, Date to) throws Exception {
        Extension extension = createExtension();
        return extension.getConsumption(this, from, to);
    }

    @JsonIgnore
    public ResourceConsumption getResourceConsumption(String consumption) {
        return ResourceConsumption.getConsumption(type, consumption);
    }

    /**
     * Get all history events for given extension.
     *
     * @param ascending
     * @return All history events for an extension.
     */
    @JsonIgnore
    public List<Model_ProductEvent> getExtensionEvents(boolean ascending) {
        ExpressionList<Model_ProductEvent> query = Model_ProductEvent.find.query()
                .where()
                .eq("reference", id);

        return (ascending ? query.orderBy("created ASC, event_type ASC") : query.orderBy("created DESC, event_type DESC"))
                .findList();
    }

    /**
     * Get last history event for given extension before / at given time.
     *
     * @param time
     * @return History event for the extension at before / at given time, null if there is none,
     */
    @JsonIgnore
    public Model_ProductEvent getLastExtensionEvent(Date time) {
        Model_ProductEvent event = Model_ProductEvent.find.query()
                .where()
                .eq("reference", id)
                .le("created", time)
                .orderBy("created DESC")
                .setMaxRows(1)
                .findOne();

        return event;
    }

    /**
     * Check if given product extension was active at given time.
     *
     * @param time
     * @return True if the extension was active at given time, false otherwise.
     */
    @JsonIgnore
    public boolean wasActive(Date time) {
        Model_ProductEvent event = Model_ProductEvent.find.query()
                .where()
                .eq("reference", id)
                .or(Expr.eq("event_type", ProductEventType.EXTENSION_ACTIVATED),
                        Expr.eq("event_type", ProductEventType.EXTENSION_DEACTIVATED))
                .le("created", time)
                .orderBy("created DESC")
                .setMaxRows(1)
                .findOne();

        if(event == null) {
            return false;
        }

        return event.event_type == ProductEventType.EXTENSION_ACTIVATED;
    }

    /**
     * Check if given product extension was active during given time.
     *
     * @param from Start if the search period. (Included.)
     * @param to End of the search period. (Excluded.)
     * @return True if the extension was active during given time, false otherwise.
     */
    @JsonIgnore
    public boolean wasActive(Date from, Date to) {
        if(wasActive(from)) {
            return true;
        }

        boolean activatedDuringPeriod = Model_ProductEvent.find.query()
                .where()
                .eq("reference", id)
                .ge("created", from)
                .lt("created", to)
                .eq("event_type", ProductEventType.EXTENSION_ACTIVATED)
                .findCount() > 0;

        return activatedDuringPeriod;
    }

    /**
     * Get time of fist activation of the given product extension.
     *
     * @return First activation of the extension if it was activated at some point; null otherwise.
     */
    @JsonIgnore
    public Date getFirstActivationTime() {
        Model_ProductEvent event = Model_ProductEvent.find.query()
                .select("created")
                .where()
                .eq("reference", id)
                .and()
                .eq("event_type", ProductEventType.EXTENSION_ACTIVATED)
                .order().asc("created")
                .setMaxRows(1)
                .findOne();

        return event == null ? null : event.created;
    }

    /**
     * Get all unpaid financial events for given product extension, ordered by event start.
     *
     * @param ascending
     * @return Unpaid financial events in descending order according to event start.
     */
    @JsonIgnore
    public List<Model_ExtensionFinancialEvent> getFinancialEventsNotInvoiced(boolean ascending) {
        List<Model_ExtensionFinancialEvent> unpaid = Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("product_extension.id", id)
                .and()
                .isNull("invoice")
                .orderBy("event_start " + (ascending ? "ASC" : "DESC"))
                .findList();
        return unpaid;
    }

    /**
     * Get last financial event for given product extension.
     *
     * @return Last financial event or null if there is none.
     */
    @JsonIgnore
    public Model_ExtensionFinancialEvent getFinancialEventLast() {
        Model_ExtensionFinancialEvent lastFinancialEvent = Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("product_extension.id", id)
                .order().desc("event_start")
                .setMaxRows(1)
                .findOne();

        return lastFinancialEvent;
    }

    @JsonIgnore
    public Model_ExtensionFinancialEvent createFinancialEvent(Date now, Date from, Date to) throws Exception {
        ResourceConsumption consumption = null;
        if(wasActive(from, to)) {
            consumption = getResourceConsumption(from, to);
        }

        Model_ExtensionFinancialEvent financialEvent = new Model_ExtensionFinancialEvent();
        financialEvent.product_extension = this;
        financialEvent.event_start = from;
        financialEvent.event_end = to;
        financialEvent.created = now;
        financialEvent.consumption = consumption == null ? "" : Json.toJson(consumption).toString();
        financialEvent.invoice = null;
        financialEvent.save();

        return financialEvent;
    }

    /**
     * Creates new financial event(s) for given product extension. Each event contain resource consumption, price,
     * information about time and date of payment. <br>
     * Events are created for every day from the last saved event or first activation of the extension if there is no event.
     * If the extension was not active during given period, empty event is saved (no consumption, zero price, paid).
     * The unfinished day can be saved or not according to the parameter.
     *
     * @param saveToday False if we want to save only event(s) till last midnight, true if we want to save today as well.
     * @return All newly created financial events in ascending order according to event period.
     */
    @JsonIgnore
    public List<Model_ExtensionFinancialEvent> updateHistory(boolean saveToday) {
        List<Model_ExtensionFinancialEvent> result = new ArrayList<>();

        // Time from which we should create new event(s) - last event or activation time.
        Date noHistoryFrom;

        Model_ExtensionFinancialEvent lastFinancialEvent = getFinancialEventLast();
        if(lastFinancialEvent != null) {
            noHistoryFrom = lastFinancialEvent.event_end;
        }
        else {
            noHistoryFrom = getFirstActivationTime();
        }

        // Product was never activated. Create no events, return empty set.
        if(noHistoryFrom == null) {
            return result;
        }

        // TODO
        ZoneId timeZoneId = ZoneId.of("UTC");

        ZonedDateTime now = ZonedDateTime.now(timeZoneId);
        ZonedDateTime stop = saveToday? now : now.toLocalDate().atStartOfDay(timeZoneId);

        ZonedDateTime from = ZonedDateTime.ofInstant(noHistoryFrom.toInstant(), timeZoneId);
        ZonedDateTime to = from.plus(1, ChronoUnit.DAYS).toLocalDate().atStartOfDay(timeZoneId);

        try {
            do {
                Model_ExtensionFinancialEvent financialEvent = createFinancialEvent(Date.from(now.toInstant()),
                        Date.from(from.toInstant()), Date.from(to.toInstant()));
                result.add(financialEvent);

                from = to;
                to = from.plus(1, ChronoUnit.DAYS).toLocalDate().atStartOfDay(timeZoneId);
            }
            while (!to.isAfter(stop));
        }
        catch (Exception e) {
            e.printStackTrace();
            // TODO log or re-throw
        }

        Collections.reverse(result);
        return result;
    }

/* EVENTS --------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_ProductEvent saveEvent(Date time, ProductEventType eventType, String data) {
        Model_ProductEvent historyEvent = new Model_ProductEvent();
        historyEvent.product = product;
        historyEvent.reference = this.id;
        historyEvent.created = time;
        historyEvent.event_type = eventType;
        historyEvent.detail = data;
        historyEvent.save();

        return historyEvent;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.PRODUCT_EXTENSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_ProductExtension.class)
    public static CacheFinder<Model_ProductExtension> find = new CacheFinder<>(Model_ProductExtension.class);

    public static List<Model_ProductExtension> getByUser(UUID personId) {
        return Model_ProductExtension.find.query().where().eq("product.owner.employees.person.id", personId).eq("deleted", false).findList();
    }
}