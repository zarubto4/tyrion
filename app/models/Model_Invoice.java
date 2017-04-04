package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_status;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Invoice",
        value = "Invoice")
public class Model_Invoice extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                           @Id  public String id;                         // 5

                                                   @JsonIgnore  public Long   facturoid_invoice_id;       // Id určené ze strany Fakturid
                                                   @JsonIgnore  public String facturoid_pdf_url;          // Adresa ke stáhnutí faktury
                                                                public String invoice_number;             // 2016-0001 - Generuje Fakutoid

                                                   @JsonIgnore  public Long   gopay_id;                   // 1213231123
                                                   @JsonIgnore  public String gopay_order_number;         // ON-783837426-1469877748551

                                                   @JsonIgnore  public boolean proforma;

                                                                public Date created;               // 4.5.2016



    @JsonIgnore @OneToMany(mappedBy="invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_InvoiceItem> invoice_items = new ArrayList<>();

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)                       public Model_Product product;

                                                        @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_status status;
                                                        @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_method method;


/* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String pdf_link()  {  return facturoid_pdf_url != null ?  Server.tyrion_serverAddress + "/invoice/pdf/" + id : null; }


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  {
        return status.name().equals(Enum_Payment_status.sent.name())
               || status.name().equals(Enum_Payment_status.created_waited.name()
        )  ;
    }


    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_status(){

        switch (status) {
            case paid           : return  "Paid";
            case sent           : return  "Please pay this invoices.";
            case created_waited : return  "Sent to your accounting officer we are waiting for confirmation from the bank_transfer.";
            case cancelled      : return  "Invoice is canceled.";
            default             : return  "Undefined state";
        }
    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){
        switch (method) {
            case bank_transfer : return  "Bank transfer.";
            case credit_card   : return  "Credit Card Payment.";
            default            : return   "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Model_InvoiceItem> getInvoice_items() {
        return invoice_items;
    }


    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public double total_price() {
        double total_price = 0.0;
        for(Model_InvoiceItem  item : invoice_items){
            total_price += item.unit_price;
        }
        return total_price;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Product getProduct() {
        return product;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

        super.save();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission() {  return true;  }
    @JsonIgnore @Transient public boolean read_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean send_reminder()     {  return true;  }
    @JsonIgnore @Transient public boolean edit_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean delete_permission() {  return true;  }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Invoice> find = new Finder<>(Model_Invoice.class);

}
