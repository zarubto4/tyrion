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
import play.libs.Json;
import utilities.Server;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;
import utilities.logger.Class_Logger;

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

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Product.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public String id;
                                             @ApiModelProperty(required = true) public String product_individual_name;

    @JsonIgnore                              @ManyToOne(fetch = FetchType.LAZY) public Model_GeneralTariff general_tariff;
    @JsonIgnore @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Payment_mode mode;
    @JsonIgnore @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Payment_method method;

                                             @ApiModelProperty(required = true) public String subscription_id;
                                                                    @JsonIgnore public String fakturoid_subject_id; // ID účtu ve fakturoidu
                                                                    @JsonIgnore public Long gopay_id;

                                             @ApiModelProperty(required = true) public boolean active;              // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                                    @JsonIgnore public Integer monthly_day_period;  // Den v měsíci, kdy bude obnovována platba // Nejvyšší možné číslo je 28!!!
                                                                    @JsonIgnore public Integer monthly_year_period; // Měsíc v roce, kdy bude obnovována platba // Nejvyšší možné číslo je 12!!!

                                            @ApiModelProperty(required = true) public Date date_of_create;
                                                                    @JsonIgnore public boolean on_demand_active;    // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                             @ApiModelProperty(required = true) public double remaining_credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure


   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Project> projects = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Model_Invoice> invoices = new ArrayList<>();


               @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)                           public Model_PaymentDetails payment_details;


    @ManyToMany(cascade = CascadeType.ALL, mappedBy="products") @JoinTable(name="typePostsTable")   public List<Model_GeneralTariffExtensions> extensions = new ArrayList<>();



/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true)
    @JsonProperty public List<Model_Invoice> invoices(){

        if(this.invoices == null || this.invoices.isEmpty()) this.invoices =  Model_Invoice.find.where().eq("product.id", this.id).order().desc("date_of_create").findList();
        return invoices;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty @ApiModelProperty(required = true)
    public Double remaining_credit(){
       return this.remaining_credit;
    }


    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String product_type(){
        return Model_GeneralTariff.find.where().eq("product.id", id).select("tariff_name").findUnique().tariff_name;
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

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override public void save() {

        date_of_create = new Date();

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

    @ApiModel(description = "Model for Proforma Details for next invoice",
            value = "Next_Invoice_Product")
    public class Next_Invoice_Product{
        @ApiModelProperty(required = true, readOnly = true) public String id;

    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_product_link;

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
        return  azure_product_link;
    }



/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static final String read_permission_docs   = "read: Bla bla bla";    // TODO
    public static final String create_permission_docs   = "read: Bla bla bla";  // TODO

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission()              {  return true;  }
    @JsonIgnore   @Transient                                    public boolean read_permission()                {  return payment_details.person.id.equals(Controller_Security.get_person().id) || Controller_Security.get_person().has_permission("Product_read");  }
                  @Transient                                    public boolean edit_permission()                {  return payment_details.person.id.equals(Controller_Security.get_person().id) || Controller_Security.get_person().has_permission("Product_edit");  }
                  @Transient                                    public boolean act_deactivate_permission()      {  return payment_details.person.id.equals(Controller_Security.get_person().id) || Controller_Security.get_person().has_permission("Product_act_deactivate"); }
    @JsonIgnore   @Transient                                    public boolean delete_permission()              {  return Controller_Security.get_person().has_permission("Product_delete");}

    // Project
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean  create_new_project()             {
        return active;
    }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public JsonNode create_new_project_if_not(){
        ObjectNode result = Json.newObject();
            result.put("tariff", general_tariff.tariff_name);

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


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Product get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_Product get_byNameAndOwner(String name) {
        return find.where().eq("product_individual_name", name).eq("payment_details.person.id", Controller_Security.get_person().id).findUnique();
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
        return find.where().eq("active",true).eq("payment_details.person.id", owner_id).select("id").select("product_individual_name").select("general_tariff.tariff_name").findList();
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Product> find = new Finder<>(Model_Product.class);

}








