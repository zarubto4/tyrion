package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.enums.ExtensionType;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.financial.extensions.extensions.Extension;
import utilities.financial.extensions.configurations.*;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "ProductExtension", description = "Model of ProductExtension")
@Table(name="ProductExtension")
public class Model_ProductExtension extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProductExtension.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                        @ApiModelProperty(required = true) public String color;

           @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public ExtensionType type;
                            @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
                                        @ApiModelProperty(required = true) public Integer order_position;

    @JsonProperty  @ApiModelProperty(required = true) public boolean active;

                                                    @JsonIgnore @ManyToOne public Model_Product product;

                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_included;
                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_optional;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) @Transient
    public Double price() {
        try {
            return getDoubleDailyPrice();
        } catch (Exception e) {
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = false, value ="Visible only for Administrator with Special Permission") @Transient  @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean include() {
        try {
            check_update_permission();
            return tariff_included!= null;
        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty  @ApiModelProperty(required = false, value = "Visible only for Administrator with Special Permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String config() {
        try {

            check_update_permission();
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
    @JsonIgnore @Override
    public void save() {

        if (product == null) {
            if (tariff_included != null) {
                order_position = find.query().where().eq("tariff_included.id", tariff_included.id).findCount() + 1;
            } else {
                order_position = find.query().where().eq("tariff_optional.id", tariff_optional.id).findCount() + 1;
            }
        }
        super.save();
    }

    @JsonIgnore @Override
    public boolean delete() {

        int pointer = 1;

        if (tariff_included != null) {
            for (Model_ProductExtension extension : tariff_included.extensions_included()) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        if (tariff_optional != null) {
            for (Model_ProductExtension extension : tariff_optional.extensions_optional()) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        if (product != null) {
            for (Model_ProductExtension extension : product.extensions) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        return super.delete();
    }

    @JsonIgnore @Override
    public void update() {

        cache_price.put(this.id, this.getDailyPrice());
        super.update();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up() throws _Base_Result_Exception {

        check_update_permission();

        if (order_position == 1) return;

        if (tariff_included != null) {
            tariff_included.extensions_included.get(order_position - 2).order_position = this.order_position;
            tariff_included.extensions_included.get(order_position - 2).update();
        } else {

            tariff_optional.extensions_optional.get(order_position - 2).order_position = this.order_position;
            tariff_optional.extensions_optional.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore
    public void down() throws _Base_Result_Exception {

        check_update_permission();

        if (tariff_included != null) {

            tariff_included.extensions_included.get(order_position).order_position = tariff_included.order_position - 1;
            tariff_included.extensions_included.get(order_position).update();

        } else {

            tariff_optional.extensions_optional.get(order_position).order_position = tariff_optional.order_position - 1;
            tariff_optional.extensions_optional.get(order_position).update();
        }

        this.order_position += 1;
        this.update();

    }

    /**
     * This method is used to get calculated price. Credit can be spent more than once per day.
     * Returned value corresponds to the daily period of spending.
     * @return Long price divided by spendDailyPeriod.
     */
    @JsonIgnore
    public Long getActualPrice() {
        try {

            logger.trace("getActualPrice: Getting price for extension of type '{}' with id: {}", this.type.name(), this.id);

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            logger.debug("getActualPrice: Got extension type and configuration.");

            Long price = extension.getActualPrice(configuration);

            logger.debug("getActualPrice: Returned value is {}", price);

            return price;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /**
     * Method gets calculated price of an extension for one day.
     * @return Long price of extension for one day.
     */
    @JsonIgnore
    public Long getDailyPrice() {
        try {

            logger.trace("getDailyPrice: Getting price for extension of type {}", this.type.name());

            Long price = cache_price.get(id);
            if (price == null) {

                Extension extension = getExtensionType();
                if (extension == null) return null;

                Object configuration = getConfiguration();
                if (configuration == null) return null;

                logger.debug("getDailyPrice: Got extension type and configuration.");

                price = extension.getDailyPrice(configuration);

                cache_price.put(id, price);
            } else {
                logger.debug("getDailyPrice: Returned from cache");
            }

            return price;

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0l;
        }
    }

    /**
     * Method serves for information purposes only.
     * Returned value is shown to users, because real price is Double USD * 1000.
     * @return Real price in Double.
     */
    @JsonIgnore
    public Double getDoubleDailyPrice() {
        try {

            Long price = getDailyPrice();
            if (price == null) return null;

            return (double) price;

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }

    /**
     * Method serves for information purposes only.
     * Returned value is shown to users, because real price is Double USD * 1000.
     * @return Real price in Double.
     */
    @JsonIgnore
    public Double getDoubleConfigPrice() {
        try {

            logger.trace("getDoubleConfigPrice: Getting price for extension of type {}", this.type.name());

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            logger.debug("getDoubleConfigPrice: Got extension type and configuration.");

            return ((double) extension.getConfigPrice(configuration));

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Long getConfigPrice() {
        try {

            logger.trace("getConfigPrice: Getting price for extension of type {}", this.type.name());

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            logger.debug("getConfigPrice: Got extension type and configuration.");

            return extension.getConfigPrice(configuration);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public String getTypeName() {

        return type.name();
    }

    @JsonIgnore
    public Extension getExtensionType() {
        try {

            Class<? extends Extension> clazz = this.type.getExtensionClass();

            Extension extension = null;

            if (clazz != null) {
                extension = clazz.newInstance();
            }

            return extension;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public String getExtensionTypeName() {

        Extension extension = getExtensionType();
        if (extension == null) return null;

        return extension.getName();
    }

    @JsonIgnore
    public String getExtensionTypeDescription() {

        Extension extension = getExtensionType();
        if (extension == null) return null;

        return extension.getDescription();
    }


    @JsonIgnore
    public Model_ProductExtension copy() {

        Model_ProductExtension extension = new Model_ProductExtension();
        extension.name = this.name;
        extension.description = this.description;
        extension.color = this.color;
        extension.type = this.type;
        extension.active = true;
        extension.deleted = false;
        extension.configuration = this.configuration;

        return extension;
    }


    @JsonIgnore
    public Object getConfiguration() {
        try {

          return  Configuration.getConfiguration(type, configuration);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_create.name())) return;
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_read.name())) return;
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_update.name())) return;
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty @ApiModelProperty(required = true) public void check_act_deactivate_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_act_deactivate.name())) return;
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { ProductExtension_create, ProductExtension_read, ProductExtension_update, ProductExtension_act_deactivate, ProductExtension_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ProductExtension> find = new Finder<>(Model_ProductExtension.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Long.class, maxElements = 300, name = "Model_ProductExtension_Price")
    public static Cache<UUID, Long> cache_price;

    public static Model_ProductExtension getById(UUID id) {
        return find.query().where().idEq(id).eq("deleted", false).findOne();
    }

    public static List<Model_ProductExtension> getByUser(UUID person_id) {
        return find.query().where().eq("product.customer.employees.person.id", person_id).eq("deleted", false).findList();
    }
}