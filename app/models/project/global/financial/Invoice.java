package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Product;
import utilities.Server;
import utilities.goPay.helps_objects.enums.Payment_method;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Invoice extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id    @GeneratedValue(strategy = GenerationType.SEQUENCE)  public Long id;                           // 5

                                                   @JsonIgnore  public Long   facturoid_invoice_id;       // Id určené ze strany Fakturid
                                                   @JsonIgnore  public String facturoid_pdf_url;          // Adresa ke stáhnutí faktury
                                                                public String invoice_number;             // 2016-0001 - Generuje Fakutoid

                                                   @JsonIgnore  public Long gopay_id;                     // 1213231123
                                                   @JsonIgnore  public String gopay_order_number;         // ON-783837426-1469877748551

    @JsonIgnore public boolean proforma;

                                                                public Date date_of_create;               // 4.5.2016



    @JsonIgnore @OneToMany(mappedBy="invoice", cascade = CascadeType.ALL) public List<Invoice_item> invoice_items = new ArrayList<>();

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL) public Product product;

    @JsonIgnore   @Enumerated(EnumType.STRING)  public Payment_status status;
    @JsonIgnore   @Enumerated(EnumType.STRING)  public Payment_method method;


/* JSON PROPERTY METHOD -----------------------------------------------------------------------------------------------*/


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String pdf_link()  {  return facturoid_pdf_url != null ?  Server.tyrion_serverAddress + "/product/invoice/pdf/" + id : null; }


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  {
        return status.name().equals(Payment_status.sent.name());
    }


    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_status(){

        switch (status) {
            case paid: {return  "Invoice is paid.";}
            case sent: {return  "Please pay this invoices.";}
            case created_waited: {return  "Sent to your accounting officer we are waiting for confirmation from the bank.";}
            case cancelled: {return  "Invoice is canceled.";}
            default: return  "Undefined state";
        }

    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){

        switch (method) {
            case bank:        {return  "Bank transfer."; }
            case credit_card: {return  "Credit Card Payment."; }
            default: return   "Undefined state";
        }

    }




/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission() {  return true;  }
    @JsonIgnore @Transient public boolean read_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean send_reminder()     {  return true;  }
    @JsonIgnore @Transient public boolean edit_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean delete_permission() {  return true;  }
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<Long,Invoice> find = new Finder<>(Invoice.class);

/* ENUM values ---------------------------------------------------------------------------------------------------------*/


    public enum Payment_status{
        paid,   // Uhrazeno
        sent,   // Zasláno u uhrazené

        created_waited,

        cancelled // Zrušeno
    }

}
