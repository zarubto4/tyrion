package utilities.swagger.outboundClass;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for specific Tariff and price plan",
          value = "Tariff")
public class Swagger_Tariff {

    @ApiModelProperty(required = true, readOnly = true, value = "List of Individual Tariffs")
    public List<Individuals_Tariff> tariffs = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true, value = "List of Additional Packages - More Slots for IoT, Projects etc..")
    public List<Additional_package> packages = new ArrayList<>();



    public class Individuals_Tariff {

        @ApiModelProperty(required = true, readOnly = true)
        public String tariff_name;

        @ApiModelProperty(required = true, readOnly = true, value = "return this value for registration new Product Tariff")
        public String identificator;

        @ApiModelProperty(required = true, readOnly = true, value = "This value indicates that for creating new Product Tariff, you have to send all value with \"company_\"  ")
        public boolean company_details_required;

        @ApiModelProperty(required = true, readOnly = true, value = "This value indicates that for creating new Product Tariff  User have to pay throw the Credit-Card Gateway or send money via Bank Account")
        public boolean required_payment_mode;

        @ApiModelProperty(required = true, readOnly = true)
        public Price price = new Price();

        @ApiModelProperty(required = true, readOnly = true, value = "Show each record in Product Tariff Object")
        public List<Swagger_Tariff_Label> labels = new ArrayList<>();
    }


    public class Additional_package {

        @ApiModelProperty(required = true, readOnly = true)
        public String package_name;

        @ApiModelProperty(required = true, readOnly = true)
        public String identificator;

        @ApiModelProperty(required = true, readOnly = true)
        public Price price = new Price();

        @ApiModelProperty(required = true, readOnly = true)
        public List<Swagger_Tariff_Label> labels = new ArrayList<>();

    }


    public class Price {
        @ApiModelProperty(required = true, readOnly = true, value = "in Double - show CZK")
        public Double CZK;

        @ApiModelProperty(required = true, readOnly = true,  value = "in Double - show €")
        public Double EUR;
    }

    @JsonIgnore public Price get_new_Price(Double CZK, Double EUR){
        Price price = new Price();
        price.CZK = CZK;
        price.EUR = EUR;
        return price;
    }


// Pomocné metody

    @JsonIgnore public Individuals_Tariff get_new_Tariff(){
        return new Individuals_Tariff();
    }
    @JsonIgnore public Additional_package get_new_Additional_package(){
        return new Additional_package();
    }
    @JsonIgnore public List<Swagger_Tariff_Label> get_new_Label(String json_in_string) throws IOException{  return  new ObjectMapper().readValue(json_in_string, new TypeReference<List<Swagger_Tariff_Label>>(){});}


}



