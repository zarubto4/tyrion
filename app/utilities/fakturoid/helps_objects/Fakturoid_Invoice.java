package utilities.fakturoid.helps_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.global.financial.Model_InvoiceItem;
import utilities.enums.Currency;

import java.util.List;

public class Fakturoid_Invoice {

    @JsonIgnore  public Long custom_id;
    public String client_name;
    public String subject_id;
    public Currency currency;
    public Integer due = 10;

    public boolean proforma;
    public boolean  partial_proforma;
    public List<Model_InvoiceItem> lines;

}
