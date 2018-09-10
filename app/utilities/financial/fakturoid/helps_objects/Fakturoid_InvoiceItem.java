package utilities.financial.fakturoid.helps_objects;

import play.data.validation.Constraints;

public class Fakturoid_InvoiceItem {
    // needs to be present when we are converting object received from Fakturoid
    @Constraints.Required
    public Integer id;

    public String name;    //Popis řádku faktur
    public Double quantity;    //Množství
    public String unit_name;    //Množstevní jednotka
    public Double unit_price;    //Cena za jednotuku
    public Integer vat_rate;    //Sazba DPH (pro neplátce DPH 0)
    public Double unit_price_without_vat; // Cena za jednotku bez DPH (readonly)
    public Double unit_price_with_vat; // Cena za jednotku včetně DPH (readonly)
}
