package utilities.goPay.helps_objects;

import models.project.global.financial.Invoice_item;
import utilities.enums.Currency;

import java.util.ArrayList;
import java.util.List;

public class GoPay_Recurrence {

    public Long amount;
    public Currency currency;
    public String order_number;
    public String order_description;
    public List<GoPay_Items> items = new ArrayList<>();




    // Pomocné Třídy a metody

    public void setItems(List<Invoice_item> invoice_items){

        for(Invoice_item item :invoice_items){
            amount += Math.round(item.unit_price*100);

            GoPay_Items go_item = new GoPay_Items();
            go_item.name = item.name;
            go_item.amount = Math.round(item.unit_price*100);
            //  go_item.fee = (long) 0;
            //  go_item.quantity = item.quantity;

            items.add(go_item);
        }

    }

    public class GoPay_Items{
        public String name;
        public Long amount;
    }

}


