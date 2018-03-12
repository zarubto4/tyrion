package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.financial.FinancialPermission;
import utilities.financial.history.History;
import utilities.financial.history.HistoryEvent;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "Product", description = "Model of Product")
@Table(name="Product")
public class Model_Product extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Product.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


                                                   @Enumerated(EnumType.STRING) public PaymentMethod method;
                                       @JsonIgnore @Enumerated(EnumType.STRING) public BusinessModel business_model;

                                             @ApiModelProperty(required = true) public String subscription_id;
                                                                    @JsonIgnore public String fakturoid_subject_id; // ID účtu ve fakturoidu
                                                                    @JsonIgnore public Long gopay_id;

                                             @ApiModelProperty(required = true) public boolean active;              // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                                    @JsonIgnore public boolean on_demand;           // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                                                    @JsonIgnore public Long credit;                 // Zbývající kredit pokud je typl platby per_credit - jako na Azure

                                 @Column(columnDefinition = "TEXT") @JsonIgnore public String financial_history;
                                 @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
                                                                    @JsonIgnore public boolean removed_byinvoi_user; // může jenom administrátor

                                                                                public boolean client_billing;                      // Zda bude fakturováno jinému zákazníkovi
                                                                    @JsonIgnore public String  client_billing_invoice_parameters;   // Zde je konfigurace k fakturám, které se zasílají zákazníkovi


                       @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_Customer customer;               // Owner
                       @OneToOne(mappedBy="product", cascade = CascadeType.ALL) public Model_PaymentDetails payment_details;        // Záměrně 1:1 aby bylo editovatelné separátně

    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_Project>          projects    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_Invoice>          invoices    = new ArrayList<>();
                @OneToMany(mappedBy="product", cascade = CascadeType.ALL)                           public List<Model_ProductExtension> extensions  = new ArrayList<>();


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_project_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_invoices_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_extensions_ids;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true) @JsonProperty
    public List<Model_Invoice> invoices() {

        if (this.invoices == null || this.invoices.isEmpty()) this.invoices =  Model_Invoice.find.query().where().eq("product.id", this.id).order().desc("created").findList();
        return invoices;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @ApiModelProperty(required = false)
    public Double remaining_credit() {

        if (this.business_model == BusinessModel.SAAS && this.method != PaymentMethod.FREE)
            return ((double) this.credit);

        return null;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Long price() {

        logger.debug("price: Beginning to count product price");

        Long total = 0L;
        for (Model_ProductExtension extension : this.extensions) {
            Long price = extension.getActualPrice();

            logger.trace("price: Returned value: {}", price);

            if (price != null)
                total += price;
        }

        logger.debug("price: Summarized = {}", total);

        return total;
    }

    @JsonIgnore
    public Model_Invoice pending_invoice() {

        return Model_Invoice.find.query().where().eq("product.id", this.id).eq("status", PaymentStatus.PENDING).findOne();
    }

    @JsonIgnore
    public Double double_credit() {
        return ((double) this.credit);
    }

    @JsonIgnore
    public void credit_upload(Long credit) {
        try {

            Long credit_before = this.credit;

            try {

                logger.debug("credit_upload: {} credit", credit);

                this.credit += credit;

                if (!this.active && this.credit > 0) {

                    this.active = true;
                    this.notificationActivation();

                } else if (!this.active && this.credit < 0) {

                    this.notificationCreditInsufficient();
                }

                this.update();

            } catch (Exception e) {

                logger.internalServerError(e);

            } finally {

                this.refresh();

                if (this.credit - credit_before == credit) {

                    this.archiveEvent("Credit Upload", ((double) credit) + " of credit was successfully added to this product", null);
                    this.notificationCreditSuccess(credit);

                } else {

                    this.archiveEvent("Credit Upload", "Fail to add " + ((double) credit)  + " of credit to this product", null);
                    this.notificationCreditFail(credit);
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void credit_remove(Long credit) {
        try {

            Long credit_before = this.credit;

            try {

                logger.debug("credit_remove: {} credit", credit);

                this.credit -= credit;

                if (this.active && this.credit < 0) {

                    this.active = false;
                    this.notificationDeactivation();
                }

                this.update();

            } catch (Exception e) {

                logger.internalServerError(e);

            } finally {

                this.refresh();

                if (credit_before - this.credit == credit) {

                    this.archiveEvent("Credit Remove", ((double) credit) + " of credit was removed from this product", null);
                    this.notificationCreditRemove(credit);

                } else {

                    this.archiveEvent("Credit Remove", "Fail to remove " + ((double) credit) + " of credit from this product", null);
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public History getFinancialHistory() {
        try {

            if (this.financial_history == null || this.financial_history.equals("")) return new History();

            History help = baseFormFactory.formFromJsonWithValidation(History.class, Json.parse(this.financial_history));

            // Sorting the list
            help.history = help.history.stream().sorted((element1, element2) -> element2.date.compareTo(element1.date)).collect(Collectors.toList());
            return help;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public void archiveEvent(String event_name, String description, UUID invoice_id) {
        try {

            History history = getFinancialHistory();
            if (history == null) return;

            HistoryEvent event = new HistoryEvent();
            event.event = event_name;
            event.description = description;
            event.invoice_id = invoice_id;
            event.date = new Date().toString();

            while (history.history.size() >= 100) {

                history.history.remove(history.history.size() - 1);
            }

            history.history.add(event);

            logger.debug("archiveEvent: {}", Json.toJson(event));

            this.financial_history = Json.toJson(history).toString();
            this.update();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public Double getLastSpending() {
        try {

            return ((double) getFinancialHistory().last_spending) ;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Double getAverageSpending() {
        try {

            return ((double) getFinancialHistory().average_spending) ;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Long getRemainingDays() {
        try {

            History history = getFinancialHistory();

            if (history.average_spending == 0) return null;

            return credit / history.average_spending;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public void credit_spend(Long credit) {

        this.credit -= credit;

        try {

            History history = getFinancialHistory();
            history.last_spending = credit;

            history.average_spending = (history.average_spending * history.mean_coefficient + credit) / history.mean_coefficient++;

            if (history.mean_coefficient > 30) history.mean_coefficient = 30L;

            this.financial_history = Json.toJson(history).toString();


        } catch (Exception e) {
            logger.internalServerError(e);
        }

        this.update();
    }

    @JsonIgnore
    public JsonNode setConfiguration() { // TODO

        /*Form<?> form;

        switch (business_model) {

            case ALPHA:{
                form = Form.form(Configuration_Alpha.class).bindFromRequest();
                break;
            }

            case SAAS:{
                form = Form.form(Configuration_Saas.class).bindFromRequest();
                break;
            }

            case FEE:{
                form = Form.form(Configuration_Fee.class).bindFromRequest();
                break;
            }

            case CAL:{
                form = Form.form(Configuration_Cal.class).bindFromRequest();
                break;
            }

            case INTEGRATOR:{
                form = Form.form(Configuration_Integrator.class).bindFromRequest();
                break;
            }

            case INTEGRATION:{
                form = Form.form(Configuration_Integration.class).bindFromRequest();
                break;
            }

            default: form = Form.form(Configuration_Saas.class).bindFromRequest(); break;
        }

        if (form.hasErrors()) return form.errorsAsJson();

        this.configuration = Json.toJson(form.get()).toString();*/
        return null;
    }

    @JsonIgnore
    public Object getConfiguration() {
        try {

            Form<?> form;

            /*switch (business_model) { TODO

                case ALPHA:{
                    form = Form.form(Configuration_Alpha.class).bind(Json.parse(configuration));
                    break;
                }

                case SAAS:{
                    form = Form.form(Configuration_Saas.class).bind(Json.parse(configuration));
                    break;
                }

                case FEE:{
                    form = Form.form(Configuration_Fee.class).bind(Json.parse(configuration));
                    break;
                }

                case CAL:{
                    form = Form.form(Configuration_Cal.class).bind(Json.parse(configuration));
                    break;
                }

                case INTEGRATOR:{
                    form = Form.form(Configuration_Integrator.class).bind(Json.parse(configuration));
                    break;
                }

                case INTEGRATION:{
                    form = Form.form(Configuration_Integration.class).bind(Json.parse(configuration));
                    break;
                }

                default: throw new Exception("Business model is unknown.");
            }

            if (form.hasErrors()) throw new Exception("Error parsing product configuration. Errors: " + form.errorsAsJson());
            return form.get();*/

            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public List<Model_Project> get_projects() throws _Base_Result_Exception {

        List<Model_Project>  projects = new ArrayList<>();

        for (UUID id : get_projects_ids()) {
            projects.add(Model_Project.getById(id));
        }

        return projects;
    }

    @JsonIgnore
    public List<UUID> get_projects_ids() {
        if (cache_project_ids == null) {
            cache_project_ids = Model_Project.find.query().where().eq("product.id", this.id).findIds();
        }

        return cache_project_ids;
    }

    @JsonIgnore
    public List<Model_Person> notificationReceivers() {

        List<Model_Person> receivers = new ArrayList<>();
        try {

            this.customer.getEmployees().forEach(employee -> receivers.add(employee.get_person()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return receivers;
    }

    @JsonIgnore
    public boolean isBillingReady() {

        if (payment_details == null) {

            notificationPaymentNeeded("Fill in payment details, otherwise your product will be deactivated soon.");

            return false;

        } else {

            if (fakturoid_subject_id == null) {

                notificationPaymentNeeded("Payment details are probably invalid. Check provided info or contact support.");

                return false;
            }
        }

        return true;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        while(true) { // I need Unique Value
            this.azure_product_link = get_Container().getName() + "/" + UUID.randomUUID().toString();
            if (Model_Product.find.query().where().eq("azure_product_link", azure_product_link ).findOne() == null) break;
        }

        while(true) { // I need Unique Value
            this.subscription_id =  UUID.randomUUID().toString().substring(0, 12);
            if (Model_Product.find.query().where().eq("subscription_id", subscription_id ).findOne() == null) break;
        }

        //Save Object
        super.save();
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notificationActivation() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was activated."))
                    .send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationDeactivation(String... args) {
        try {

            Model_Notification notification = new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was deactivated."));

            for (String message : args) {
                notification.setText(new Notification_Text().setText(message));
            }

            notification.send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationPaymentNeeded(String message) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Payment is needed for your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(". " + message))
                    .send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private void notificationCreditSuccess(Long credit) {
        try {

            Double amount = ((double) credit) ;

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Credit was uploaded. ").setBoldText())
                    .setText(new Notification_Text().setText(" " + amount + " of credit was added to your product "))
                    .setObject(this)
                    .send(notificationReceivers());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private void notificationCreditFail(Long credit) {
        try {

            Double amount = ((double) credit) ;

            new Model_Notification()
                    .setImportance(NotificationImportance.HIGH)
                    .setLevel(NotificationLevel.ERROR)
                    .setText(new Notification_Text().setText("Failed to upload credit. ").setBoldText())
                    .setText(new Notification_Text().setText(" Adding" + amount + " of credit to your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was unsuccessful."))
                    .send(notificationReceivers());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private void notificationCreditRemove(Long credit) {
        try {

            Double amount = ((double) credit);

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("Credit was removed. ").setBoldText())
                    .setText(new Notification_Text().setText(" " + amount + " of credit was removed from your product "))
                    .setObject(this)
                    .send(notificationReceivers());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationCreditInsufficient() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Amount of credit is insufficient. Your credit balance is " + this.credit + ". Credit must be positive, so your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" could be activated."))
                    .send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationTerminateOnDemand(boolean success) {
        try {

            Model_Notification notification = new Model_Notification();

            if (success) {
                notification
                        .setImportance(NotificationImportance.NORMAL)
                        .setLevel(NotificationLevel.SUCCESS)
                        .setText(new Notification_Text().setText("On demand payments were canceled on your product "));
            } else {
                notification
                        .setImportance(NotificationImportance.HIGH)
                        .setLevel(NotificationLevel.ERROR)
                        .setText(new Notification_Text().setText("Failed to cancel on demand payments on your product "));
            }

            notification
                    .setObject(this)
                    .setText(new Notification_Text().setText("."))
                    .send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationRefundPaymentSuccess(double amount) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Refund payment of $" + amount + " for your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successfully refunded."))
                    .send(notificationReceivers());

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
                    .setText(new Notification_Text().setText("Payment $" + amount + " for your product "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" was successful."))
                    .send(notificationReceivers());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_product_link;

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container() {
        try {
            return Server.blobClient.getContainerReference("product");
        } catch (Exception e) {
            logger.internalServerError(e);
            throw new NullPointerException();
        }
    }

    @JsonIgnore @Transient
    public String get_path() {
        return azure_product_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user is customer who owns the product or is employee of a customer(company) which owns it, he can read the product";
    @JsonIgnore @Transient public static final String create_permission_docs   = "create: Everyone can create personal product and every employee of a customer(company) can create product for the company.";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        // Not Limited now, Maybe max per user?
        return;
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Product_read.name())) return;
        if(customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Product_update.name())) return;
        if(customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Product_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient public void check_act_deactivate_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Product_act_deactivate.name())) return;
        if(customer.isEmployee(_BaseController.person())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient public void check_financial_permission(String action)  throws _Base_Result_Exception {
        FinancialPermission.check_permission(this, action);
    }

    public enum Permission {Product_crete, Product_update, Product_read, Product_act_deactivate, Product_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Product.class)
    public static Cache<UUID, Model_Product> cache;

    public static Model_Product getById(UUID id) throws _Base_Result_Exception  {

        Model_Product product = cache.get(id);

        if (product == null) {

            product = Model_Product.find.byId(id);
            if (product == null) throw new Result_Error_NotFound(Model_Product.class);

            cache.put(id, product);
        }

        // Check Permission
        product.check_read_permission();
        return product;
    }

    public static Model_Product getByInvoice(UUID invoice_id) throws _Base_Result_Exception  {
        return find.query().where().eq("invoices.id", invoice_id).findOne();
    }

    public static List<Model_Product> getByOwner(UUID owner_id) throws _Base_Result_Exception  {
        return find.query().where().disjunction().eq("customer.employees.person.id", owner_id).findList();
    }

    public static List<Model_Product> getApplicableByOwner(UUID owner_id) throws _Base_Result_Exception {
        return find.query().where().eq("active",true).eq("customer.employees.person.id", owner_id).select("id").select("name").findList();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Product> find = new Finder<>(Model_Product.class);
}