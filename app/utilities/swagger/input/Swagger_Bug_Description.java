package utilities.swagger.input;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Json Model for adding description to bug",
          value = "Bug_Description")
public class Swagger_Bug_Description {

    public String description;
}
