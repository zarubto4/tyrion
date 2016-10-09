package utilities.fakturoid.helps_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.global.financial.Invoice_item;
import utilities.enums.Currency;

import java.util.List;

public class Fakturoid_Invoice {

    @JsonIgnore  public Long custom_id;
    public String client_name;
    public String subject_id;
    public Currency currency;
    public String payment_method;
    public Integer due = 10;

    public boolean proforma;
    public boolean  partial_proforma;
    public List<Invoice_item> lines;

}
