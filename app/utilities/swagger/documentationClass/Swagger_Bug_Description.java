package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Json Model for adding description to bug",
          value = "Bug_Description")
public class Swagger_Bug_Description {

    public String description;
}
