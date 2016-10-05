package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class GeneralTariffLabel extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;

    @JsonIgnore @ManyToOne public GeneralTariff general_tariff;

    public String label;
    public String description;
    public String icon;


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,GeneralTariffLabel> find = new Finder<>(GeneralTariffLabel.class);


}