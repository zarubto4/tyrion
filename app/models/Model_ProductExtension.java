package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Result;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_ExtensionType;
import utilities.financial.extensions.extensions.Extension;
import utilities.financial.extensions.configurations.*;
import utilities.logger.Class_Logger;
import utilities.swagger.documentationClass.Swagger_ProductExtension_New;
import utilities.swagger.outboundClass.Swagger_ProductExtension_Type;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "ProductExtension", description = "Model of ProductExtension")
@Table(name="ProductExtension")
public class Model_ProductExtension extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_ProductExtension.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                    @Id @ApiModelProperty(required = true) public String id;
                                        @ApiModelProperty(required = true) public String name;
                                        @ApiModelProperty(required = true) public String description;

                                        @ApiModelProperty(required = true) public String color;

           @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_ExtensionType type;
                            @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
                                        @ApiModelProperty(required = true) public Integer order_position;

    @JsonProperty  @ApiModelProperty(required = true) public boolean active;
                                                               @JsonIgnore public boolean removed;

                                        @ApiModelProperty(required = true) public Date created;

                                                    @JsonIgnore @ManyToOne public Model_Product product;

                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_included;
                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_optional;



  /* CACHE VALUES --------------------------------------------------------------------------------------------------------*/


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) @Transient
    public Double price(){
        try {
            return getDoubleDailyPrice();
        } catch (Exception e) {
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = true, value = "Only for Administration used") @Transient  @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean include(){
        try {
            if(edit_permission()) return tariff_included!= null;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = false, value = "only with edit permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String config(){
        try {

            if(!edit_permission()) return null;
            return Json.toJson(Configuration.getConfiguration(type, configuration)).toString();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return "{\"error\":\"config file error\"}";
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    @JsonIgnore @Override
    public void save(){

        created = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString().substring(0,8);
            if (find.byId(this.id) == null) break;
        }
        if (product == null) {
            if (tariff_included != null) {
                order_position = find.where().eq("tariff_included.id", tariff_included.id).findRowCount() + 1;
            } else {
                order_position = find.where().eq("tariff_optional.id", tariff_optional.id).findRowCount() + 1;
            }
        }
        super.save();
    }

    @JsonIgnore @Override
    public void delete(){

        int pointer = 1;

        if(tariff_included != null) {
            for (Model_ProductExtension extension : tariff_included.extensions_included()) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        if(tariff_optional != null) {
            for (Model_ProductExtension extension : tariff_optional.extensions_optional()) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        if(product != null) {
            for (Model_ProductExtension extension : product.extensions) {

                if (!extension.id.equals(this.id)) {
                    extension.order_position = pointer++;
                    extension.update();
                }
            }
        }
        super.delete();
    }

    @JsonIgnore @Override
    public void update(){

        cache_price.put(this.id, this.getDailyPrice());
        super.update();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up(){

        if(order_position == 1) return;

        if(tariff_included != null) {
            tariff_included.extensions_included.get(order_position - 2).order_position = this.order_position;
            tariff_included.extensions_included.get(order_position - 2).update();
        }else {

            tariff_optional.extensions_optional.get(order_position - 2).order_position = this.order_position;
            tariff_optional.extensions_optional.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore
    public void down(){


        if(tariff_included != null){

            tariff_included.extensions_included.get(order_position).order_position = tariff_included.order_position - 1;
            tariff_included.extensions_included.get(order_position).update();

        }else{

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

            terminal_logger.trace("getActualPrice: Getting price for extension of type '{}' with id: {}", this.type.name(), this.id);

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            terminal_logger.debug("getActualPrice: Got extension type and configuration.");

            Long price = extension.getActualPrice(configuration);

            terminal_logger.debug("getActualPrice: Returned value is {}", price);

            return price;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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

            terminal_logger.trace("getDailyPrice: Getting price for extension of type {}", this.type.name());

            Long price = cache_price.get(id);
            if (price == null) {

                Extension extension = getExtensionType();
                if (extension == null) return null;

                Object configuration = getConfiguration();
                if (configuration == null) return null;

                terminal_logger.debug("getDailyPrice: Got extension type and configuration.");

                price = extension.getDailyPrice(configuration);

                cache_price.put(id, price);
            } else {
                terminal_logger.debug("getDailyPrice: Returned from cache");
            }

            return price;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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
            terminal_logger.internalServerError(e);
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

            terminal_logger.trace("getDoubleConfigPrice: Getting price for extension of type {}", this.type.name());

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            terminal_logger.debug("getDoubleConfigPrice: Got extension type and configuration.");

            return ((double) extension.getConfigPrice(configuration));

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Long getConfigPrice() {
        try {

            terminal_logger.trace("getConfigPrice: Getting price for extension of type {}", this.type.name());

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            terminal_logger.debug("getConfigPrice: Got extension type and configuration.");

            return extension.getConfigPrice(configuration);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public String getTypeName(){

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

        } catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public String getExtensionTypeName(){

        Extension extension = getExtensionType();
        if (extension == null) return null;

        return extension.getName();
    }

    @JsonIgnore
    public String getExtensionTypeDescription(){

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
        extension.removed = false;
        extension.configuration = this.configuration;

        return extension;
    }


    @JsonIgnore
    public Object getConfiguration(){
        try {

          return  Configuration.getConfiguration(type, configuration);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission(){
        return (product != null && product.customer.isEmployee(Controller_Security.get_person()))
                || Controller_Security.get_person().has_permission("ProductExtension_create");
    }

    @JsonIgnore
    public boolean read_permission(){
        return product == null
                ||  product.customer.isEmployee(Controller_Security.get_person())
                || Controller_Security.get_person().has_permission("ProductExtension_read");
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean edit_permission(){
        return (product != null && product.customer.isEmployee(Controller_Security.get_person()))
                || Controller_Security.get_person().has_permission("ProductExtension_edit");
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean act_deactivate_permission(){
        return (product != null && product.customer.isEmployee(Controller_Security.get_person()))
                || Controller_Security.get_person().has_permission("ProductExtension_act_deactivate");
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean delete_permission(){
        return Controller_Security.get_person().has_permission("ProductExtension_delete");
    }

    public enum permissions{ProductExtension_create, ProductExtension_read, ProductExtension_edit, ProductExtension_act_deactivate, ProductExtension_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_ProductExtension> find = new Model.Finder<>(Model_ProductExtension.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public static final String CACHE_PRICE = Model_ProductExtension.class.getSimpleName() + "_PRICE";

    public static Cache<String, Long> cache_price;

    @JsonIgnore
    public static Model_ProductExtension get_byId(String id) {
        return find.where().eq("id", id).eq("removed", false).findUnique();
    }

    @JsonIgnore
    public static List<Model_ProductExtension> get_byUser(String person_id) {
        return find.where().eq("product.customer.employees.person.id", person_id).eq("removed", false).findList();
    }
}