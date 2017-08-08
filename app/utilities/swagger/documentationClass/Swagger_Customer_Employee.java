package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.List;

@ApiModel(value = "Customer_Employee", description = "For adding employees to a company.")
public class Swagger_Customer_Employee {

    @Constraints.Required
    public List<String> mails;

    @Constraints.Required
    public String customer_id;
}
