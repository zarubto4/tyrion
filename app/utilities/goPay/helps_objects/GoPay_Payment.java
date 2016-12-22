package utilities.goPay.helps_objects;


import com.fasterxml.jackson.annotation.JsonInclude;
import models.project.global.financial.Model_InvoiceItem;
import utilities.Server;
import utilities.enums.Currency;

import java.util.ArrayList;
import java.util.List;

public class GoPay_Payment {

    public GoPay_Payer payer;
    public GoPay_Target target = new GoPay_Target();

    public long  amount = 0;
    public Currency currency;
    public String order_number;

    public String order_description;
    public List<GoPay_Items> items = new ArrayList<>();

    public GoPay_Callback callback = new GoPay_Callback();
    public String lang;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Recurrence recurrence;

 // Pomocné metody -----------------------------------------------------------------------------------------------------

    public void setItems(List<Model_InvoiceItem> invoice_items){

        for(Model_InvoiceItem item :invoice_items){
            amount += Math.round(item.unit_price*100);

            GoPay_Items go_item = new GoPay_Items();
            go_item.name = item.name;
            go_item.amount =Math.round(item.unit_price*100);
       //     go_item.fee = (long) 0;
       //     go_item.quantity = item.quantity;

            items.add(go_item);
        }

    }



 // Pomocné Třídy -----------------------------------------------------------------------------------------------------

    public class GoPay_Callback{

        public String return_url = Server.GoPay_return_url;
        public String notification_url = Server.GoPay_notification_url;
    }

    public class GoPay_Items{
        public String name;
        public Long amount;
    }

    public class GoPay_Target{

        public String type = "ACCOUNT";
        public Long goid = Server.GoPay_go_id;

    }

}
