package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.*;
import utilities.financial.extensions.configurations.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Notification_Text;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Invoice", description = "Model of Invoice")
@Table(name="Invoice")
public class Model_Invoice extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Invoice.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                   @JsonIgnore  public Long   fakturoid_id;         // Id určené ze strany Fakturoid
                                                   @JsonIgnore  public String fakturoid_pdf_url;    // Adresa ke stáhnutí faktury
            @ApiModelProperty(required = true, readOnly = true) public String invoice_number;       // 2016-0001 - Generuje Fakturoid

                                                   @JsonIgnore  public Long   gopay_id;             // 1213231123
                                                   @JsonIgnore  public String gopay_order_number;   // ON-783837426-1469877748551
                                                   @JsonIgnore  public String gw_url;

                                                   @JsonIgnore  public boolean proforma;

                                                   @JsonIgnore  public Long   proforma_id;          // Id proformy ze které je faktura
                                                   @JsonIgnore  public String proforma_pdf_url;

    @ApiModelProperty(required = false, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                          public Date paid;

    @ApiModelProperty(required = true, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                          public Date overdue;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)              public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="invoice",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_InvoiceItem> invoice_items = new ArrayList<>();

                    @JsonIgnore   @Enumerated(EnumType.STRING)  public PaymentStatus status;
                    @JsonIgnore   @Enumerated(EnumType.STRING)  public PaymentMethod method;
                    @JsonIgnore   @Enumerated(EnumType.STRING)  public PaymentWarning warning;

/* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String invoice_pdf_link()  { return fakturoid_pdf_url != null ?  Server.httpAddress + "/invoice/pdf/invoice/" + id : null; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String proforma_pdf_link() { return proforma_pdf_url != null ?  Server.httpAddress + "/invoice/pdf/proforma/" + id : null; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  { return status == PaymentStatus.PENDING || status == PaymentStatus.OVERDUE; }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public String gw_url()  {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.OVERDUE) return this.gw_url;

        return null;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String payment_status() {

        switch (status) {
            case PAID: return  "Invoice is paid.";
            case PENDING: return  "Invoice needs to be paid.";
            case OVERDUE: return  "Invoice is overdue.";
            case CANCELED: return  "Invoice is canceled.";
            default             : return  "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String payment_method() {
        switch (method) {
            case BANK_TRANSFER: return  "Bank transfer.";
            case CREDIT_CARD: return  "Credit Card Payment.";
            default            : return  "Undefined state";
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Model_InvoiceItem> invoice_items() {

        if (invoice_items != null) return invoice_items;

        return Model_InvoiceItem.find.query().where().eq("invoice.id", this.id).findList();
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public double price() {
        return ((double)this.total_price());
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Product getProduct() {
        if (product == null) product = Model_Product.getByInvoice(this.id);
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
            item.unit_price = extension.getConfigPrice() * 30;

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
            item.currency = Currency.USD;

            invoice_items.add(item);
        }
    }

    @JsonIgnore
    public Long total_price() {
        Long total_price = 0L;
        for (Model_InvoiceItem  item : invoice_items) {
            total_price += item.unit_price * item.quantity;
        }
        return total_price;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notificationInvoiceNew() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("See new invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" for your product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText("."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationInvoiceReminder(String message) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Payment for this product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText(" was not received. See this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" and resolve it. "))
                    .setText(new Notification_Text().setText(message))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationInvoiceOverdue() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" for this product "))
                    .setObject(this.getProduct())
                    .setText(new Notification_Text().setText(" is overdue."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationPaymentIncomplete() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("It seems, that you did not finish your payment for this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationPaymentSuccess(double amount) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Payment $" + amount + " for invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successful."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationPaymentFail() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.ERROR)
                    .setText(new Notification_Text().setText("Failed to receive your payment for this invoice "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" Check the payment or contact support."))
                    .send(this.getProduct().notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission() {  return product.customer.isEmployee(BaseController.person()) || BaseController.person().has_permission("Invoice_create");}
    @JsonIgnore @Transient public boolean read_permission()   {  return product.customer.isEmployee(BaseController.person()) || BaseController.person().has_permission("Invoice_read");}
    @JsonIgnore @Transient public boolean remind_permission() {  return true;  }
    @JsonIgnore @Transient public boolean edit_permission()   {  return true;  }
    @JsonIgnore @Transient public boolean delete_permission() {  return true;  }

    public enum Permission {Invoice_create, Invoice_update, Invoice_read, Invoice_edit, Invoice_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Invoice get_byId(String id) {
        return get_byId(UUID.fromString(id));
    }

    public static Model_Invoice get_byId(UUID id) {
        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Invoice> find = new Finder<>(Model_Invoice.class);

}
