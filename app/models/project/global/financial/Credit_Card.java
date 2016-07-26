package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import models.project.global.Product;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.persistence.*;


@Entity
public class Credit_Card  extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;


    @JsonIgnore  @CreditCardNumber                                      public String credit_card_number;
    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Credit_Card_Type credit_card_type;
    @JsonIgnore                                                         public int cvc;
    @JsonIgnore                                                         public int expiry_month;
    @JsonIgnore                                                         public int expiry_year;
    @ApiModelProperty(required = true)                                  public String card_owner_name;


    @JsonIgnore @OneToOne @JoinColumn(name="product_id")                public Product product;
    @JsonIgnore @OneToOne @JoinColumn(name="person_id")                 public Person credit_card_owner;

    public String credit_card_number() { return "**** **** **** *" + credit_card_number.substring(15); }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Credit_Card> find = new Finder<>(Credit_Card.class);



    enum Credit_Card_Type{
        master_card,
        visa
    }

}
