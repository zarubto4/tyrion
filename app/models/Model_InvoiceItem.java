package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Currency;

import javax.persistence.*;

@Entity
@ApiModel(value = "InvoiceItem", description = "Model of InvoiceItem")
@Table(name="InvoiceItem")
public class Model_InvoiceItem extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                       public Model_Invoice invoice;

                                                                         public String name; // Jméno položky
                                                                         public Long   quantity; // Počet položek
                                                                         public String unit_name; // Piece,
                                                            @JsonIgnore  public Long unit_price; // Cena / Musí být public - zasílá se do fakturoidu

    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)    public Enum_Currency currency;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String vat_rate(){

        Long v = vat / 1000;

        return v.toString();
    }

    @JsonProperty @Transient public Double unit_price_without_vat(){ return  ((double) (unit_price  - (unit_price * (vat / (100 + vat))))) / 1000;}

    @JsonProperty public Double unit_price() { return ((double) unit_price) / 1000;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient public Long vat = (long) 21000;

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<Long,Model_InvoiceItem> find = new Finder<>(Model_InvoiceItem.class);
}
