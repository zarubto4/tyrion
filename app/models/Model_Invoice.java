package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_status;
import utilities.enums.Enum_Payment_warning;

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

        @ApiModelProperty(required = true, readOnly = true) @Id public String id;                         // 5

                                                   @JsonIgnore  public Long   fakturoid_id;                 // Id určené ze strany Fakturoid
                                                   @JsonIgnore  public String fakturoid_pdf_url;          // Adresa ke stáhnutí faktury
            @ApiModelProperty(required = true, readOnly = true) public String invoice_number;             // 2016-0001 - Generuje Fakturoid

                                                   @JsonIgnore  public Long   gopay_id;                   // 1213231123
                                                   @JsonIgnore  public String gopay_order_number;         // ON-783837426-1469877748551
                                                   @JsonIgnore  public String gw_url;

                                                   @JsonIgnore  public boolean proforma;

                                                   @JsonIgnore  public Long   proforma_id;                 // Id proformy ze které je faktura
                                                   @JsonIgnore  public String proforma_pdf_url;

    @ApiModelProperty(required = true, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                          public Date created;

    @ApiModelProperty(required = false, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                          public Date paid;

    @ApiModelProperty(required = true, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                          public Date overdue;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)              public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_InvoiceItem> invoice_items = new ArrayList<>();

                                                        @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_status status;
                                                        @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_method method;
                                                        @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_warning warning;


/* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String pdf_link()  {  return fakturoid_pdf_url != null ?  Server.tyrion_serverAddress + "/invoice/pdf/" + id : null; }


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  {
        return status == Enum_Payment_status.pending || status == Enum_Payment_status.overdue;
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public String gw_url()  {
        if (status == Enum_Payment_status.pending || status == Enum_Payment_status.overdue) return this.gw_url;

        return null;
    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_status(){

        switch (status) {
            case paid           : return  "Invoice is paid.";
            case pending        : return  "Invoice needs to be paid.";
            case overdue        : return  "Invoice is overdue.";
            case canceled       : return  "Invoice is canceled.";
            default             : return  "Undefined state";
        }
    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){
        switch (method) {
            case bank_transfer : return  "Bank transfer.";
            case credit_card   : return  "Credit Card Payment.";
            default            : return  "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Model_InvoiceItem> getInvoiceItems() {

        if(invoice_items != null) return invoice_items;

        return Model_InvoiceItem.find.where().eq("invoice.id", this.id).findList();
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
        if (product == null) product = Model_Product.get_byInvoice(this.id);
        return product;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        this.created = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

        super.save();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission() {  return this.getProduct().payment_details.person.id.equals(Controller_Security.getPerson().id);}
    @JsonIgnore @Transient public boolean read_permission()   {  return this.getProduct().payment_details.person.id.equals(Controller_Security.getPerson().id);}
    @JsonIgnore @Transient public boolean send_reminder()     {  return true;  }
    @JsonIgnore @Transient public boolean edit_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean delete_permission() {  return true;  }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Invoice> find = new Finder<>(Model_Invoice.class);

}
