package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GeneralTariff_Extensions extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;

    public String name;
    public String description;

    public Integer order_position;

    @JsonIgnore  public boolean active; // Tarify nejdou mazat ale jdou Hidnout!!!

    public String color;

    @OneToMany(mappedBy="general_tariff_extension", cascade = CascadeType.ALL) @OrderBy("order_position ASC")  public List<GeneralTariffLabel> labels = new ArrayList<>();
    @JsonIgnore @ManyToOne public GeneralTariff general_tariff;


/* Special Method -------------------------------------------------------------------------------------------------------*/

    @Override
    public void save(){
        order_position = GeneralTariff_Extensions.find.where().eq("general_tariff.id", general_tariff.id).findRowCount() + 1;
        super.save();
    }

    // TODO DELETE?? Dovolím smazat?

    @JsonIgnore @Transient
    public void up(){

        general_tariff.extensionses.get(order_position-2).order_position = this.order_position ;
        general_tariff.extensionses.get(order_position-2).update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        general_tariff.extensionses.get(order_position).order_position =  general_tariff.labels.get(order_position).order_position-1;
        general_tariff.extensionses.get(order_position).update();

        this.order_position += 1;
        this.update();

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,GeneralTariff_Extensions> find = new Finder<>(GeneralTariff_Extensions.class);



}
