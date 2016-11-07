package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.LibraryGroup;
import models.compiler.SingleLibrary;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.GeneralTariff_Extensions;
import models.project.global.financial.Invoice;
import models.project.global.financial.Payment_Details;
import play.Configuration;
import play.libs.Json;
import utilities.Server;
import utilities.enums.Currency;
import utilities.enums.Payment_method;
import utilities.enums.Payment_mode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Product extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)                 public Long id;
                                                                            public String product_individual_name;

    @JsonIgnore                                  @ManyToOne(fetch = FetchType.LAZY)    public GeneralTariff general_tariff;
    @JsonIgnore    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Payment_mode mode;
    @JsonIgnore    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Payment_method method;

                                                                            public String subscription_id;
                    @JsonIgnore                                             public String fakturoid_subject_id;        // ID účtu ve fakturoidu
                    @JsonIgnore                                             public Long gopay_id;

                                                                            public boolean active;           // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                        @JsonIgnore         public Integer monthly_day_period;  // Den v měsíci, kdy bude obnovována platba // Nejvyšší možné číslo je 28!!!
                                                        @JsonIgnore         public Integer monthly_year_period;  // Měsíc v roce, kdy bude obnovována platba // Nejvyšší možné číslo je 12!!!

                                                                            public Date paid_until_the_day;  // Termín do kdy je služba předplacena (Pokud zaplatím na měsíc teď tak je to čas teď + 1 měsíc.
                                                        @JsonIgnore         public boolean on_demand_active; // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                                                            public double remaining_credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure
   @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)        public Currency currency;


   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Project> projects = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<Invoice> invoices = new ArrayList<>();

   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<SingleLibrary> single_libraries  = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    public List<LibraryGroup> library_groups  = new ArrayList<>();


               @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)   public Payment_Details payment_details;


    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "products") @JoinTable(name = "typePostsTable") public List<GeneralTariff_Extensions> extensionses = new ArrayList<>();



 /* JSON PROPERTY METHOD -----------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true)
    @JsonProperty public List<Invoice> invoices(){

        List<Invoice> invoices = Invoice.find.where().eq("product.id", this.id).findList();
        return invoices;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty @ApiModelProperty(required = false, value = "Only if Payment Mode is CREDIT ")
    public Integer remaining_credit(){
        if(this.mode.name().equals( Payment_mode.per_credit.name())) return null;
        return 0;
    }


    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String product_type(){
        return GeneralTariff.find.where().eq("product.id", id).select("tariff_name").findUnique().tariff_name;
    }


    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_mode(){
        switch (mode) {
            case free:        {return  "Free Account"; }
            case monthly:     {return  "Monthly payment"; }
            default: return   "Undefined state";
        }
    }

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true)
    public String payment_method(){
        try{
            switch (method) {
                case bank_transfer:        {return  "Bank transfer payment."; }
                case credit_card:          {return  "Credit-Card payment."; }
                case free:                 {return  "Free Account"; }
                default: return   "Undefined state";
            }
        }catch (NullPointerException e) {
            return "Not set yet";
        }
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore private String azure_product_link;

    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.azure_product_link = get_Container().getName() + "/" + UUID.randomUUID().toString();
            if (Product.find.where().eq("azure_product_link", azure_product_link ).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            this.subscription_id =  UUID.randomUUID().toString().substring(0, 12);
            if (Product.find.where().eq("subscription_id", subscription_id ).findUnique() == null) break;
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
        return  azure_product_link;
    }



/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static final String read_permission_docs   = "read: Bla bla bla";
    public static final String create_permission_docs   = "read: Bla bla bla";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission()              {  return true;  }
    @JsonIgnore   @Transient                                    public boolean read_permission()                {  return payment_details.person.id.equals(SecurityController.getPerson().id) || SecurityController.getPerson().has_permission("Product_read");  }
                  @Transient                                    public boolean edit_permission()                {  return payment_details.person.id.equals(SecurityController.getPerson().id) || SecurityController.getPerson().has_permission("Product_edit");  }
                  @Transient                                    public boolean act_deactivate_permission()      {  return payment_details.person.id.equals(SecurityController.getPerson().id) || SecurityController.getPerson().has_permission("Product_act_deactivate"); }
    @JsonIgnore   @Transient                                    public boolean delete_permission()              {  return SecurityController.getPerson().has_permission("Product_delete");}

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


/* Price_List ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Double get_all_monthly_fees(){
        return  get_price_general_fee();
    }

    @JsonIgnore @Transient public Long get_days_to_blocation(){ return Math.round(  (paid_until_the_day.getTime() - new Date().getTime() ) / (double) 86400000); }

    @JsonIgnore @Transient public Double get_price_general_fee()  {
        switch (currency) {
            case EUR:     {return  general_tariff.eur;  }
            case USD:     {return  general_tariff.usd;  }
            case CZK:     {return  general_tariff.czk;  }
            default: return null;
        }

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<Long,Product> find = new Finder<>(Product.class);


/* Private classes values ----------------------------------------------------------------------------------------------*/


}








