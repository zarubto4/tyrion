package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Model_Product;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for details about Tariff - all financial details in one Json",
             value = "Financial_Summary")
public class Swagger_Financial_Summary {

   @ApiModelProperty(required = true, readOnly = true)
   public List<Model_Product> products = new ArrayList<>();


}

