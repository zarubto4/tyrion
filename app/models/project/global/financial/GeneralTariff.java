package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GeneralTariff extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;

    public String tariff_name;
    public String identificator;

    public boolean company_details_required;
    public boolean required_payment_mode;
    public boolean required_payment_method;

    @JsonIgnore  public Double usd;
    @JsonIgnore  public Double eur;
    @JsonIgnore  public Double czk;

    public String color;

    @JsonIgnore public boolean bank_transfer_support;
    @JsonIgnore public boolean credit_card_support;

    @JsonIgnore public boolean mode_annually;
    @JsonIgnore public boolean mode_credit;


    @OneToMany(mappedBy="general_tariff", cascade = CascadeType.ALL) public List<GeneralTariffLabel> labels = new ArrayList<>();


/* JSON PROPERTY METHOD -----------------------------------------------------------------------------------------------*/

    @JsonProperty public List<String> payment_methods(){

        List<String> methods = new ArrayList<>();

        if(bank_transfer_support) methods.add("bank_transfer");
        if(credit_card_support)   methods.add("credit_card");

        return methods;
    }


    @JsonProperty public List<String> payment_modes(){

        List<String> modes = new ArrayList<>();

        if(mode_annually)  modes.add("mode_annually");
        if(mode_credit)    modes.add("mode_credit");

        return modes;
    }

    @JsonProperty public Price price(){

        Price price = new Price();
        price.CZK = czk;
        price.EUR = eur;
        price.USD = usd;
        return price;

    }



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,GeneralTariff> find = new Finder<>(GeneralTariff.class);


/*  Class --------------------------------------------------------------------------------------------------------------*/

      public class Price {
        @ApiModelProperty(required = true, readOnly = true, value = "in Double - show CZK")
        public Double CZK;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show €")
        public Double EUR;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show $")
        public Double USD;
    }


}

