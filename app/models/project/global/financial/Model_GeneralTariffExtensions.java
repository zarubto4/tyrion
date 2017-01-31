package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Model_Product;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of GeneralTariff_Extensions",
        value = "GeneralTariff_Extensions")
@Table(name="GeneralTariffExt")
public class Model_GeneralTariffExtensions extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

             @Id public String id;
                 public String name;
                 public String description;

    @JsonIgnore  public Integer order_position;

    @JsonIgnore  public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!

                 public String color;

    @JsonIgnore  public Double usd;             // Cena je za jeden den!!
    @JsonIgnore  public Double eur;             // Cena je za jeden den!!
    @JsonIgnore  public Double czk;             // Cena je za jeden den!!

    @OneToMany(mappedBy="extensions", cascade = CascadeType.ALL) @OrderBy("order_position ASC")  public List<Model_GeneralTariffLabel> labels = new ArrayList<>();


    @JsonIgnore  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public Model_GeneralTariff general_tariff_included;
    @JsonIgnore  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public Model_GeneralTariff general_tariff_optional;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Product> products = new ArrayList<>();


    @JsonProperty public Price price(){

        Price price = new Price();
        price.CZK = czk * 30.0;       // Kvuli "průměrné měsíční ceně
        price.EUR = eur * 30.0;       // Kvuli "průměrné měsíční ceně
        price.USD = usd * 30.0;       // Kvuli "průměrné měsíční ceně
        return price;

    }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString().substring(0,8);
            if (Model_GeneralTariffExtensions.find.byId(this.id) == null) break;
        }
        if(general_tariff_included != null) {
            order_position = Model_GeneralTariffExtensions.find.where().eq("general_tariff_included.id", general_tariff_included.id).findRowCount() + 1;
        }else {
            order_position = Model_GeneralTariffExtensions.find.where().eq("general_tariff_optional.id",  general_tariff_optional.id).findRowCount() + 1;
        }
        super.save();
    }

    @JsonIgnore @Transient
    public void up(){

        if(general_tariff_included != null) {
            general_tariff_included.extensions_included.get(order_position - 2).order_position = this.order_position;
            general_tariff_included.extensions_included.get(order_position - 2).update();
        }else {

            general_tariff_optional.extensions_optional.get(order_position - 2).order_position = this.order_position;
            general_tariff_optional.extensions_optional.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        if(general_tariff_included != null){

            general_tariff_included.extensions_included.get(order_position).order_position = general_tariff_included.labels.get(order_position).order_position - 1;
            general_tariff_included.extensions_included.get(order_position).update();

        }else{

            general_tariff_optional.extensions_optional.get(order_position).order_position = general_tariff_optional.labels.get(order_position).order_position - 1;
            general_tariff_optional.extensions_optional.get(order_position).update();
        }

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Price {
        @ApiModelProperty(required = true, readOnly = true, value = "in Double - show CZK - Average price per month")
        public Double CZK;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show € - Average price per month")
        public Double EUR;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show $ - Average price per month")
        public Double USD;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_GeneralTariffExtensions> find = new Finder<>(Model_GeneralTariffExtensions.class);

}
