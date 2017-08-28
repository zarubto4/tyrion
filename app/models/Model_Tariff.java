package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_BusinessModel;
import utilities.enums.Enum_Payment_method;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Tariff", description = "Model of Tariff")
@Table(name="Tariff")
public class Model_Tariff extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Tariff.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                        @Id public String id;

                            public String name;
                            public String description;
    @Column(unique = true)  public String identifier;

                @JsonIgnore public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!

    @Enumerated(EnumType.STRING) @JsonIgnore public Enum_BusinessModel business_model;

                            public Integer order_position;

                            public boolean company_details_required;
                            public boolean payment_details_required;
                            public boolean payment_method_required;

                @JsonIgnore public Long credit_for_beginning;  // Kredit, který se po zaregistrování připíše uživatelovi k dobru. (Náhrada Trial Verze)

                            public String color;
                            public String awesome_icon;

                @OneToMany(mappedBy="tariff",          cascade = CascadeType.ALL, fetch = FetchType.EAGER) @OrderBy("order_position ASC") public List<Model_TariffLabel> labels = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="tariff_included", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_ProductExtension> extensions_included = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="tariff_optional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_ProductExtension> extensions_optional = new ArrayList<>();


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty public List<Pair> payment_methods(){

        List<Pair> methods = new ArrayList<>();

        methods.add( new Pair( Enum_Payment_method.bank_transfer.name(), "Bank transfers") );
        methods.add( new Pair( Enum_Payment_method.credit_card.name()  , "Credit Card Payment"));

        return methods;
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient public Long credit_for_beginning(){
        if(!this.edit_permission()) return null;
        return credit_for_beginning;
    }


    @JsonProperty public List<String> payment_currency(){

        List<String> payment_currency = new ArrayList<>();
        payment_currency.add("CZK");
        payment_currency.add("EUR");
        payment_currency.add("USD");

        return payment_currency;
    }

    @JsonProperty public Price price(){

        Price price = new Price();
        price.USD = total_per_month();
        return price;

    }

    @JsonProperty
    public List<Model_ProductExtension> extensions_included(){
        return  Model_ProductExtension.find.where().eq("tariff_included.id", id).eq("active", true).orderBy("order_position").findList();
    }


    @JsonProperty
    public List<Model_ProductExtension> extensions_optional(){
        return  Model_ProductExtension.find.where().eq("tariff_optional.id", id).eq("active", true).orderBy("order_position").findList();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonProperty
    public Double total_per_month(){
        Long total_price = 0L;
        for(Model_ProductExtension extension : this.extensions_included){
            Long price = extension.getDailyPrice();

            if(price != null)
                total_price += price;
        }
        return (double) total_price * 30 / 1000;
    }



/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        order_position = Model_Tariff.find.findRowCount() + 1;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString().substring(0, 8);
            if (Model_Tariff.find.byId(this.id) == null) break;
        }
        super.save();
    }


    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();
    }

    @JsonIgnore @Override
    public void delete(){
        terminal_logger.debug("delete :: Update object Id: {}",  this.id);
        super.delete();
    }


/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        Model_Tariff up = Model_Tariff.find.where().eq("order_position", (order_position-1) ).findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_Tariff down = Model_Tariff.find.where().eq("order_position", (order_position+1) ).findUnique();
        if(down == null)return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

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

    public class Pair {

        public Pair(String json_identifier, String user_description){
            this.json_identifier = json_identifier;
            this.user_description = user_description;
        }

        @ApiModelProperty(required = true, readOnly = true)
        public String json_identifier;

        @ApiModelProperty(required = true, readOnly = true)
        public String user_description;

    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Tariff_create");}
    @JsonIgnore   @Transient public boolean read_permission()  {  return Controller_Security.get_person().permissions_keys.containsKey("Tariff_read") || active;}
    @JsonProperty @Transient public boolean edit_permission()  {  return Controller_Security.get_person().permissions_keys.containsKey("Tariff_edit");}
    @JsonProperty @Transient public boolean update_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Tariff_update");}
    @JsonProperty @Transient public boolean delete_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Tariff_delete");}

    public enum permissions{Tariff_create, Tariff_read, Tariff_edit, Tariff_update, Tariff_delete, Tariff_register_new_device, Tariff_bootloader,  Tariff_c_program_edit_permission, Tariff_test_c_program_edit_permission}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Tariff get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Tariff> find = new Finder<>(Model_Tariff.class);

}

