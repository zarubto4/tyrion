package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Result;
import utilities.enums.Enum_ExtensionType;
import utilities.financial.extensions.Extension;
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

                                        @ApiModelProperty(required = true) public boolean active;
                                                               @JsonIgnore public boolean removed;

                                        @ApiModelProperty(required = true) public Date created;

                                                    @JsonIgnore @ManyToOne public Model_Product product;

                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_included;
                                                    @JsonIgnore @ManyToOne public Model_Tariff tariff_optional;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Price price(){
        try {
            Price price = new Price();
            price.USD = getDoubleDailyPrice();
            return price;
        } catch (Exception e) {
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save(){

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

    @JsonIgnore
    public void up(){

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

            tariff_included.extensions_included.get(order_position).order_position = tariff_included.labels.get(order_position).order_position - 1;
            tariff_included.extensions_included.get(order_position).update();

        }else{

            tariff_optional.extensions_optional.get(order_position).order_position = tariff_optional.labels.get(order_position).order_position - 1;
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

            terminal_logger.trace("getActualPrice: Getting price for extension of type {}", this.type.name());

            Extension extension = getExtensionType();
            if (extension == null) return null;

            Object configuration = getConfiguration();
            if (configuration == null) return null;

            terminal_logger.debug("getDailyPrice: Got extension type and configuration.");

            return extension.getDailyPrice(configuration);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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

            Long price = getDailyPrice();
            if (price == null) return null;

            return (double) price / 1000;

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

    /**
     * Prerequisite is an assigned type of Extension.
     * Gets the default daily price, this price is used when new extension is created.
     * @return Long price of given extension type.
     */
    @JsonIgnore
    public Long getExtensionTypePrice(){

        Extension extension = getExtensionType();
        if (extension == null) return null;

        return extension.getDefaultDailyPrice();
    }

    @JsonIgnore
    public static List<Swagger_ProductExtension_Type> getExtensionTypes() {
        try {

            List<Swagger_ProductExtension_Type> types = new ArrayList<>();

            for (Enum_ExtensionType e : Enum_ExtensionType.values()){

                Class<? extends Extension> clazz = e.getExtensionClass();
                if (clazz != null) {
                    Extension extension = clazz.newInstance();

                    Swagger_ProductExtension_Type type = new Swagger_ProductExtension_Type();
                    type.name = extension.getName();
                    type.description = extension.getDescription();
                    type.monthly_price = extension.getDefaultMonthlyPrice();

                    types.add(type);
                }

            }

            return types;

        } catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public static Model_ProductExtension copyExtension(Model_ProductExtension ext) {

        Model_ProductExtension extension = new Model_ProductExtension();
        extension.name = ext.name;
        extension.description = ext.description;
        extension.color = ext.color;
        extension.type = ext.type;
        extension.active = true;
        extension.removed = false;
        extension.configuration = ext.configuration;

        return extension;
    }

    @JsonIgnore
    public Object getConfiguration(){
        try {

            terminal_logger.trace("getConfiguration: Binding JSON: {}", this.configuration);

            Form<?> form;

            switch (type) {

                case project:{
                    form = Form.form(Configuration_Project.class).bind(Json.parse(configuration));
                    break;
                }

                case database:{
                    form = Form.form(Configuration_Database.class).bind(Json.parse(configuration));
                    break;
                }

                case log:{
                    form = Form.form(Configuration_Log.class).bind(Json.parse(configuration));
                    break;
                }

                case rest_api:{
                    form = Form.form(Configuration_RestApi.class).bind(Json.parse(configuration));
                    break;
                }

                case support:{
                    form = Form.form(Configuration_Support.class).bind(Json.parse(configuration));
                    break;
                }

                case instance:{
                    form = Form.form(Configuration_Instance.class).bind(Json.parse(configuration));
                    break;
                }

                case homer_server:{
                    form = Form.form(Configuration_HomerServer.class).bind(Json.parse(configuration));
                    break;
                }

                default: throw new Exception("Extension type is unknown.");
            }

            if(form.hasErrors()) throw new Exception("Error parsing product configuration. Errors: " + form.errorsAsJson(Lang.forCode("en-US")));

            terminal_logger.debug("getConfiguration: Seems like the configuration is ok");

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Result setConfiguration(Swagger_ProductExtension_New help) throws Exception{

        Object configuration;

        switch (type) {

            case project:{

                Configuration_Project project = new Configuration_Project();
                project.count = help.count;
                project.price = getExtensionTypePrice();

                configuration = project;
                break;
            }

            case database:{

                Configuration_Database database = new Configuration_Database();
                database.price = getExtensionTypePrice();

                configuration = database;
                break;
            }

            case log:{

                Configuration_Log log = new Configuration_Log();
                log.count = help.count;
                log.price = getExtensionTypePrice();

                configuration = log;
                break;
            }

            case rest_api:{

                Configuration_RestApi restApi = new Configuration_RestApi();
                restApi.available_requests = 30L;
                restApi.price = getExtensionTypePrice();

                configuration = restApi;
                break;
            }

            case support:{

                Configuration_Support support = new Configuration_Support();
                support.nonstop = true;
                support.price = getExtensionTypePrice();

                configuration = support;
                break;
            }

            case instance:{

                Configuration_Instance instance = new Configuration_Instance();
                instance.count = 5L;
                instance.price = getExtensionTypePrice();

                configuration = instance;
                break;
            }

            case homer_server:{

                Configuration_HomerServer homerServer = new Configuration_HomerServer();
                homerServer.price = getExtensionTypePrice();

                configuration = homerServer;
                break;
            }

            default: throw new Exception("Extension type is unknown.");
        }

        this.configuration = Json.toJson(configuration).toString();

        return null;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Price {
        @ApiModelProperty(required = true, readOnly = true, value = "in Double - show CZK")
        public Double CZK = 0.0;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show €")
        public Double EUR  = 0.0;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show $")
        public Double USD = 0.0;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      @Transient public boolean create_permission()         {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().permissions_keys.containsKey("ProductExtension_create");}
    @JsonIgnore                                      @Transient public boolean read_permission()           {  return product == null || product.payment_details.person.id.equals(Controller_Security.get_person_id())   || Controller_Security.get_person().permissions_keys.containsKey("ProductExtension_read");  }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean edit_permission()           {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().permissions_keys.containsKey("ProductExtension_edit");  }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean act_deactivate_permission() {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().permissions_keys.containsKey("ProductExtension_act_deactivate"); }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean delete_permission()         {  return Controller_Security.get_person().permissions_keys.containsKey("ProductExtension_delete");}

    public enum permissions{ProductExtension_create, ProductExtension_read, ProductExtension_edit, ProductExtension_act_deactivate, ProductExtension_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_ProductExtension> find = new Model.Finder<>(Model_ProductExtension.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_ProductExtension get_byId(String id) {
        return find.where().eq("id", id).eq("removed", false).findUnique();
    }

    @JsonIgnore
    public static List<Model_ProductExtension> get_byUser(String person_id) {
        return find.where().eq("product.payment_details.person.id", person_id).eq("removed", false).findList();
    }
}