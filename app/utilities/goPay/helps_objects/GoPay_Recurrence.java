package utilities.goPay.helps_objects;

import models.Model_InvoiceItem;
import utilities.enums.Enum_Currency;

import java.util.ArrayList;
import java.util.List;

public class GoPay_Recurrence {

    public long amount = 0;
    public Enum_Currency currency;
    public String order_number;
    public String order_description;
    public List<GoPay_Items> items = new ArrayList<>();




    // Pomocné Třídy a metody

    public void setItems(List<Model_InvoiceItem> invoice_items){

        for(Model_InvoiceItem item :invoice_items){
            amount += item.unit_price / 10;

            GoPay_Items go_item = new GoPay_Items();
            go_item.name = item.name;
            go_item.amount = (long) item.unit_price/10;
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


