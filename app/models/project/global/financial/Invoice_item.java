package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Currency;

import javax.persistence.*;

@Entity
public class Invoice_item extends Model {

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Invoice invoice;


    public String name; // Jméno položky

    public Long   quantity; // Počet položek

    public String unit_name; // Piece,

    public Double unit_price; // Cena / Musí být public - zasílá se do fakturoidu

    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)  public Currency currency;


    @Transient public Double vat = 21.0;

    @JsonProperty @Transient public String vat_rate(){return this.vat.toString();}

    @JsonProperty @Transient public Double unit_price_without_vat(){ return  unit_price  - (unit_price * (vat / (100 + vat)) );}


    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<Long,Invoice_item> find = new Finder<>(Invoice_item.class);


}
