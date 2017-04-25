package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.libs.Json;
import utilities.enums.Enum_ExtensionType;
import utilities.financial.*;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_ProductExtension_Type;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of ProductExtension",
        value = "ProductExtension")
public class Model_ProductExtension extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_ProductExtension.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                    @Id @ApiModelProperty(required = true) public String id;
                                        @ApiModelProperty(required = true) public String name;
                                        @ApiModelProperty(required = true) public String description;

                                        @ApiModelProperty(required = true) public String color;

           @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_ExtensionType type;
                                        @ApiModelProperty(required = true) public String config;
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
            price.USD = ((double) getPrice()) / 1000;
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

    @JsonIgnore @Transient
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

    @JsonIgnore @Transient
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

    @JsonIgnore
    public Long getPrice() {
        try {

            Extension extension = getExtensionType();

            if (extension == null) return null;

            return extension.getPrice(getConfig());

        } catch (Exception e) {
            terminal_logger.internalServerError("Model_ProductExtension:: getPrice:", e);
            return null;
        }
    }

    @JsonIgnore
    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public Config getConfig() {
        Form<Config> form = Form.form(Config.class).bind(Json.parse(this.config));
        if(form.hasErrors()) return null;
        return form.get();
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
            terminal_logger.internalServerError("Model_ProductExtension:: getExtensionType:", e);
            return null;
        }
    }

    @JsonIgnore
    public String getExtensionTypeName(){
        return this.type.name();
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
            terminal_logger.internalServerError("Model_ProductExtension:: getExtensionTypes:", e);
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
        extension.config = ext.config;

        return extension;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public static class Config {

        public Long price;
        public int count;
    }

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

    @JsonIgnore                                      @Transient public boolean create_permission()         {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().has_permission("ProductExtension_create");}
    @JsonIgnore                                      @Transient public boolean read_permission()           {  return product == null || product.payment_details.person.id.equals(Controller_Security.get_person_id())   || Controller_Security.get_person().has_permission("ProductExtension_read");  }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean edit_permission()           {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().has_permission("ProductExtension_edit");  }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean act_deactivate_permission() {  return (product != null && product.payment_details.person.id.equals(Controller_Security.get_person_id())) || Controller_Security.get_person().has_permission("ProductExtension_act_deactivate"); }
    @JsonProperty @ApiModelProperty(required = true) @Transient public boolean delete_permission()         {  return Controller_Security.get_person().has_permission("ProductExtension_delete");}

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