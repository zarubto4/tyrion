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
public class Model_GeneralTariffExtensions extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

             @Id public String id;
                 public String name;
                 public String description;

    @JsonIgnore  public Integer order_position;

    @JsonIgnore  public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!

                 public String color;

    @JsonIgnore  public Double usd;
    @JsonIgnore  public Double eur;
    @JsonIgnore  public Double czk;

    @OneToMany(mappedBy="extensions", cascade = CascadeType.ALL) @OrderBy("order_position ASC")  public List<Model_GeneralTariffLabel> labels = new ArrayList<>();


    @JsonIgnore  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_GeneralTariff general_tariff;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Product> products = new ArrayList<>();


    @JsonProperty public Price price(){

        Price price = new Price();
        price.CZK = czk;
        price.EUR = eur;
        price.USD = usd;
        return price;

    }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GeneralTariffExtensions.find.byId(this.id) == null) break;
        }
        order_position = Model_GeneralTariffExtensions.find.where().eq("general_tariff.id", general_tariff.id).findRowCount() + 1;
        super.save();
    }

    // TODO DELETE?? Dovolím smazat?

    @JsonIgnore @Transient
    public void up(){

        general_tariff.extensions.get(order_position-2).order_position = this.order_position ;
        general_tariff.extensions.get(order_position-2).update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        general_tariff.extensions.get(order_position).order_position =  general_tariff.labels.get(order_position).order_position-1;
        general_tariff.extensions.get(order_position).update();

        this.order_position += 1;
        this.update();

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

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_GeneralTariffExtensions> find = new Finder<>(Model_GeneralTariffExtensions.class);

}
