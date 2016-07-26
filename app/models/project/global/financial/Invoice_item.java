package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Invoice_item extends Model {

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @ManyToOne public Invoice invoice;


    public String name; // Jméno položky

    public Long   quantity; // Počet položek

    public String unit_name; // Piece,

    public Long   unit_price; // Cena

}
