package utilities.swagger.input;

import play.data.validation.Constraints;

public class Swagger_Payment_Refund {

    public boolean whole;

    public Double amount;

    @Constraints.Required
    public String reason;
}
