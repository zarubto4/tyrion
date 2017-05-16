package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.libs.Json;
import utilities.Server;
import utilities.enums.*;
import utilities.financial.FinancialPermission;
import utilities.financial.history.History;
import utilities.financial.history.HistoryEvent;
import utilities.financial.goPay.GoPay_Controller;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "Product", description = "Model of Product")
public class Model_Product extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Product.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public String id;
                                             @ApiModelProperty(required = true) public String name;

                                 @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Tariff tariff;
                                       @JsonIgnore @Enumerated(EnumType.STRING) public Enum_Payment_mode mode;
                                       @JsonIgnore @Enumerated(EnumType.STRING) public Enum_Payment_method method;

                                       @JsonIgnore @Enumerated(EnumType.STRING) public Enum_BusinessModel business_model;

                                             @ApiModelProperty(required = true) public String subscription_id;
                                                                    @JsonIgnore public String fakturoid_subject_id; // ID účtu ve fakturoidu
                                                                    @JsonIgnore public Long gopay_id;

                                             @ApiModelProperty(required = true) public boolean active;              // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                                    @JsonIgnore public Integer monthly_day_period;  // Den v měsíci, kdy bude obnovována platba // Nejvyšší možné číslo je 28!!!
                                                                    @JsonIgnore public Integer monthly_year_period; // Měsíc v roce, kdy bude obnovována platba // Nejvyšší možné číslo je 12!!!

                                             @ApiModelProperty(required = true) public Date created;
                                                                    @JsonIgnore public boolean on_demand;    // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                                                    @JsonIgnore public Long credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure

                                 @Column(columnDefinition = "TEXT") @JsonIgnore public String financial_history;


    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Project> projects = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Invoice> invoices = new ArrayList<>();

                                          @OneToOne(mappedBy="product", cascade = CascadeType.ALL)  public Model_PaymentDetails payment_details;

                                          @OneToMany(mappedBy="product", cascade = CascadeType.ALL) public List<Model_ProductExtension> extensions = new ArrayList<>();


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true)
    @JsonProperty public List<Model_Invoice> invoices(){

        if(this.invoices == null || this.invoices.isEmpty()) this.invoices =  Model_Invoice.find.where().eq("product.id", this.id).order().desc("created").findList();
        return invoices;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @ApiModelProperty(required = false)
    public Double remaining_credit(){

        if (this.mode == Enum_Payment_mode.per_credit) return ((double) this.credit) / 1000;
        return null;
    }


    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String product_type(){
        if (tariff == null) return Model_Tariff.find.where().eq("product.id", id).select("name").findUnique().name;

        return tariff.name;
    }


    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_mode(){
        switch (mode) {
            case free       : return "free";
            case monthly    : return "monthly";
            case annual     : return "annual";
            case per_credit : return "per_credit";
            default         : return "Undefined state";
        }
    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){
        try{
            switch (method) {
                case bank_transfer  : return  "bank_transfer";
                case credit_card    : return  "credit_card";
                case free           : return  "free";
                default             : return  "Undefined state";
            }
        }catch (NullPointerException e) {
            return "Not set yet";
        }
    }


    @JsonIgnore
    public Long price(){
        Long total = (long) 0;
        for(Model_ProductExtension extension : this.extensions){
            Long price = extension.getPrice();

            if(price != null)
                total += price;
        }
        return total;
    }

    @JsonIgnore
    public Model_Invoice pending_invoice(){

        return Model_Invoice.find.where().eq("product.id", this.id).eq("status", Enum_Payment_status.pending).findUnique();
    }

    @JsonIgnore
    public Double double_credit(){
        return ((double) this.credit) / 1000;
    }

    @JsonIgnore
    public void credit_upload(Long credit){
        try {

            Long credit_before = this.credit;

            try {

                terminal_logger.debug("credit_upload: {} credit", credit);

                this.credit += credit;

                if (!this.active && this.credit > 0) this.active = true; // TODO notifikace - různé stavy

                this.update();

            } catch (Exception e) {

                terminal_logger.internalServerError("credit_upload:", e);

            } finally {

                this.refresh();

                if (this.credit - credit_before == credit) {

                    this.archiveEvent("Credit Upload", ((double) credit) / 1000 + " of credit was successfully added to this product", null);
                    this.notificationCreditSuccess(credit);

                } else {

                    this.archiveEvent("Credit Upload", "Fail to add " + ((double) credit) / 1000 + " of credit to this product", null);
                    this.notificationCreditFail(credit);
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("credit_upload:", e);
        }
    }

    @JsonIgnore
    public void credit_remove(Long credit){
        try {

            Long credit_before = this.credit;

            try {

                terminal_logger.debug("credit_remove: {} credit", credit);

                this.credit -= credit;

                if (this.active && this.credit < 0) this.active = false; // TODO notifikace - různé stavy

                this.update();

            } catch (Exception e) {

                terminal_logger.internalServerError("credit_remove:", e);

            } finally {

                this.refresh();

                if (credit_before - this.credit == credit) {

                    this.archiveEvent("Credit Remove", ((double) credit) / 1000 + " of credit was removed from this product", null);
                    this.notificationCreditRemove(credit);

                } else {

                    this.archiveEvent("Credit Remove", "Fail to remove " + ((double) credit) / 1000 + " of credit from this product", null);
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("credit_remove:", e);
        }
    }

    @JsonIgnore
    public boolean terminateOnDemand(){
        try {

            GoPay_Controller.terminateOnDemand(this);

            this.gopay_id = null;
            this.on_demand = false;
            this.mode = Enum_Payment_mode.per_credit;
            this.update();

            return true;

        } catch (Exception e) {
            terminal_logger.internalServerError("terminateOnDemand:", e);
            return false;
        }
    }

    @JsonIgnore
    public History getFinancialHistory(){
        try {

            if (this.financial_history == null || this.financial_history.equals("")) return new History();

            Form<History> form = Form.form(History.class).bind(Json.parse(this.financial_history));
            if(form.hasErrors()) throw new Exception("Error parsing product financial history. Errors: " + form.errorsAsJson());
            History help = form.get();

            // Sorting the list
            help.history = help.history.stream().sorted((element1, element2) -> element2.date.compareTo(element1.date)).collect(Collectors.toList());
            return help;

        } catch (Exception e) {
            terminal_logger.internalServerError("getFinancialHistory:", e);
            return null;
        }
    }

    @JsonIgnore
    public void archiveEvent(String event_name, String description, String invoice_id){
        try {

            History history = getFinancialHistory();
            if (history == null) return;

            HistoryEvent event = new HistoryEvent();
            event.event = event_name;
            event.description = description;
            event.invoice_id = invoice_id;
            event.date = new Date().toString();

            history.history.add(event);

            this.financial_history = Json.toJson(history).toString();
            this.update();

        } catch (Exception e) {
            terminal_logger.internalServerError("archiveEvent:", e);
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        created = new Date();

        while(true){ // I need Unique Value
            this.azure_product_link = get_Container().getName() + "/" + UUID.randomUUID().toString();
            if (Model_Product.find.where().eq("azure_product_link", azure_product_link ).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            this.subscription_id =  UUID.randomUUID().toString().substring(0, 12);
            if (Model_Product.find.where().eq("subscription_id", subscription_id ).findUnique() == null) break;
        }

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }

        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.error("delete :: This object is not legitimate to remove. ");
        throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());

    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private void notificationCreditSuccess(Long credit){
        try {

            Double amount = ((double) credit) / 1000;

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Credit was uploaded. ").setBoldText())
                    .setText(new Notification_Text().setText(" " + amount + "of credit was added to your product "))
                    .setObject(this)
                    .send(this.payment_details.person);
        } catch (Exception e) {
            terminal_logger.internalServerError("notificationCreditSuccess:", e);
        }
    }

    @JsonIgnore
    private void notificationCreditFail(Long credit){
        try {

            Double amount = ((double) credit) / 1000;

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.high)
                    .setLevel(Enum_Notification_level.error)
                    .setText(new Notification_Text().setText("Failed to upload credit. ").setBoldText())
                    .setText(new Notification_Text().setText(" Adding" + amount + "of credit to your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was unsuccessful."))
                    .send(this.payment_details.person);
        } catch (Exception e) {
            terminal_logger.internalServerError("notificationCreditFail:", e);
        }
    }

    @JsonIgnore
    private void notificationCreditRemove(Long credit){
        try {

            Double amount = ((double) credit) / 1000;

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.info)
                    .setText(new Notification_Text().setText("Credit was removed. ").setBoldText())
                    .setText(new Notification_Text().setText(" " + amount + "of credit was removed from your product "))
                    .setObject(this)
                    .send(this.payment_details.person);
        } catch (Exception e) {
            terminal_logger.internalServerError("notificationCreditRemove:", e);
        }
    }

    @JsonIgnore
    public void notificationTerminateOnDemand(boolean success){
        try {

            Model_Notification notification = new Model_Notification();

            if (success) {
                notification
                        .setImportance(Enum_Notification_importance.normal)
                        .setLevel(Enum_Notification_level.success)
                        .setText(new Notification_Text().setText("On demand payments were canceled on your product"));
            } else {
                notification
                        .setImportance(Enum_Notification_importance.high)
                        .setLevel(Enum_Notification_level.error)
                        .setText(new Notification_Text().setText("Failed to cancel on demand payments on your product"));
            }

            notification
                    .setObject(this)
                    .setText(new Notification_Text().setText("."))
                    .send(this.payment_details.person);

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationCreditRemove:", e);
        }
    }

    @JsonIgnore
    public void notificationRefundPaymentSuccess(double amount){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Refund payment of $" + amount + " for your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successfully refunded."))
                    .send(this.payment_details.person);

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationRefundPaymentSuccess:", e);
        }
    }

    @JsonIgnore
    public void notificationPaymentSuccess(double amount){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Payment $" + amount + " for your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successful."))
                    .send(this.payment_details.person);

        } catch (Exception e) {
            terminal_logger.internalServerError("notificationRefundPaymentSuccess:", e);
        }
    }

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_product_link;

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("product");
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            throw new NullPointerException();
        }

    }

    @JsonIgnore @Transient
    public String get_path(){
        return azure_product_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static final String read_permission_docs   = "read: Bla bla bla";    // TODO
    public static final String create_permission_docs   = "read: Bla bla bla";  // TODO

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  public boolean create_permission()              {  return true;  }
    @JsonIgnore  public boolean read_permission()                {  return payment_details.person.id.equals(Controller_Security.get_person_id()) || Controller_Security.get_person().has_permission("Product_read");  }
                 public boolean edit_permission()                {  return payment_details.person.id.equals(Controller_Security.get_person_id()) || Controller_Security.get_person().has_permission("Product_edit");  }
                 public boolean act_deactivate_permission()      {  return payment_details.person.id.equals(Controller_Security.get_person_id()) || Controller_Security.get_person().has_permission("Product_act_deactivate"); }
    @JsonIgnore  public boolean delete_permission()              {  return Controller_Security.get_person().has_permission("Product_delete");}
    @JsonIgnore  public boolean financial_permission(String action){  return FinancialPermission.check(this, action);}

    public enum permissions{Product_update, Product_read, Product_edit,Product_act_deactivate, Product_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Product get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_Product get_byNameAndOwner(String name, String person_id) {
        return find.where().eq("name", name).eq("payment_details.person.id", person_id).findUnique();
    }

    @JsonIgnore
    public static Model_Product get_byInvoice(String invoice_id) {
        return find.where().eq("invoices.id", invoice_id).findUnique();
    }

    @JsonIgnore
    public static List<Model_Product> get_byOwner(String owner_id) {
        return find.where().eq("payment_details.person.id", owner_id).findList();
    }

    @JsonIgnore
    public static List<Model_Product> get_applicableByOwner(String owner_id) {
        return find.where().eq("active",true).eq("payment_details.person.id", owner_id).select("id").select("name").select("tariff").findList();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Product> find = new Finder<>(Model_Product.class);

}








