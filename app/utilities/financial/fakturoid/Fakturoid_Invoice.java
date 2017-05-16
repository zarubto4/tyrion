package utilities.financial.fakturoid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_InvoiceItem;
import utilities.enums.Enum_Currency;

import java.util.List;

public class Fakturoid_Invoice {

    @JsonIgnore  public String custom_id;
    public String client_name;
    public String subject_id;
    public Enum_Currency currency;
    public Integer due = 10;

    public boolean proforma;
    public boolean  partial_proforma;
    public List<Model_InvoiceItem> lines;

}
