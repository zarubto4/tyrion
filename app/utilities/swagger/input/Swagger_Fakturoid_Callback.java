package utilities.swagger.input;

import play.data.validation.Constraints;

import java.util.Date;

public class Swagger_Fakturoid_Callback {

    @Constraints.Required
    public Long invoice_id;

    @Constraints.Required
    public String number;

    @Constraints.Required
    public String status;

    @Constraints.Required
    public double total;

    public Date paid_at;

    @Constraints.Required
    public String event_name;
}
