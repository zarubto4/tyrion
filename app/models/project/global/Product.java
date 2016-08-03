package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.LibraryGroup;
import models.compiler.SingleLibrary;
import models.project.global.financial.Invoice;
import models.project.global.financial.Payment_Details;
import play.Configuration;
import play.libs.Json;
import utilities.Server;
import utilities.goPay.helps_objects.enums.Currency;
import utilities.goPay.helps_objects.enums.Payment_method;

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


    @JsonIgnore    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Product_Type type;
    @JsonIgnore    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Payment_mode mode;
    @JsonIgnore    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Payment_method method;

                    @JsonIgnore                                             public String fakturoid_subject_id;        // ID účtu ve fakturoidu
                    @JsonIgnore                                             public Long gopay_id;

                                                        @JsonIgnore         public boolean active;           // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)

                                                        @JsonIgnore         public Integer monthly_day_period;  // Den v měsíci, kdy bude obnovována platba // Nejvyšší možné číslo je 28!!!
                                                        @JsonIgnore         public Integer monthly_year_period;  // Měsíc v roce, kdy bude obnovována platba // Nejvyšší možné číslo je 12!!!

                                                                            public Date paid_until_the_day;  // Termín do kdy je služba předplacena (Pokud zaplatím na měsíc teď tak je to čas teď + 1 měsíc.
                                                        @JsonIgnore         public boolean on_demand_active; // Jestli je povoleno a zaregistrováno, že Tyrion muže žádat o provedení platby

                                                                            public double remaining_credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure
   @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)        public Currency currency;


   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL)    public List<Project> projects = new ArrayList<>();
               @OneToMany(mappedBy="product", cascade = CascadeType.ALL)    public List<Invoice> invoices = new ArrayList<>();

   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL)    public List<SingleLibrary> single_libraries  = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL)    public List<LibraryGroup> library_groups  = new ArrayList<>();


               @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)   public Payment_Details payment_details;



 /* JSON PROPERTY METHOD -----------------------------------------------------------------------------------------------*/

   // @JsonProperty public Long product_detail_id(){return  payment_details.id;}
   @JsonProperty
   @Transient  @ApiModelProperty(required = true, readOnly = true)
   public String product_type(){

       switch (type) {
           case alpha:       {return  "Alpha - Temporal limitation";}
           case free:        {return  "Free Account ";}
           case business:    {return  "Enterprise account";}
           default: return  "Undefined state";
       }
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
                case bank:        {return  "Bank transfer payment."; }
                case credit_card: {return  "Credit-Card payment."; }
                default: return   "Undefined state";
            }
        }catch (NullPointerException e) {
            return "Not set yet";
        }
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore            private String azure_product_link;

    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.azure_product_link = get_Container().getName() + "/" + UUID.randomUUID().toString();
            if (Product.find.where().eq("azure_product_link", azure_product_link ).findUnique() == null) break;
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
    @JsonIgnore   @Transient                                    public boolean read_permission()                {  return true;  }
                  @Transient                                    public boolean edit_permission()                {  return true;  }


    // Project
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_project()             { return  true; }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public JsonNode create_new_project_if_not()     {
        ObjectNode result = Json.newObject();
        result.put("tariff", String.valueOf(type));
        result.put("maximum", Configuration.root().getInt("Byzance.tariff." + type + ".maximum_project"));
        result.put("message", "Sorry, but you have no free slots for creating another project");
        return  result;
    }

    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_register_new_Device()     {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_C_Program()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_M_Project()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_M_Program()           {  return true;  }

    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_B_program()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_Instrance()           {  return true;  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_own_server()              {  return true;  }






    public enum permissions{}


/* Price_List ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Double get_all_monthly_fees(){
        return  get_price_general_fee();
    }

    @JsonIgnore @Transient public Double get_price_general_fee()  {return (Configuration.root().getDouble("Byzance.tariff."+type.name()+".price_list."+  "general_fee.monthly"      +"." + currency.name() )) ;}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<Long,Product> find = new Finder<>(Product.class);


/* ENUM values ---------------------------------------------------------------------------------------------------------*/

    public enum Product_Type{
        alpha,
        free,
        business
    }

    public enum Payment_mode{
        free,
        monthly,
        annual,
        per_credit
    }

    public enum Recurrence_cycle{
        ON_DEMAND // Režim, kdy Tyrion žádá sám Go_Pay o platbu!
    }



}



