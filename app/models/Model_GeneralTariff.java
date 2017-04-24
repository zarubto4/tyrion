package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GeneralTariff", description = "Model of GeneralTariff")
public class Model_GeneralTariff extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(_Model_ExampleModelName.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                        @Id public String id;

                            public String tariff_name;
                            public String tariff_description;
    @Column(unique = true)  public String identificator;

    @JsonIgnore             public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!


    @JsonIgnore  public Integer order_position;

    public boolean company_details_required;
    public boolean required_payment_mode;
    public boolean required_payment_method;

    @JsonIgnore  public boolean required_paid_that; // Říká, zda se po zaregistrování okamžitě vytvoří faktura a další procedury pro zaplacení

    @JsonIgnore  public Double credit_for_beginning;    // Kredit, který se po zaregistrování připíše uživatelovi k dobru. (Náhrada Trial Verze)
                                                        // Je to V USD!!! - Nutné přepočítávat!!!

    @JsonIgnore  public Double price_in_usd;


    public String color;

    @JsonIgnore public boolean bank_transfer_support;
    @JsonIgnore public boolean credit_card_support;


    @JsonIgnore public boolean mode_annually;
    @JsonIgnore public boolean mode_credit;
    @JsonIgnore public boolean free_tariff;


                @OneToMany(mappedBy="general_tariff",          cascade = CascadeType.ALL, fetch = FetchType.EAGER) @OrderBy("order_position ASC") public List<Model_GeneralTariffLabel> labels = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="general_tariff_included", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_GeneralTariffExtensions> extensions_included = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="general_tariff_optional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_GeneralTariffExtensions> extensions_optional = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="general_tariff",           cascade = CascadeType.ALL, fetch = FetchType.LAZY)                                public List<Model_Product> product = new ArrayList<>(); //Vazba na uživateli zaregistrované produkty


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty public List<Pair> payment_methods(){

        List<Pair> methods = new ArrayList<>();

        if(bank_transfer_support) methods.add( new Pair( Enum_Payment_method.bank_transfer.name(), "Bank transfers") );
        if(credit_card_support)   methods.add( new Pair( Enum_Payment_method.credit_card.name()  , "Credit Card Payment"));
        if(free_tariff)           methods.add( new Pair( Enum_Payment_method.free.name()         , "I want it free"));

        return methods;
    }


    @JsonProperty public List<Pair> payment_modes(){

        List<Pair> modes = new ArrayList<>();

        if(mode_annually)  modes.add( new Pair( Enum_Payment_mode.monthly.name()   , "Annual monthly / yearly payment"));
        if(mode_credit)    modes.add( new Pair( Enum_Payment_mode.per_credit.name(), "Pre-paid credit"));
        if(free_tariff)    modes.add( new Pair( Enum_Payment_mode.free.name()      , "I want it free"));

        return modes;
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
        price.USD = price_in_usd;
        return price;

    }

    @JsonProperty public List<Model_GeneralTariffExtensions> extensions_included(){
        return  Model_GeneralTariffExtensions.find.where().eq("general_tariff_included.id", id).eq("active", true).orderBy("order_position").findList();
    }


    @JsonProperty public List<Model_GeneralTariffExtensions> extensions_optional(){
        return  Model_GeneralTariffExtensions.find.where().eq("general_tariff_optional.id", id).eq("active", true).orderBy("order_position").findList();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public double total_per_month(){
        double total_price = 0.0;
        for(Model_GeneralTariffExtensions extension : this.extensions_included){
            if(extension.price_in_usd != null)
                total_price += extension.price_in_usd;
        }
        return  total_price*30;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    @JsonIgnore @Override
    public void save() {

        order_position = Model_GeneralTariff.find.findRowCount() + 1;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString().substring(0, 8);
            if (Model_GeneralTariff.find.byId(this.id) == null) break;
        }
        super.save();
    }


    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();
    }

    @JsonIgnore @Override
    public void delete(){

        terminal_logger.error("delete :: This object is not legitimate to remove. ");
        throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        Model_GeneralTariff up = Model_GeneralTariff.find.where().eq("order_position", (order_position-1) ).findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_GeneralTariff down = Model_GeneralTariff.find.where().eq("order_position", (order_position+1) ).findUnique();
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

        public Pair(String json_identificator, String user_description){
            this.json_identificator = json_identificator;
            this.user_description = user_description;
        }

        @ApiModelProperty(required = true, readOnly = true)
        public String json_identificator;

        @ApiModelProperty(required = true, readOnly = true)
        public String user_description;

    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_GeneralTariff> find = new Finder<>(Model_GeneralTariff.class);

}

