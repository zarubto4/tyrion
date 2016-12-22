package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Model_Product;
import utilities.enums.Payment_method;
import utilities.enums.Payment_mode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of GeneralTariff",
        value = "GeneralTariff")
public class Model_GeneralTariff extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                        @Id public String id;

                            public String tariff_name;
                            public String tariff_description;
    @Column(unique = true)  public String identificator;

    @JsonIgnore             public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!


    public boolean company_details_required;
    public boolean required_payment_mode;
    public boolean required_payment_method;

    @JsonIgnore  public boolean required_paid_that; // Říká, zda se po zaregistrování okamžitě vytvoří faktura a další procedury pro zaplacení
    @JsonIgnore  public Integer number_of_free_months;


    @JsonIgnore  public Double usd;
    @JsonIgnore  public Double eur;
    @JsonIgnore  public Double czk;

    public String color;

    @JsonIgnore public boolean bank_transfer_support;
    @JsonIgnore public boolean credit_card_support;


    @JsonIgnore public boolean mode_annually;
    @JsonIgnore public boolean mode_credit;
    @JsonIgnore public boolean free_tariff;


                @OneToMany(mappedBy="general_tariff", cascade = CascadeType.ALL, fetch = FetchType.EAGER) @OrderBy("order_position ASC") public List<Model_GeneralTariffLabel> labels = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="general_tariff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_GeneralTariffExtensions> extensions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="general_tariff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)                                 public List<Model_Product> product = new ArrayList<>(); //Vazba na uživateli zaregistrované produkty

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty public List<Pair> payment_methods(){

        List<Pair> methods = new ArrayList<>();

        if(bank_transfer_support) methods.add( new Pair( Payment_method.bank_transfer.name(), "Bank transfers") );
        if(credit_card_support)   methods.add( new Pair( Payment_method.credit_card.name()  , "Credit Card Payment"));
        if(free_tariff)           methods.add( new Pair( Payment_method.free.name()         , "I want it free"));

        return methods;
    }


    @JsonProperty public List<Pair> payment_modes(){

        List<Pair> modes = new ArrayList<>();

        if(mode_annually)  modes.add( new Pair( Payment_mode.monthly.name()   , "Annual monthly / yearly payment"));
        if(mode_credit)    modes.add( new Pair( Payment_mode.per_credit.name(), "Pre-paid credit"));
        if(free_tariff)    modes.add( new Pair( Payment_mode.free.name()      , "I want it free"));

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
        price.CZK = czk;
        price.EUR = eur;
        price.USD = usd;
        return price;

    }

    @JsonProperty public List<Model_GeneralTariffExtensions> extensions(){
        return  Model_GeneralTariffExtensions.find.where().eq("general_tariff.id", id).eq("active", true).orderBy("order_position").findList();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GeneralTariff.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void delete(){
        throw new IllegalAccessError("Delete is not supported under General Tariff");
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Price {
        @ApiModelProperty(required = true, readOnly = true, value = "in Double - show CZK")
        public Double CZK;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show €")
        public Double EUR;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show $")
        public Double USD;
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

