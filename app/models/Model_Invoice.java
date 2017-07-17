package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.*;
import utilities.financial.extensions.configurations.*;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Invoice", description = "Model of Invoice")
public class Model_Invoice extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Invoice.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

        @ApiModelProperty(required = true, readOnly = true) @Id public String id;

                                                   @JsonIgnore  public Long   fakturoid_id;         // Id určené ze strany Fakturoid
                                                   @JsonIgnore  public String fakturoid_pdf_url;    // Adresa ke stáhnutí faktury
            @ApiModelProperty(required = true, readOnly = true) public String invoice_number;       // 2016-0001 - Generuje Fakturoid

                                                   @JsonIgnore  public Long   gopay_id;             // 1213231123
                                                   @JsonIgnore  public String gopay_order_number;   // ON-783837426-1469877748551
                                                   @JsonIgnore  public String gw_url;

                                                   @JsonIgnore  public boolean proforma;

                                                   @JsonIgnore  public Long   proforma_id;          // Id proformy ze které je faktura
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

    @JsonIgnore @OneToMany(mappedBy="invoice",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_InvoiceItem> invoice_items = new ArrayList<>();

                    @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_status status;
                    @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_method method;
                    @JsonIgnore   @Enumerated(EnumType.STRING)  public Enum_Payment_warning warning;

/* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String invoice_pdf_link()  { return fakturoid_pdf_url != null ?  Server.tyrion_serverAddress + "/invoice/pdf/invoice/" + id : null; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String proforma_pdf_link() { return proforma_pdf_url != null ?  Server.tyrion_serverAddress + "/invoice/pdf/proforma/" + id : null; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  { return status == Enum_Payment_status.pending || status == Enum_Payment_status.overdue; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public String gw_url()  {
        if (status == Enum_Payment_status.pending || status == Enum_Payment_status.overdue) return this.gw_url;

        return null;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String payment_status(){

        switch (status) {
            case paid           : return  "Invoice is paid.";
            case pending        : return  "Invoice needs to be paid.";
            case overdue        : return  "Invoice is overdue.";
            case canceled       : return  "Invoice is canceled.";
            default             : return  "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){
        switch (method) {
            case bank_transfer : return  "Bank transfer.";
            case credit_card   : return  "Credit Card Payment.";
            default            : return  "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Model_InvoiceItem> invoice_items() {

        if(invoice_items != null) return invoice_items;

        return Model_InvoiceItem.find.where().eq("invoice.id", this.id).findList();
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public double price() {
        return ((double)this.total_price()) / 1000;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Product getProduct() {
        if (product == null) product = Model_Product.get_byInvoice(this.id);
        return product;
    }

    /**
     * Method takes all extensions and converts them to invoice items.
     * Prerequisite is an assigned product to the invoice.
     */
    @JsonIgnore
    public void setItems() {

        Model_Product product = getProduct();
        if (product == null) throw new NullPointerException("Product is not set yet for this invoice.");

        for (Model_ProductExtension extension : product.extensions) {

            Model_InvoiceItem item = new Model_InvoiceItem();
            item.name = extension.name;
            item.unit_price = extension.getDailyPrice() * 30;

            switch (extension.type) {
                case project: {
                    item.quantity = ((Configuration_Project) extension.getConfiguration()).count;
                    break;
                }
                case log: {
                    item.quantity = ((Configuration_Log) extension.getConfiguration()).count;
                    break;
                }
                case rest_api: {
                    item.quantity = ((Configuration_RestApi) extension.getConfiguration()).available_requests;
                    break;
                }
                case instance: {
                    item.quantity = ((Configuration_Instance) extension.getConfiguration()).count;
                    break;
                }
                case participant: {
                    item.quantity = ((Configuration_Participant) extension.getConfiguration()).count;
                    break;
                }
                default: item.quantity = 1L;
            }

            item.unit_name = "Pcs";
            item.currency = Enum_Currency.USD;

            invoice_items.add(item);
        }
    }

    @JsonIgnore
    public Long total_price() {
        Long total_price = (long) 0;
        for(Model_InvoiceItem  item : invoice_items){
            total_price += item.unit_price;
        }
        return total_price;
    }

    @JsonIgnore @Override
    public void save() {

        this.created = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notificationInvoiceNew(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.info)
                    .setText(new Notification_Text().setText("See new invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" for your product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText("."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationInvoiceNew:", e);
        }
    }

    @JsonIgnore
    public void notificationInvoiceReminder(String message){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.high)
                    .setLevel(Enum_Notification_level.warning)
                    .setText(new Notification_Text().setText("Payment for this product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText(" was not received. See this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" and resolve it. "))
                    .setText(new Notification_Text().setText(message))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationInvoiceReminder:", e);
        }
    }

    @JsonIgnore
    public void notificationInvoiceOverdue(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.high)
                    .setLevel(Enum_Notification_level.warning)
                    .setText(new Notification_Text().setText("Invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" for this product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText(" is overdue."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationInvoiceReminder:", e);
        }
    }

    @JsonIgnore
    public void notificationPaymentIncomplete(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.high)
                    .setLevel(Enum_Notification_level.warning)
                    .setText(new Notification_Text().setText("It seems, that you did not finish your payment for this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationPaymentIncomplete:", e);
        }
    }

    @JsonIgnore
    public void notificationPaymentSuccess(double amount){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Payment $" + amount + " for invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successful."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationPaymentSuccess:", e);
        }
    }

    @JsonIgnore
    public void notificationPaymentFail(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.high)
                    .setLevel(Enum_Notification_level.error)
                    .setText(new Notification_Text().setText("Failed to receive your payment for this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" Check the payment or contact support."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationPaymentFail:", e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission() {  return ((getProduct().customer.company && product.customer.isEmployee(Controller_Security.get_person())) || (!getProduct().customer.company && product.customer.getPerson().id.equals(Controller_Security.get_person_id()))) || Controller_Security.get_person().permissions_keys.containsKey("Invoice_create");}
    @JsonIgnore @Transient public boolean read_permission()   {  return ((getProduct().customer.company && product.customer.isEmployee(Controller_Security.get_person())) || (!getProduct().customer.company && product.customer.getPerson().id.equals(Controller_Security.get_person_id()))) || Controller_Security.get_person().permissions_keys.containsKey("Invoice_read");}
    @JsonIgnore @Transient public boolean remind_permission() {  return true;  }
    @JsonIgnore @Transient public boolean edit_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean delete_permission() {  return true;  }

    public enum permissions{Invoice_create, Invoice_update, Invoice_read, Invoice_edit, Invoice_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Invoice get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Invoice> find = new Finder<>(Model_Invoice.class);

}
