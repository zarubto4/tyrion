package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.PaymentMethod;

@ApiModel(description = "Json Model with Product Payment Details",
        value = "PaymentDetailsUpdate")
public class Swagger_PaymentDetails_Update {
    @Constraints.Required
    @ApiModelProperty(required = true, value =  "The way how the customer will pay for the product.")
    public PaymentMethod payment_method;
}
