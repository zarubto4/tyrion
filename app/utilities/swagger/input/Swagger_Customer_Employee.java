package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Customer_Employee", description = "For adding employees to a company.")
public class Swagger_Customer_Employee {

    @Constraints.Required
    public List<String> mails;

    @Constraints.Required
    public UUID customer_id;
}
