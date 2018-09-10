package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.*;
import utilities.enums.Currency;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.slack.Slack;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@ApiModel( value = "Invoice", description = "Model of Invoice")
@Table(name="Invoice")
public class Model_Invoice extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Invoice.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                   @JsonIgnore   public Long   fakturoid_id;         // Id určené ze strany Fakturoid
                                                   @JsonIgnore   public String fakturoid_pdf_url;    // Adresa ke stáhnutí faktury
            @ApiModelProperty(required = true, readOnly = true)  public String invoice_number;       // 2016-0001 - Generuje Fakturoid

                                                   @JsonIgnore   public BigDecimal total_price_without_vat;
                                                   @JsonIgnore   public BigDecimal total_price_with_vat;

                                                   @JsonIgnore   public Long   gopay_id;             // 1213231123
                                                   @JsonIgnore   public String gopay_order_number;   // ON-783837426-1469877748551
                                                   @JsonIgnore   public String gw_url;

                                                   @JsonIgnore   public boolean proforma;

                                                   @JsonIgnore   public Long   proforma_id;          // Id proformy ze které je faktura
                                                   @JsonIgnore   public String proforma_pdf_url;
                                                                 public String public_html_url;

    @ApiModelProperty(required = false, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                           public Date issued;

    @ApiModelProperty(required = false, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                           public Date paid;

    @ApiModelProperty(required = true, readOnly = true,
            dataType = "integer", value = "UNIX time in ms",
            example = "1466163478925")                           public Date overdue;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)               public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="invoice",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_InvoiceItem> invoice_items = new ArrayList<>();

 @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Currency currency;
 @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public PaymentMethod method;

                    @JsonIgnore   @Enumerated(EnumType.STRING)   public InvoiceStatus status;
                    @JsonIgnore   @Enumerated(EnumType.STRING)   public PaymentWarning warning;

/* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/

    /**
     * @return Price without VAT from Fakturoid. If it is not set, return null for customer (such invoice should not be displayed)
     * or an estimate for user with update rights (may be needed for confirmation).
     */
    @JsonProperty
    public Double total_price_without_vat() {
        if (total_price_without_vat != null) {
            return total_price_without_vat.doubleValue();
        }

        // those are only two states, where price should be empty
        if(status != InvoiceStatus.UNCONFIRMED && status != InvoiceStatus.UNFINISHED) {
            return null;
        }

        // Only user with update permission should be able to see invoices which are not
        // synchronized with Facturoid nad without prices set from it - show the estimate.
        try {
            check_update_permission();
            return getTotalPriceWithoutVatEstimate();
        } catch (_Base_Result_Exception e){
            return null;
        }
    }

    /**
     * @return Price without VAT from Fakturoid. If it is not set, return null for customer (such invoice should not be displayed)
     * or an estimate for user with update rights (may be needed for confirmation).
     */
    @JsonProperty
    public Double total_price_with_vat() {
        if (total_price_with_vat != null) {
            return total_price_with_vat.doubleValue();
        }

        // those are only two states, where price should be empty
        if(status != InvoiceStatus.UNCONFIRMED && status != InvoiceStatus.UNFINISHED) {
            return null;
        }

        // Only user with update permission should be able to see invoices which are not
        // synchronized with Facturoid nad without prices set from it - show the estimate.
        try {
            check_update_permission();
            return getTotalPriceWithVatEstimate();
        } catch (_Base_Result_Exception e){
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String invoice_pdf_link()  {
        try{
            return fakturoid_pdf_url != null ?  Server.httpAddress + "/invoice/pdf/invoice/" + id : null;
            } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is available")
    public String proforma_pdf_link() {
        try {
            return proforma_pdf_url != null ?  Server.httpAddress + "/invoice/pdf/proforma/" + id : null;
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public boolean require_payment()  {
        try{
            return status == InvoiceStatus.PENDING || status == InvoiceStatus.OVERDUE;
        } catch (_Base_Result_Exception e){
            //nothing
            return false;
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Visible only when the invoice is not paid")
    public String gw_url()  {
       try{
        if (status == InvoiceStatus.PENDING || status == InvoiceStatus.OVERDUE) return this.gw_url;

        return null;
       } catch (_Base_Result_Exception e){
           //nothing
           return null;
       } catch (Exception e) {
           logger.internalServerError(e);
           return null;
       }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String payment_status() {
        try {
            switch (status) {
                case PAID: return  "Invoice is paid.";
                case PENDING: return  "Invoice needs to be paid.";
                case OVERDUE: return  "Invoice is overdue.";
                case CANCELED: return  "Invoice is canceled.";
                case UNCONFIRMED: return  "Invoice is waiting for conformation by admin.";
                case UNFINISHED: return  "Invoice is waiting for registration in Fakturoid.";
                default             : return  "Undefined state";
            }

        }catch (_Base_Result_Exception e){
            logger.internalServerError(e);
            return null;

        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Model_InvoiceItem> invoice_items() {
        try {
            if (invoice_items != null) return invoice_items;

            return Model_InvoiceItem.find.query().where().eq("invoice.id", this.id).findList();

         }catch (_Base_Result_Exception e){
            logger.internalServerError(e);
            return null;

        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = false, readOnly = true)
    public boolean to_confirm() {
        return status == InvoiceStatus.UNCONFIRMED;
    }

    @JsonProperty
    public Date from() {
        Model_ExtensionFinancialEvent fst =  Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("invoice.id", id)
                .orderBy("event_start ASC")
                .setMaxRows(1)
                .findOne();
        if(fst == null) {
            logger.error("Cannot find oldest financial event for invoice {}.", id);
            return null;
        }

        return fst.event_start;
    }

    @JsonProperty
    public Date to() {
        Model_ExtensionFinancialEvent last =  Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("invoice.id", id)
                .orderBy("event_start DESC")
                .setMaxRows(1)
                .findOne();
        if(last == null) {
            logger.error("Cannot find oldest financial event for invoice {}.", id);
            return null;
        }

        return last.event_end;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Product getProduct() {
        if (product == null) product = Model_Product.getByInvoice(this.id);
        return product;
    }

    /**
     * We takes the calculated price of an invoice from Fakturoid
     * (Just to be sure and avoid some small rounding troubles.) <br /><br />
     *
     * Users should be able to see only invoices which are already in Fakturoid so price is prepared.
     * This is for admins.
     *
     * @return price of the whole invoice without taxes
     */
    @JsonIgnore
    public double  getTotalPriceWithoutVatEstimate() {
        BigDecimal total = BigDecimal.ZERO;
        for (Model_InvoiceItem  item : invoice_items) {
            total = total.add(item.quantity.multiply(item.unit_price));
        }
        return total.setScale(Server.financial_price_scale, Server.financial_price_rounding).doubleValue();
    }

    /**
     * We takes the calculated price of an invoice from Fakturoid
     * (Just to be sure and avoid some small rounding troubles.) <br /><br />
     *
     * Users should be able to see only invoices which are already in Fakturoid so price is prepared.
     * This is for admins.
     *
     * @return price of the whole invoice inc. taxes
     */
    @JsonIgnore
    public double getTotalPriceWithVatEstimate() {
        Map<Integer, BigDecimal> vatRatesPrices = new HashMap();

        for (Model_InvoiceItem  item : invoice_items) {
            BigDecimal total = vatRatesPrices.get(item.vat_rate);
            if(total == null) {
                total = BigDecimal.ZERO;
            }
            total = total.add(item.quantity.multiply(item.unit_price));
            vatRatesPrices.put(item.vat_rate, total);
        }

        BigDecimal totalWithoutVat = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;
        for(Map.Entry<Integer, BigDecimal> ratePriceItem: vatRatesPrices.entrySet()) {
            BigDecimal withoutVat = ratePriceItem.getValue();
            BigDecimal vat = withoutVat
                    .multiply(new BigDecimal(ratePriceItem.getKey()))
                    .divide(new BigDecimal(100));

            totalWithoutVat = totalWithoutVat.add(withoutVat);
            totalVat = totalVat.add(vat);
        }

        totalWithoutVat = totalWithoutVat.setScale(Server.financial_price_scale, Server.financial_price_rounding);
        totalVat = totalVat.setScale(Server.financial_tax_scale, Server.financial_tax_rounding);

        return totalWithoutVat.add(totalVat).doubleValue();
    }

    @JsonIgnore
    public boolean isIssued() {
        return status != InvoiceStatus.UNCONFIRMED && status != InvoiceStatus.UNFINISHED;
    }

/* EVENTS --------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_ProductEvent saveEvent(Date time, ProductEventType eventType) {
        return saveEvent(time, eventType, null);
    }

    @JsonIgnore
    public Model_ProductEvent saveEvent(Date time, ProductEventType eventType, String data) {
        Model_ProductEvent historyEvent = new Model_ProductEvent();
        historyEvent.product = getProduct();
        historyEvent.reference = this.id;
        historyEvent.created = time;
        historyEvent.event_type = eventType;
        historyEvent.detail = data;
        historyEvent.save();

        return historyEvent;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* MESSAGE FOR ADMIN  ---------------------------------------------------------------------------------------------------*/
    public void sendMessageToAdmin(String message) {
        String invoiceURL = Server.becki_mainUrl + "/financial/" + product.id + "/invoices/" + id;
        String fullMessage = message + "\nLink: " + invoiceURL + " .";
        logger.debug(fullMessage);
//        Slack.post(fullMessage); TODO uncomment line for production
    }

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
    public void notificationPaymentSuccess() {
        try {
            double amount = total_price_with_vat();

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

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invoice_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invoice_read.name())) return;
        if(!isIssued()) {
            if(_BaseController.person().has_permission(Permission.Invoice_update.name())) return;
            throw new Result_Error_PermissionDenied();
        }

        getProduct().owner.check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invoice_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invoice_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }


    public enum Permission {Invoice_create, Invoice_update, Invoice_read, Invoice_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Invoice.class)
    public static CacheFinder<Model_Invoice> find = new CacheFinder<>(Model_Invoice.class);
}
