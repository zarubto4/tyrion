package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import models.project.global.Product;

import javax.persistence.*;


@Entity
public class Payment_Details extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)   public Long id;

    @JsonIgnore @OneToOne()   @JoinColumn(name="person_id")   public Person  person;
    @JsonIgnore @OneToOne()   @JoinColumn(name="product_id")  public Product product;

    public boolean company_account; // Rozhoduji se zda jde o detaily firemní nebo osobní

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_name;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_email;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_authorized_phone;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_invoice_email;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String company_web;

    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String registration_no;
    @ApiModelProperty(required = false, value = "Only if Product is for business") @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty  public String VAT_number;


    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String street;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String street_number;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String city;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String zip_code;
    @ApiModelProperty(required = true, value = "Used for billing and invoices") public String country;



/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Payment_Details> find = new Finder<>(Payment_Details.class);




}
