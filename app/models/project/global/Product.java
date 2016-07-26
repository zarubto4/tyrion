package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.gopay.api.v3.model.common.Currency;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.financial.Credit_Card;
import models.project.global.financial.Invoice;
import models.project.global.financial.Payment_Details;
import play.Configuration;
import play.libs.Json;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Product extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)              public Long id;

    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Product_Type type;
    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)    public Payment_mode payment_mode;
                    @JsonIgnore   @OneToOne(mappedBy = "product")       public Credit_Card credit_card;

                    @JsonIgnore                                         public String subject_id;        // ID účtu ve fakturoidu


                                                                        public boolean active;           // Jestli je projekt aktivní (může být zmražený, nebo třeba ještě neuhrazený platbou)


                                                                        public double remaining_credit;     // Zbývající kredit pokud je typl platby per_credit - jako na Azure
    @Enumerated(EnumType.STRING)   @ApiModelProperty(required = true)   public Currency currency_type;


   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL) public List<Project> projects = new ArrayList<>();

                                 @OneToOne(mappedBy = "product")         public Payment_Details payment_details;

   @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL) List<Invoice> invoices = new ArrayList<>();


 /* JSON PROPERTY METHOD -----------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static final String read_permission_docs   = "read: Bla bla bla";
    public static final String create_permission_docs   = "read: Bla bla bla";

    @JsonIgnore   @Transient                                    public boolean create_permission()              {  return true;  }

    // Project
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_new_project()             { return projects.size() <  ( Configuration.root().getInt("Byzance.tariff." + type + ".maximum_project") < 0 ? 1000000 : Configuration.root().getInt("Byzance.tariff." + type + ".maximum_project") ) ;} // Chráním hodnotu -1 - která je určena pro nekonečno (bez limitů)
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


    @JsonIgnore @Transient public Long get_price_general_fee()             {return(long)(Configuration.root().getDouble("Byzance.tariff."+type.name()+".price_list."+  "general_fee"                           +"." + currency_type.name()) * 100 );}
    @JsonIgnore @Transient public Long get_price_month_device_connection() {return(long)(Configuration.root().getDouble("Byzance.tariff."+type.name()+".price_list."+  "get_price_month_device_connection"     +"." + currency_type.name()) * 100 );}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Product> find = new Finder<>(Product.class);


/* ENUM values ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public Date trial_end;

    public enum Product_Type{
        alpha,
        trial,
        community,
        free,
        tier1,
        tier2,
        tier3,
        tier4,
        tier5,
    }

    public Date monthly_day_period;
    public Date time_of_end_period;

    public enum Payment_mode{
        free,
        monthly,
        annual,
        per_credit
    }


}



