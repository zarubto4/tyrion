package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for new Producer",
         value = "Products_All_Applicable ")
public class Swagger_Product_Applicable {

    public List<Product_Detail> list = new ArrayList<>();

    public void add(Long id, String product_individual_name, String tariff_name) {
        this.list.add( new Product_Detail(id, product_individual_name, tariff_name) );
    }


    public class Product_Detail{

        public Product_Detail(Long id, String product_individual_name, String tariff_name){
            this.id = id;
            this.product_individual_name = product_individual_name;
            this.tariff_name = tariff_name;
        }

        public Long id;
        public String product_individual_name;
        public String tariff_name;
    }
}
