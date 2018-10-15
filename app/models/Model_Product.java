package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import exceptions.NotFoundException;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.db.ebean.Transactional;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.emails.Email;
import utilities.enums.*;
import utilities.enums.Currency;
import exceptions.BadRequestException;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.model.UnderCustomer;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.slack.Slack;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "Product", description = "Model of Product")
@Table(name="Product")
public class Model_Product extends NamedModel implements Permissible, UnderCustomer {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Product.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


                                                                    @JsonIgnore public BusinessModel business_model;

                                             @ApiModelProperty(required = true) public String subscription_id;


                                              /**
                                               * Should never be set directly!! Use setActive() method after extension is created.
                                               */
                                             @ApiModelProperty(required = true) public boolean active;               // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                                    @JsonIgnore public BigDecimal credit;            // Zbývající kredit, který klient dostane do začátku

                                 @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
                                                                    @JsonIgnore public boolean removed_byinvoi_user; // může jenom administrátor


                 @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_Customer owner;
                  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_IntegratorClient integrator_client; // null pokud zákazník (owner) dělá projekt pro sebe

                  // informace o placení nezávislé na tom, jestli je zákazník integrátor nebo ne
   @ApiModelProperty(required = true) @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_PaymentDetails payment_details;

    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_Project>          projects    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_Invoice>          invoices    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<Model_ProductExtension> extensions  = new ArrayList<>();


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @ApiModelProperty(required = false)
    public double remaining_credit() {
        return credit
                .setScale(Server.financial_price_scale, Server.financial_price_rounding)
                .doubleValue();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transactional
    public void setActive(boolean activeNew) throws Exception {
        if(this.active == activeNew) {
            throw new BadRequestException("Extension is already " + (activeNew ? "activated" : "deactivated"));
        }

        if(activeNew) {
            this.active = true;
            update();
            return;
        }

        List<Model_ProductExtension> activeExtensions =  getExtensions().stream()
                .filter(extension -> extension.active)
                .collect(Collectors.toList());

        boolean allDeactivated = true;
        for(Model_ProductExtension extension : activeExtensions) {
            try {
                extension.setActive(false);
            } catch (Exception e) {
                logger.error("Error while deactivating extension during deactivating product.", e);
                allDeactivated = false;
            }
        }

        if(allDeactivated) {
            throw new Exception("Extension(s) of the product cannot be deactivated. Product still stays active!");
        }

        this.active = false;
        update();
    }

    @JsonIgnore
    public int getExtensionCount() {
        return Model_ProductExtension.find.query().where().eq("product.id", id).findCount();
    }

    @JsonIgnore
    public List<Model_ProductExtension> getExtensions() {
        try {

            List<Model_ProductExtension> list = new ArrayList<>();

            for (UUID id : getExtensionIds() ) {
                list.add(Model_ProductExtension.find.byId(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getExtensionIds() {
        return Model_ProductExtension.find
                .query()
                .where().eq("product.id", id)
                .ne("deleted", true)
                .orderBy("UPPER(name) ASC")
                .select("id")
                .findSingleAttributeList();
    }

    /**
     * Find all product extensions that were active during given period.
     *
     * @param from Start if the search period. (Included.)
     * @param to End of the search period. (Excluded.)
     * @return All products extension active at least for a part of given period.
     */
    @JsonIgnore
    public Collection<Model_ProductExtension> getActiveExtensions(Date from, Date to) {
        // get all extensions created before 'to' and not deleted before 'from'
        Set<Model_ProductExtension> extensions = Model_ProductExtension.find.query()
                .setIncludeSoftDeletes()
                .where()
                .eq("product.id", id)
                .lt("created", to)
                .or(Expr.eq("deleted", false), Expr.gt("removed", from))
                .findSet();

        return extensions.stream().filter(e -> e.wasActive(from, to)).collect(Collectors.toSet());
    }

    /**
     * Creates new financial event(s) for all extensions of given product. Event(s) contain(s) particularly
     * resource consumption and price. <br>
     * Events are created for every day from the last saved event or first activation of the extension if there is no event.
     * The unfinished day can be saved or not according to the parameter.
     *
     * @param saveToday False if we want to save only event(s) till last midnight, true if we want to save today as well.
     * @return All newly created financial events in ascending order according to event period.
     */
    @JsonIgnore
    public List<Model_ExtensionFinancialEvent> updateHistory(boolean saveToday) {
        SortedSet<Model_ExtensionFinancialEvent> result = new TreeSet<>((o1, o2) -> o1.event_start.compareTo(o2.event_start));

        for(Model_ProductExtension productExtension : extensions) {
            List<Model_ExtensionFinancialEvent> extensionsEvents = productExtension.updateHistory(saveToday);
            result.addAll(extensionsEvents);
        }

        return new ArrayList<>(result);
    }

    /**
     * Get all history events.
     *
     * @param page index of a page, starting from 0
     * @param rows maximal number of rows on one page
     * @param ascending
     * @return All history events for the product.
     */
    @JsonIgnore
    public PagedList<Model_ProductEvent> getProductEvents(int page, int rows, boolean ascending, ProductEventTypeReadPermission read_permission) {
        ExpressionList<Model_ProductEvent> query = Model_ProductEvent.find.query()
                .where()
                .eq("product.id", id);

        if(read_permission == ProductEventTypeReadPermission.USER) {
            query.eq("read_permission", ProductEventTypeReadPermission.USER);
        }

        return (ascending ? query.orderBy("created ASC, event_type ASC") : query.orderBy("created DESC, event_type DESC"))
                .setFirstRow((page) * rows)
                .setMaxRows(rows)
                .findPagedList();
    }

    /**
     * Get all unpaid financial events for all extension of given product, ordered by event start.
     *
     * @param ascending
     * @return Unpaid financial events in descending order according to event start.
     */
    @JsonIgnore
    public List<Model_ExtensionFinancialEvent> getFinancialEventsNotInvoiced(boolean ascending) {
        List<Model_ExtensionFinancialEvent> unpaid = Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("product_extension.product.id", id)
                .isNull("invoice")
                .orderBy("event_start " + (ascending ? "ASC" : "DESC"))
                .findList();
        return unpaid;
    }

    @JsonIgnore
    public Model_ExtensionFinancialEvent getFinancialEventFirstNotInvoiced() {
        Model_ExtensionFinancialEvent unpaid = Model_ExtensionFinancialEvent.find.query()
                .where()
                .eq("product_extension.product.id", id)
                .isNull("invoice")
                .order().asc("event_start")
                .setMaxRows(1)
                .findOne();
        return unpaid;
    }

    @JsonIgnore
    @Transactional
    public Model_Invoice createInvoice(Date from, Date to) throws Exception {
        if(owner.contact == null) {
            throw new Exception("Cannot create invoice when owner contact information are not set!");
        }

        if(!owner.contact.isValid()) {
            throw new Exception("Cannot create invoice when owner contact is invalid!");
        }

        if(!payment_details.isValid()) {
            throw new Exception("Cannot create invoice when payment details are not valid!");
        }

        Model_Invoice invoice = new Model_Invoice();
        invoice.product = this;
        invoice.created = new Date();
        invoice.currency = Currency.USD;
        invoice.method = payment_details.payment_method;
        invoice.save();

        List<ExtensionInvoiceItem> extensionInvoiceItems = createInvoiceItems(from, to, invoice);
        for (ExtensionInvoiceItem item : extensionInvoiceItems) {
            Model_InvoiceItem invoiceItem = new Model_InvoiceItem();
            invoiceItem.invoice = invoice;
            invoiceItem.name = item.getName();
            invoiceItem.quantity = item.getQuantity().setScale(Server.financial_quantity_scale, Server.financial_quantity_rounding);
            invoiceItem.unit_name = item.getUnitName();
            invoiceItem.unit_price = item.getUnitPrice().setScale(Server.financial_price_scale, Server.financial_price_rounding);
            invoiceItem.save();

            invoice.invoice_items().add(invoiceItem);
        }

        BigDecimal unpaid = extensionInvoiceItems.stream()
                .map(i -> i.getPriceTotal())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        if (credit.signum() > 0) {
            BigDecimal paidFromCredit =  unpaid.min(credit);
            unpaid = unpaid.subtract(paidFromCredit);

            Model_InvoiceItem invoiceItemDiscount = new Model_InvoiceItem();
            invoiceItemDiscount.invoice = invoice;
            invoiceItemDiscount.name = "Free credit";
            invoiceItemDiscount.quantity = BigDecimal.ONE;
            invoiceItemDiscount.unit_name = "";
            invoiceItemDiscount.unit_price = paidFromCredit.negate().setScale(Server.financial_price_scale, Server.financial_price_rounding);
            invoiceItemDiscount.save();

            credit = credit.subtract(paidFromCredit);
        }

        invoice.status = unpaid.compareTo(payment_details.getMonthlyLimit()) > 0 ?  InvoiceStatus.UNCONFIRMED : InvoiceStatus.UNFINISHED;
        invoice.update();

        invoice.saveEvent(invoice.created, ProductEventType.INVOICE_CREATED, "{status: " + invoice.status + "}");

        if(invoice.status == InvoiceStatus.UNCONFIRMED) {
            invoice.sendMessageToAdmin("New invoice created with spending greater than limit! Please check and confirm.");
        }

        return invoice;
    }

    /**
     * Calculate invoice items for given invoice (which has already product assign to it). <br />
     *
     * If price for any extension is calculated as 0, it is left out from invoice!!
     * Financial events are however assign to this invoice. (To marked them as processed.)
     *
     * @param from
     * @param to
     * @param invoice
     * @return
     * @throws Exception
     */
    @JsonIgnore
    public List<ExtensionInvoiceItem> createInvoiceItems(Date from, Date to, Model_Invoice invoice) throws Exception {
        List<ExtensionInvoiceItem> items = new ArrayList<>();

        Collection<Model_ProductExtension> activeExtensions = getActiveExtensions(from, to);
        for (Model_ProductExtension extension : activeExtensions) {
            List<Model_ExtensionFinancialEvent> extensionEvents = extension.getFinancialEventsNotInvoiced(true)
                    .stream().filter(e -> e.event_end.before(to)).collect(Collectors.toList());
            if(extensionEvents.isEmpty()) {
                continue;
            }

            List<ResourceConsumption> consumptions = extensionEvents.stream()
                    .map(event -> extension.getResourceConsumption(event.consumption))
                    .collect(Collectors.toList());

            // calculate invoice items, leave all with price == 0
            List<ExtensionInvoiceItem> invoiceItems = extension.createExtension()
                    .getInvoiceItems(extension.getConfiguration(), consumptions)
                    .stream()
                    .filter(item -> item.getPriceTotal().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            items.addAll(invoiceItems);

            if(invoice != null) {
                for (Model_ExtensionFinancialEvent event : extensionEvents) {
                    event.invoice = invoice;
                    event.save();
                }
            }
        }

        return items;
    }

    @JsonIgnore
    public List<Model_Invoice> getInvoices(boolean notReadyInvoices) {
        ExpressionList<Model_Invoice> query = Model_Invoice.find.query()
                .where()
                .eq("product.id", this.id);

        if (!notReadyInvoices) {
            query = query.ne("status", InvoiceStatus.UNCONFIRMED)
                    .ne("status", InvoiceStatus.UNFINISHED);
        }

        return query.orderBy("created DESC").findList();
    }

    @JsonIgnore
    public Collection<Model_Invoice> getInvoices(InvoiceStatus status) {
        return Model_Invoice.find.query()
                .where()
                .eq("product.id", id)
                .eq("status", status)
                .findSet();
    }

    @JsonIgnore
    public Collection<Model_Invoice> getInvoicesToBePaid() {
        return Model_Invoice.find.query()
                .where()
                .eq("product.id", id)
                .or(Expr.eq("status", InvoiceStatus.PENDING),
                    Expr.eq("status", InvoiceStatus.OVERDUE))
                .findSet();
    }

    @JsonIgnore
    public List<Model_Project> get_projects() {
        List<Model_Project>  projects = new ArrayList<>();

        for (UUID id : get_projects_ids()) {
            projects.add(Model_Project.find.byId(id));
        }

        return projects;
    }

    @JsonIgnore
    public List<UUID> get_projects_ids() {
        if (idCache().gets(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("product.id", id).select("id").findSingleAttribute());
        }

        return idCache().gets(Model_Project.class) != null ?  idCache().gets(Model_Project.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Person> notificationReceivers() {
        List<Model_Person> receivers = new ArrayList<>();
        try {

            this.owner.getEmployees().forEach(employee -> receivers.add(employee.getPerson()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return receivers;
    }

    @JsonIgnore
    public boolean isBillingReady() {
        return owner.contact != null && owner.contact.fakturoid_subject_id != null && payment_details != null;
    }

    @JsonIgnore
    public boolean isRelated(Model_Person person) {
        return this.getCustomer().isEmployee(person);
    }

    @JsonIgnore
    public Model_Customer getCustomer() {
        return isLoaded("customer") ? owner : Model_Customer.find.query().where().eq("products.id", id).findOne();
    }

/* EVENTS --------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_ProductEvent saveEvent(Date time, ProductEventType eventType, String data) {
        Model_ProductEvent historyEvent = new Model_ProductEvent();
        historyEvent.product = this;
        historyEvent.reference = null;
        historyEvent.created = time;
        historyEvent.event_type = eventType;
        historyEvent.detail = data;
        historyEvent.save();

        return historyEvent;
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        this.subscription_id =  UUID.randomUUID().toString();
        super.save();
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();
    }

    @JsonIgnore @Override public boolean delete() {
        boolean allExtensionDeactivated = true;

        List<Model_ProductExtension> activeExtensions = extensions.stream().filter(e -> e.active).collect(Collectors.toList());
        for(Model_ProductExtension extension: activeExtensions) {
            try {
                extension.setActive(false);
            }
            catch (Exception e) {
                allExtensionDeactivated = false;
                logger.error("Extension cannot be deactivated!", e);
            }
        }

        if(allExtensionDeactivated) {
            logger.error("Cannot delete product. Some of its extensions cannot be deactivated.");
            return false;
        }

        try {
            if (!super.delete()) {
                logger.error("Product cannot be deleted.");
                return false;
            }
        }
        catch (Exception e) {
            logger.error("Product cannot be deleted.", e);
            return false;
        }

        saveEvent(removed, ProductEventType.PRODUCT_DELETED, null);
        return true;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* MESSAGE FOR ADMIN  ---------------------------------------------------------------------------------------------------*/
    public void sendMessageToAdmin(String message) {
        String productURL = Server.becki_mainUrl + "/financial/" + id;
        String fullMessage = message + "\nLink: " + productURL + " .";
        logger.debug(fullMessage);
        Slack.post(fullMessage);
    }

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
    public void notificationLowCredit(BigDecimal notInvoicedCredit) {
        try {
            BigDecimal remainingCredit = credit.subtract(notInvoicedCredit)
                    .setScale(Server.financial_price_scale, Server.financial_price_rounding);

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("Remaining credit: " + remainingCredit.toPlainString() + "."))
                    .setObject(this)
                    .send(notificationReceivers());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notificationPaymentDetails() {
        try {
            String text = null;
            if (owner.contact == null) {
                text = "Fill in payment details, otherwise your product will be deactivated soon.";

            } else if (owner.contact.fakturoid_subject_id == null) {
                text = "Payment details are probably invalid. Check provided info or contact support.";
            }

            if (text != null) {
                new Model_Notification()
                        .setImportance(NotificationImportance.HIGH)
                        .setLevel(NotificationLevel.WARNING)
                        .setText(new Notification_Text().setText(text).setBoldText())
                        .setObject(this)
                        .send(notificationReceivers());
            }
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

/* EMAILS --------------------------------------------------------------------------------------------------------------*/
    @JsonIgnore
    public void emailDeactivation() {
        Email email = new Email();
        email.text("Dear customer,");

        if(owner.contact == null) {
            email.text("We are sorry to inform you, that you have run of ouf credit and no payment method is set.")
                 .text("We had to deactivate your product. If you want to continue using your product, " +
                       "please set the payment details and activate the product again.");

        }
        else if(invoices.stream().filter(invoice -> invoice.status == InvoiceStatus.OVERDUE).count() > 0) {
            email.text("We are sorry to inform you, but you have an overdue invoice(s).")
                 .text("We had to deactivate your product. If you want to continue using your product, " +
                       "pay all the invoices and activate the product again.");
        }
        else {
            email.text("We are sorry to inform you, but you product was deactivated.")
                 .text("If you did not perform this action, please contact us for further information.");
        }

        email.text("Best regards, Byzance Team")
             .send(owner, "Product deactivation" );
    }

    @JsonIgnore
    public void emailLowCredit(BigDecimal notInvoicedCredit) {
        BigDecimal remainingCredit = credit.subtract(notInvoicedCredit)
                .setScale(Server.financial_price_scale, Server.financial_price_rounding);

        new Email()
                .text("Dear customer,")
                .text("You have only " + remainingCredit.toPlainString() + " of your credit left.")
                .text("Please set the payment details otherwise you product will be deactivated after you run out of the credit.")
                .text("Best regards, Byzance Team")
                .send(owner, "Missing payment method");
    }



/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/


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
       return get_Container().getName() + "/" + UUID.randomUUID().toString();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user is customer who owns the product or is employee of a customer(company) which owns it, he can read the product";
    @JsonIgnore @Transient public static final String create_permission_docs   = "create: Everyone can create personal product and every employee of a customer(company) can create product for the company.";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.PRODUCT;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Product getByInvoice(UUID invoice_id) throws NotFoundException {
        return find.query().where().eq("invoices.id", invoice_id).findOne();
    }

    public static List<Model_Product> getByOwner(UUID owner_id) {
        return find.query().where().disjunction().eq("owner.employees.person.id", owner_id).findList();
    }

    public static List<Model_Product> getApplicableByOwner(UUID owner_id) {
        return find.query().where().eq("active",true).eq("owner.employees.person.id", owner_id).select("id").select("name").findList();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Product.class)
    public static CacheFinder<Model_Product> find = new CacheFinder<>(Model_Product.class);
}