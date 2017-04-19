package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.Configuration;
import play.data.Form;
import play.libs.Json;
import utilities.Server;
import utilities.enums.Enum_BusinessModel;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;
import utilities.enums.Enum_Payment_status;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.loggy.Loggy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Product",
        value = "Product")
public class Model_Product extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

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

                                                                    @JsonIgnore public double credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure

                                 @Column(columnDefinition = "TEXT") @JsonIgnore public String financial_history;


    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Project> projects = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Invoice> invoices = new ArrayList<>();

                                          @OneToOne(mappedBy="product", cascade = CascadeType.ALL)  public Model_PaymentDetails payment_details;

                                          @OneToMany(mappedBy="product", cascade = CascadeType.ALL) public List<Model_ProductExtension> extensions = new ArrayList<>();


 /* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true)
    @JsonProperty public List<Model_Invoice> invoices(){

        if(this.invoices == null || this.invoices.isEmpty()) this.invoices =  Model_Invoice.find.where().eq("product.id", this.id).order().desc("created").findList();
        return invoices;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @ApiModelProperty(required = false)
    public Double remaining_credit(){

        if (this.mode == Enum_Payment_mode.per_credit) return this.credit;
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

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public double price(){
        double total = 0.0;
        for(Model_ProductExtension extension : this.extensions){
            Double price = extension.getPrice();

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
    public void credit_upload(double credit){
        try {

            this.credit += credit;

            if (!this.active && this.credit > 0) this.active = true; // TODO notifikace - různé stavy

            this.update();

            // TODO notifikace - nahrál se credit/nepovedlo se

        } catch (Exception e) {
            Loggy.internalServerError("Model_Product:: credit_upload:", e);
        }
    }

    @JsonIgnore
    public void credit_remove(double credit){
        try {

            this.credit -= credit;

            if (this.active && this.credit < 0) this.active = false; // TODO notifikace - různé stavy

            this.update();

            // TODO notifikace - odečetl se credit/nepovedlo se

        } catch (Exception e) {
            Loggy.internalServerError("Model_Product:: credit_remove:", e);
        }
    }

    @JsonIgnore
    public boolean terminateOnDemand(){
        try {

            Utilities_GoPay_Controller.terminateOnDemand(this);

            this.gopay_id = null;
            this.on_demand = false;
            this.mode = Enum_Payment_mode.per_credit;
            this.update();

            return true;

        } catch (Exception e) {
            Loggy.internalServerError("Model_Product:: terminateOnDemand:", e);
            return false;
        }
    }

    @JsonIgnore
    public History getFinancialHistory(){
        try {

            if (this.financial_history == null) return new History();

            Form<History> form = Form.form(History.class).bind(Json.parse(this.financial_history));
            if(form.hasErrors()) throw new Exception("Error parsing product financial history");
            return form.get();

        } catch (Exception e) {
            Loggy.internalServerError("Model_Product:: getFinancialHistory:", e);
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
            event.date = new Date();

            history.history.add(event);

            this.financial_history = Json.toJson(history).toString();
            this.update();

        } catch (Exception e) {
            Loggy.internalServerError("Model_Product:: archiveEvent:", e);
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class HistoryEvent{

        public Date date;
        public String event;
        public String description;
        public String invoice_id;
    }

    public class History{

        public List<HistoryEvent> history = new ArrayList<>();
    }
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA -----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_product_link;

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

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("product");
        }catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException();
        }

    }

    @JsonIgnore @Transient
    public String get_path(){
        return azure_product_link;
    }



/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static final String read_permission_docs   = "read: Bla bla bla";
    public static final String create_permission_docs   = "read: Bla bla bla";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()         {  return true;  }
    @JsonIgnore   @Transient public boolean read_permission()           {  return payment_details.person.id.equals(Controller_Security.getPerson().id) || Controller_Security.getPerson().has_permission("Product_read");  }
                  @Transient public boolean edit_permission()           {  return payment_details.person.id.equals(Controller_Security.getPerson().id) || Controller_Security.getPerson().has_permission("Product_edit");  }
                  @Transient public boolean act_deactivate_permission() {  return payment_details.person.id.equals(Controller_Security.getPerson().id) || Controller_Security.getPerson().has_permission("Product_act_deactivate"); }
    @JsonIgnore   @Transient public boolean delete_permission()         {  return Controller_Security.getPerson().has_permission("Product_delete");}

    // Project
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean  create_new_project()             {
        return active;
    }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public JsonNode create_new_project_if_not(){
        ObjectNode result = Json.newObject();
            result.put("tariff", tariff.name);

        if(! active) result.put("message", Configuration.root().getInt("Your Product is not Paid for this moment"));
        return  result;
    }

    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_register_new_Device()     {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_C_Program()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_M_Project()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_M_Program()           {  return true;  }

    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_B_program()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_Instrance()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_own_server()              {  return true;  }


    public enum permissions{Product_update, Product_read, Product_edit,Product_act_deactivate, Product_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Product> find = new Finder<>(Model_Product.class);

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
}








