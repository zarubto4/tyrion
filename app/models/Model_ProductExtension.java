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

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) @Transient
    public Double price() {
        try {
            return getDoubleDailyPrice();

        }catch (_Base_Result_Exception e){
            return null;
        } catch (Exception e) {
            return null;
        }
    }

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
    @JsonIgnore @Override
    public void save() {
        if (order_position == null || order_position < 0) {
                order_position = find.query().where().eq("product.id", product.id).findCount() + 1;
        }

        super.save();
    }

    @JsonIgnore @Override
    public boolean delete() {
        int pointer = 1;
        for (Model_ProductExtension extension : product.extensions) {
            if (!extension.id.equals(this.id)) {
                extension.order_position = pointer++;
                extension.update();
            }
        }

        return super.delete();
    }

    @JsonIgnore @Override
    public void update() {
        cache_price.remove(this.id);
        super.update();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Object getConfiguration() {
        try {

            return  Configuration.getConfiguration(type, configuration);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
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
            Long price = cache_price.get(id);
            if(price == null) {
                price = Extension.getDailyPrice(type, configuration);
                cache_price.put(this.id, price);
            }

            return (double) price;

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }

    @JsonIgnore
    public Long getActualPrice() {
        try {
            return Extension.getActualPrice(type, configuration);

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0L;
        }
    }


    @JsonIgnore
    public boolean isActive() {
        return active;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_create.name())) return;
        if (product == null || product.customer == null) {
            System.out.println("Model_ProductExtension:: check_create_permission:: product.customer is null - this.product name: " + this.name );
            throw new Result_Error_PermissionDenied();
        }
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_read.name())) return;
        if (product == null) {
           return;
        }
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_update.name())) return;
        if (product == null || product.customer == null) {
            System.out.println("Model_ProductExtension:: check_update_permission:: product.customer is null - this.product name: " + this.name );
            throw new Result_Error_PermissionDenied();
        }
        if(product.customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_delete.name())) return;
        if (product == null || product.customer == null) {
            System.out.println("Model_ProductExtension:: check_delete_permission:: product.customer is null - this.product name: " + this.name );
            throw new Result_Error_PermissionDenied();
        }
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty @ApiModelProperty(required = true) public void check_act_deactivate_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.ProductExtension_act_deactivate.name())) return;
        if (product == null || product.customer == null) {
            System.out.println("Model_ProductExtension:: check_act_deactivate_permission:: product.customer is null - this.product name: " + this.id );
            throw new Result_Error_PermissionDenied();
        }
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