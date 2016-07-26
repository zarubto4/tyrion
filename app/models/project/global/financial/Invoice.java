package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Product;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
public class Invoice extends Model {


    @Id    @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;                         // 5
                                                                public Long facturoid_invoice_id;       // ASD489
                                                                public String invoice_number;           // 2016-0001
                                                                public Date date_of_create;             // 4.5.2016
                                  @Enumerated(EnumType.STRING)  public Payment_status status;


    @JsonIgnore @OneToMany(mappedBy="invoice", cascade = CascadeType.ALL) public List<Invoice_item> invoice_items = new ArrayList<>();

    @JsonIgnore @ManyToOne public Product product;

    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)   public Payment_method payment_method;


    public void set_basic_data(){
        date_of_create = new Date();
        invoice_number = Calendar.getInstance().get(Calendar.YEAR) + "-" + id;
    }


    enum Payment_method{
        bank,
        credit_card
    }

    enum Payment_status{
        paid,
        open,
        sent,
        overdue,
        cancelled
    }

    enum Invoice_State{

    }



}
