package utilities.swagger.documentationClass;

import play.data.validation.Constraints;

public class Swagger_Payment_Refund {

    public boolean whole;

    public Long amount;

    @Constraints.Required
    public String reason;
}
