package utilities.goPay.helps_objects;

import play.data.validation.Constraints;

public class GoPay_Result {

    @Constraints.Required
    public Long id;

    public Long parent_id;

    @Constraints.Required
    public String order_number;

    @Constraints.Required
    public String state;

    @Constraints.Required
    public Long amount;

    public Recurrence recurrence;

    @Constraints.Required
    public String gw_url;
}
