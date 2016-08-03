package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
public class Invoice_item extends Model {

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @ManyToOne public Invoice invoice;


    public String name; // Jméno položky

    public Long   quantity; // Počet položek

    public String unit_name; // Piece,

    public Double unit_price; // Cena



    @Transient public Double vat = 21.0;

    @JsonProperty @Transient public String vat_rate(){return this.vat.toString();}

    @JsonProperty @Transient public Double unit_price_without_vat(){ return  unit_price  - (unit_price * (vat / (100 + vat)) );}

    @JsonProperty @Transient public Double unit_price_with_vat(){return unit_price;}



}
