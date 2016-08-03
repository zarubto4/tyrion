package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Product;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for details about Tariff - all financial details in one Json",
             value = "Financial_Summary")
public class Swagger_Financial_Summary {

   @ApiModelProperty(required = true, readOnly = true)
   public List<Product> products = new ArrayList<>();


}

