package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class GeneralTariffLabel extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;

    @JsonIgnore @ManyToOne public GeneralTariff general_tariff;
    @JsonIgnore @ManyToOne public GeneralTariff_Extensions general_tariff_extension;

    public String label;
    public String description;
    public String icon;
    public Integer order_position;


/* Special Method -------------------------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        if(general_tariff != null) {
            order_position = GeneralTariffLabel.find.where().eq("general_tariff.id", general_tariff.id).findRowCount() + 1;
        }else {
            order_position = GeneralTariffLabel.find.where().eq("general_tariff_extension.id", general_tariff_extension.id).findRowCount() + 1;
        }
        super.save();
    }

    @Override
    public void delete(){

        int pointer = 1;

        if(general_tariff!=null) {
            for (GeneralTariffLabel label : general_tariff.labels) {

                if (!label.id.equals(this.id)) {
                    label.order_position = pointer++;
                    label.update();
                }
            }
        }else {
            for (GeneralTariffLabel label : general_tariff_extension.labels) {

                if (!label.id.equals(this.id)) {
                    label.order_position = pointer++;
                    label.update();
                }
            }
        }
        super.delete();
    }

    @JsonIgnore @Transient
    public void up(){

        if(general_tariff != null) {
            general_tariff.labels.get(order_position - 2).order_position = this.order_position;
            general_tariff.labels.get(order_position - 2).update();
        }else {
            general_tariff_extension.labels.get(order_position - 2).order_position = this.order_position;
            general_tariff_extension.labels.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        if(general_tariff != null) {
            general_tariff.labels.get(order_position).order_position =  general_tariff.labels.get(order_position).order_position-1;
            general_tariff.labels.get(order_position).update();
        }else {
            general_tariff_extension.labels.get(order_position).order_position =  general_tariff_extension.labels.get(order_position).order_position-1;
            general_tariff_extension.labels.get(order_position).update();
        }

        this.order_position += 1;
        this.update();

    }



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,GeneralTariffLabel> find = new Finder<>(GeneralTariffLabel.class);


}